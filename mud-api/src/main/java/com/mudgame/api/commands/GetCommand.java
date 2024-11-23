package com.mudgame.api.commands;

import com.mudgame.entities.InventoryResult;
import com.mudgame.entities.Item;
import com.mudgame.entities.Player;
import com.mudgame.entities.Room;

import java.util.Optional;

// Command to pick up items
public interface GetCommand extends GameCommand {
    @Override
    default String getName() {
        return "get";
    }

    @Override
    default String getHelp() {
        return "get <item> [amount] - Pick up an item from the room";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("What do you want to get?");
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

        // Find the item in the room
        Optional<Item> roomItem = currentRoom.getItems().stream()
                .filter(item -> item.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (roomItem.isEmpty()) {
            return CommandResult.failure("You don't see that here.");
        }

        // Try to add to inventory
        InventoryResult result = player.pickupItem(roomItem.get());
        if (result.isSuccess()) {
            if (result.isPartial()) {
                currentRoom.removeItem(roomItem.get());
                return CommandResult.builder()
                        .success(true)
                        .privateMessage("You picked up " + result.getAmount() + " " + roomItem.get().getName())
                        .roomMessage(player.getFullName() + " picks up " + roomItem.get().getName())
                        .build();
            } else {
                currentRoom.removeItem(roomItem.get());
                return CommandResult.builder()
                        .success(true)
                        .privateMessage("You picked up " + roomItem.get().getName())
                        .roomMessage(player.getFullName() + " picks up " + roomItem.get().getName())
                        .build();
            }
        }

        return CommandResult.failure(result.getMessage());
    }
}
