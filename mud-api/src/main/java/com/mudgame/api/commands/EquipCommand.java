package com.mudgame.api.commands;

import com.mudgame.entities.InventoryItem;
import com.mudgame.entities.InventoryResult;
import com.mudgame.entities.Player;

import java.util.Optional;

// Command to equip items
public interface EquipCommand extends GameCommand {
    @Override
    default String getName() {
        return "equip";
    }

    @Override
    default String getHelp() {
        return "equip <item> - Equip an item from your inventory";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("What do you want to equip?");
        }

        String itemName = args[0].toLowerCase();
        Optional<InventoryItem> inventoryItem = player.getInventory().getItems().stream()
                .filter(item -> item.getItem().getName().toLowerCase().contains(itemName))
                .findFirst();

        if (inventoryItem.isEmpty()) {
            return CommandResult.failure("You don't have that item.");
        }

        if (!inventoryItem.get().getItem().isEquippable()) {
            return CommandResult.failure("That item cannot be equipped.");
        }

        InventoryResult result = player.equipItem(inventoryItem.get().getItem());
        if (result.isSuccess()) {
            return CommandResult.builder()
                    .success(true)
                    .privateMessage("You equipped " + inventoryItem.get().getItem().getName())
                    .roomMessage(player.getFullName() + " equips " + inventoryItem.get().getItem().getName())
                    .build();
        }

        return CommandResult.failure(result.getMessage());
    }
}
