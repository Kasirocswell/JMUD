package com.mudgame.api.commands;

import com.mudgame.entities.Player;

public interface GameCommand {
    CommandResult execute(Player player, String[] args);
    String getHelp();
    String getName();
}