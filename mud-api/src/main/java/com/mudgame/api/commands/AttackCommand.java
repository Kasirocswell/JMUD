package com.mudgame.api.commands;

import com.mudgame.entities.*;
import java.util.Optional;

public interface AttackCommand extends GameCommand {
    @Override
    default String getName() {
        return "attack";
    }

    @Override
    default String getHelp() {
        return "attack <target> - Attack a target. Can also use 'kill' as an alias.";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("Who do you want to attack?");
        }

        // Check if player can attack
        if (!player.getCombatState().canAttack()) {
            return CommandResult.failure("You can't attack yet.");
        }

        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return CommandResult.failure("You are not in any room!");
        }

        // Get target name from args
        String targetName = String.join(" ", args).toLowerCase();

        // First check for NPCs
        Optional<NPC> targetNPC = currentRoom.getNPCs().stream()
                .filter(npc -> npc.getName().toLowerCase().contains(targetName))
                .findFirst();

        if (targetNPC.isPresent()) {
            NPC target = targetNPC.get();

            // Check if target can be attacked
            if (target.isDead()) {
                return CommandResult.failure(target.getName() + " is already dead.");
            }

            if (target.getType() != NPCType.ENEMY) {
                return CommandResult.failure("You cannot attack " + target.getName() + ".");
            }

            // Get combat state before the attack
            CombatState combatState = player.getCombatState();
            boolean wasInCombat = combatState.isInCombat();

            // Attempt the attack
            boolean success = player.attack(target);
            if (!success) {
                return CommandResult.failure("You failed to attack " + target.getName() + ".");
            }

            // If this is our first attack (entering combat), enable auto-attack
            if (!wasInCombat) {
                System.out.println("Enabling auto-attack for: " + player.getFullName()); // Debug log
                combatState.setAutoAttack(true);
            }

            return CommandResult.builder()
                    .success(true)
                    .privateMessage("You attack " + target.getName() + "!")
                    .roomMessage(player.getFullName() + " attacks " + target.getName() + "!")
                    .build();
        }

        // Then check for other players if PvP is enabled
        Optional<Player> targetPlayer = currentRoom.getPlayers().stream()
                .filter(p -> p != player && p.getFullName().toLowerCase().contains(targetName))
                .findFirst();

        if (targetPlayer.isPresent()) {
            return CommandResult.failure("PvP combat is not enabled."); // We can modify this later if adding PvP
        }

        return CommandResult.failure("You don't see them here.");
    }
}