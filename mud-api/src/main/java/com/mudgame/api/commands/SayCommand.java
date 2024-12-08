package com.mudgame.api.commands;

import com.mudgame.entities.Player;

public interface SayCommand extends GameCommand {
    @Override
    default String getName() {
        return "say";
    }

    @Override
    default String getHelp() {
        return "say <message> - Say something to everyone in the room";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length == 0) {
            return CommandResult.failure("What do you want to say?");
        }

        String message = String.join(" ", args);
        return CommandResult.builder()
                .success(true)
                .roomMessage(player.getFullName() + " says: " + message)
                .build();
    }
}