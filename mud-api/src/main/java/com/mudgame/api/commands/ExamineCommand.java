package com.mudgame.api.commands;

import com.mudgame.entities.EquipmentSlot;
import com.mudgame.entities.InventoryItem;
import com.mudgame.entities.Item;
import com.mudgame.entities.Player;
import com.mudgame.entities.Room;

import java.util.Optional;

// Command to examine items
public interface ExamineCommand extends GameCommand {
    @Override
    default String getName() {
        return "examine";
    }

    @Override
    default String getHelp() {
        return "examine <item> - Get detailed information about an item";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("What do you want to examine?");
        }

        String itemName = args[0].toLowerCase();

        // Check inventory first
        Optional<InventoryItem> inventoryItem = player.getInventory().getItems().stream()
                .filter(item -> item.getItem().getName().toLowerCase().contains(itemName))
                .findFirst();

        if (inventoryItem.isPresent()) {
            return CommandResult.success(inventoryItem.get().getItem().getDetailedDescription());
        }

        // Check equipped items
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Optional<Item> equippedItem = player.getEquipment().getEquippedItem(slot);
            if (equippedItem.isPresent() &&
                    equippedItem.get().getName().toLowerCase().contains(itemName)) {
                return CommandResult.success(equippedItem.get().getDetailedDescription());
            }
        }

        // Check room items
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom != null) {
            Optional<Item> roomItem = currentRoom.getItems().stream()
                    .filter(item -> item.getName().toLowerCase().contains(itemName))
                    .findFirst();

            if (roomItem.isPresent()) {
                return CommandResult.success(roomItem.get().getDetailedDescription());
            }
        }

        return CommandResult.failure("You don't see that item anywhere.");
    }
}
