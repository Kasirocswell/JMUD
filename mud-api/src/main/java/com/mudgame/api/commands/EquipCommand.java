package com.mudgame.api.commands;

import com.mudgame.entities.EquipmentSlot;
import com.mudgame.entities.InventoryItem;
import com.mudgame.entities.InventoryResult;
import com.mudgame.entities.Item;
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
        return "equip <item> - Equip an item from your inventory. Use quotes for items with spaces, e.g. equip \"Recruit's Helmet\"";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("What do you want to equip?");
        }

        // Join args to handle items with spaces in their names
        String itemName = String.join(" ", args).toLowerCase();

        // Find exact matches first, then partial matches
        Optional<InventoryItem> inventoryItem = player.getInventory().getItems().stream()
                .filter(item -> item.getItem().getName().toLowerCase().equals(itemName))
                .findFirst();

        // If no exact match, try partial match
        if (inventoryItem.isEmpty()) {
            inventoryItem = player.getInventory().getItems().stream()
                    .filter(item -> item.getItem().getName().toLowerCase().contains(itemName))
                    .findFirst();
        }

        if (inventoryItem.isEmpty()) {
            return CommandResult.failure("You don't have that item.");
        }

        Item itemToEquip = inventoryItem.get().getItem();

        if (!itemToEquip.isEquippable()) {
            return CommandResult.failure("That item cannot be equipped.");
        }

        // Check if the slot is already occupied
        EquipmentSlot targetSlot = itemToEquip.getSlot();
        Optional<Item> currentlyEquipped = player.getEquipment().getEquippedItem(targetSlot);

        if (currentlyEquipped.isPresent()) {
            return CommandResult.failure("You already have " + currentlyEquipped.get().getName() +
                    " equipped in the " + targetSlot.toString() + " slot. Unequip it first.");
        }

        // Verify level requirement
        if (itemToEquip.getLevelRequired() > player.getLevel()) {
            return CommandResult.failure("You need to be level " +
                    itemToEquip.getLevelRequired() + " to equip this item.");
        }

        InventoryResult result = player.equipItem(itemToEquip);
        if (result.isSuccess()) {
            return CommandResult.builder()
                    .success(true)
                    .privateMessage("You equipped " + itemToEquip.getName() + " in your " +
                            targetSlot.toString() + " slot.")
                    .roomMessage(player.getFullName() + " equips " + itemToEquip.getName())
                    .build();
        }

        return CommandResult.failure(result.getMessage());
    }
}