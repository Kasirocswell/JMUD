package com.mudgame.api.commands;

import com.mudgame.entities.Player;
import com.mudgame.entities.Room;

public interface LookCommand extends GameCommand {
    @Override
    default String getName() {
        return "look";
    }

    @Override
    default String getHelp() {
        return "look [target] - Look at your surroundings or a specific target";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        Room currentRoom = player.getCurrentRoom();

        if (currentRoom == null) {
            return CommandResult.failure("You are not in any room!");
        }

        // If no arguments, look at room
        if (args.length == 0) {
            return CommandResult.success(currentRoom.getFullDescription());
        }

        // TODO: Implement looking at specific targets (players, items) when we add inventory system
        return CommandResult.failure("You don't see that here.");
    }
}