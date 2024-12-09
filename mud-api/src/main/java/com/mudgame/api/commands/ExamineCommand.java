package com.mudgame.api.commands;

import com.mudgame.entities.EquipmentSlot;
import com.mudgame.entities.InventoryItem;
import com.mudgame.entities.Item;
import com.mudgame.entities.NPC;
import com.mudgame.entities.NPCType;
import com.mudgame.entities.Player;
import com.mudgame.entities.Room;

import java.util.Map;
import java.util.Optional;

public interface ExamineCommand extends GameCommand {
    @Override
    default String getName() {
        return "examine";
    }

    @Override
    default String getHelp() {
        return "examine <target> - Get detailed information about a player, NPC, or item";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("What do you want to examine?");
        }

        String target = String.join(" ", args).toLowerCase();

        // Check if examining self
        if (target.equals("self") || target.equals(player.getFirstName().toLowerCase())) {
            return CommandResult.success(getPlayerStats(player));
        }

        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return CommandResult.failure("You are not in any room!");
        }

        // Check other players in room
        Optional<Player> targetPlayer = currentRoom.getPlayers().stream()
                .filter(p -> p.getFullName().toLowerCase().contains(target))
                .findFirst();

        if (targetPlayer.isPresent()) {
            return CommandResult.success(getPlayerDescription(targetPlayer.get()));
        }

        // Check NPCs
        Optional<NPC> targetNPC = currentRoom.getNPCs().stream()
                .filter(npc -> npc.getName().toLowerCase().contains(target))
                .findFirst();

        if (targetNPC.isPresent()) {
            return CommandResult.success(getNPCDescription(targetNPC.get()));
        }

        // Check inventory items
        Optional<InventoryItem> inventoryItem = player.getInventory().getItems().stream()
                .filter(item -> item.getItem().getName().toLowerCase().contains(target))
                .findFirst();

        if (inventoryItem.isPresent()) {
            return CommandResult.success(inventoryItem.get().getItem().getDetailedDescription());
        }

        // Check equipped items
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Optional<Item> equippedItem = player.getEquipment().getEquippedItem(slot);
            if (equippedItem.isPresent() &&
                    equippedItem.get().getName().toLowerCase().contains(target)) {
                return CommandResult.success(equippedItem.get().getDetailedDescription());
            }
        }

        // Check room items
        Optional<Item> roomItem = currentRoom.getItems().stream()
                .filter(item -> item.getName().toLowerCase().contains(target))
                .findFirst();

        if (roomItem.isPresent()) {
            return CommandResult.success(roomItem.get().getDetailedDescription());
        }

        return CommandResult.failure("You don't see that here.");
    }

    private String getPlayerStats(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Your Statistics ===\n\n");

        sb.append(String.format("Name: %s\n", player.getFullName()));
        sb.append(String.format("Level %d %s %s\n",
                player.getLevel(), player.getRace(), player.getCharacterClass()));

        if (player.getSpecialization() != null) {
            sb.append(String.format("Specialization: %s\n", player.getSpecialization()));
        }

        sb.append(String.format("\nHealth: %d/%d\n", player.getHealth(), player.getMaxHealth()));
        sb.append(String.format("Energy: %d/%d\n", player.getEnergy(), player.getMaxEnergy()));
        sb.append(String.format("Credits: %d\n", player.getCredits()));

        return sb.toString();
    }

    private String getPlayerDescription(Player target) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("You examine %s:\n\n", target.getFullName()));

        sb.append(String.format("Level %d %s %s\n",
                target.getLevel(), target.getRace(), target.getCharacterClass()));

        if (target.getSpecialization() != null) {
            sb.append(String.format("Specialized as a %s\n", target.getSpecialization()));
        }

        // Add equipment overview with explicit type declarations
        sb.append("\nNotable Equipment:\n");
        for (Map.Entry<EquipmentSlot, Item> entry : target.getEquipment().getEquippedItems().entrySet()) {
            if (entry.getValue() != null) {
                sb.append(String.format("- %s: %s\n", entry.getKey(), entry.getValue().getName()));
            }
        }

        // Add health status
        int healthPercent = (target.getHealth() * 100) / target.getMaxHealth();
        sb.append("\n").append(getHealthDescription(healthPercent));

        return sb.toString();
    }

    private String getNPCDescription(NPC npc) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("You examine %s:\n\n", npc.getName()));
        sb.append(npc.getDescription()).append("\n");

        if (npc.getType() == NPCType.MERCHANT) {
            sb.append("\nThey appear to be a merchant. Perhaps you could trade with them.\n");
        }

        // Add health status for enemies
        if (npc.getType() == NPCType.ENEMY) {
            int healthPercent = (npc.getHealth() * 100) / npc.getMaxHealth();
            sb.append("\n").append(getHealthDescription(healthPercent));
        }

        return sb.toString();
    }

    private String getHealthDescription(int healthPercent) {
        if (healthPercent > 90) return "They appear to be in perfect health.";
        if (healthPercent > 75) return "They have a few minor scratches.";
        if (healthPercent > 50) return "They show signs of injury.";
        if (healthPercent > 25) return "They are badly wounded.";
        if (healthPercent > 10) return "They are critically injured.";
        return "They are barely clinging to life.";
    }
}