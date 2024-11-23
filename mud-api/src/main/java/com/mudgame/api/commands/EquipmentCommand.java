package com.mudgame.api.commands;

import com.mudgame.entities.Player;

// Command to view equipped items
public interface EquipmentCommand extends GameCommand {
    @Override
    default String getName() {
        return "equipment";
    }

    @Override
    default String getHelp() {
        return "equipment - View your equipped items";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        return CommandResult.success(player.getEquipment().getEquipmentDisplay());
    }
}
