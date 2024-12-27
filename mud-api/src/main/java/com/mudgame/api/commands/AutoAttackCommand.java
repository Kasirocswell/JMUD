package com.mudgame.api.commands;

import com.mudgame.entities.*;

public interface AutoAttackCommand extends GameCommand {
    @Override
    default String getName() {
        return "autoattack";
    }

    @Override
    default String getHelp() {
        return "autoattack - Toggle automatic attacks in combat";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        CombatState combat = player.getCombatState();

        if (!combat.isInCombat()) {
            return CommandResult.failure("You are not in combat.");
        }

        boolean newState = !combat.isAutoAttack();
        combat.setAutoAttack(newState);

        return CommandResult.success(
                newState ? "Auto-attack enabled." : "Auto-attack disabled."
        );
    }
}