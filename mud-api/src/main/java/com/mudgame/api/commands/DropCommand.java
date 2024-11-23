package com.mudgame.api.commands;

import com.mudgame.entities.InventoryItem;
import com.mudgame.entities.InventoryResult;
import com.mudgame.entities.Player;
import com.mudgame.entities.Room;

import java.util.Optional;

// Command to drop items
public interface DropCommand extends GameCommand {
    @Override
    default String getName() {
        return "drop";
    }

    @Override
    default String getHelp() {
        return "drop <item> [amount] - Drop an item from your inventory";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("What do you want to drop?");
        }

        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return CommandResult.failure("You are not in any room!");
        }

        String itemName = args[0].toLowerCase();
        int amount = 1;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1) {
                    return CommandResult.failure("Invalid amount specified.");
                }
            } catch (NumberFormatException e) {
                return CommandResult.failure("Invalid amount specified.");
            }
        }

        // Find the item in inventory
        Optional<InventoryItem> inventoryItem = player.getInventory().getItems().stream()
                .filter(item -> item.getItem().getName().toLowerCase().contains(itemName))
                .findFirst();

        if (inventoryItem.isEmpty()) {
            return CommandResult.failure("You don't have that item.");
        }

        // Try to drop the item
        InventoryResult result = player.dropItem(inventoryItem.get().getItem().getId(), amount);
        if (result.isSuccess()) {
            currentRoom.addItem(inventoryItem.get().getItem());
            if (result.isPartial()) {
                return CommandResult.builder()
                        .success(true)
                        .privateMessage("You dropped " + result.getAmount() + " " + inventoryItem.get().getItem().getName())
                        .roomMessage(player.getFullName() + " drops " + inventoryItem.get().getItem().getName())
                        .build();
            } else {
                return CommandResult.builder()
                        .success(true)
                        .privateMessage("You dropped " + inventoryItem.get().getItem().getName())
                        .roomMessage(player.getFullName() + " drops " + inventoryItem.get().getItem().getName())
                        .build();
            }
        }

        return CommandResult.failure(result.getMessage());
    }
}
