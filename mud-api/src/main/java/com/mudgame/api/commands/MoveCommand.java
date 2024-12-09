package com.mudgame.api.commands;

import com.mudgame.entities.Direction;
import com.mudgame.entities.Player;
import com.mudgame.entities.Room;

public interface MoveCommand extends GameCommand {
    @Override
    default String getName() {
        return "move";
    }

    @Override
    default String getHelp() {
        return "move <direction> - Move in the specified direction (n/s/e/w/u/d/enter/exit)";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("Which direction do you want to move?");
        }

        try {
            Direction direction = Direction.fromCommand(args[0]);
            Room currentRoom = player.getCurrentRoom();

            if (currentRoom == null) {
                return CommandResult.failure("You are not in any room!");
            }

            Room destinationRoom = currentRoom.getExit(direction);
            if (destinationRoom == null) {
                return CommandResult.failure("You cannot go that way.");
            }

            // Remove player from current room
            currentRoom.removePlayer(player);

            // Add player to new room
            destinationRoom.addPlayer(player);

            String moveMessage;
            if (direction == Direction.ENTER) {
                moveMessage = " enters " + destinationRoom.getName() + ".";
            } else if (direction == Direction.EXIT) {
                moveMessage = " exits to " + destinationRoom.getName() + ".";
            } else {
                moveMessage = " has left to the " + direction.name().toLowerCase() + ".";
            }

            return CommandResult.builder()
                    .success(true)
                    .roomMessage(player.getFullName() + moveMessage)
                    .privateMessage(destinationRoom.getFullDescription())
                    .build();

        } catch (IllegalArgumentException e) {
            return CommandResult.failure("Invalid direction. Use n/s/e/w/u/d/enter/exit.");
        }
    }
}