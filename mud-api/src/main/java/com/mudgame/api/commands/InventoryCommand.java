package com.mudgame.api.commands;

import com.mudgame.entities.Player;

// Command to view inventory contents
public interface InventoryCommand extends GameCommand {
    @Override
    default String getName() {
        return "inventory";
    }

    @Override
    default String getHelp() {
        return "inventory - View your inventory contents";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        return CommandResult.success(player.getInventory().getInventoryDisplay());
    }
}

