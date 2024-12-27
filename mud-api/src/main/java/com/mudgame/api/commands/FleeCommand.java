package com.mudgame.api.commands;

import com.mudgame.entities.*;
import java.util.*;

public interface FleeCommand extends GameCommand {
    @Override
    default String getName() {
        return "flee";
    }

    @Override
    default String getHelp() {
        return "flee - Attempt to escape from combat. Success chance based on your speed.";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (!player.getCombatState().isInCombat()) {
            return CommandResult.failure("You are not in combat.");
        }

        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return CommandResult.failure("You are not in any room!");
        }

        // Get available exits
        Map<Direction, Room> exits = currentRoom.getExits();
        if (exits.isEmpty()) {
            return CommandResult.failure("There's nowhere to flee to!");
        }

        // Calculate flee chance based on speed
        int speed = player.getAttributes().getOrDefault(Attributes.SPEED, 10);
        double fleeChance = Math.min(0.8, 0.4 + (speed * 0.02)); // 40% base + 2% per speed, max 80%

        if (Math.random() < fleeChance) {
            // Pick a random exit
            List<Direction> directions = new ArrayList<>(exits.keySet());
            Direction fleeDirection = directions.get(new Random().nextInt(directions.size()));
            Room destination = exits.get(fleeDirection);

            // Move player
            currentRoom.removePlayer(player);
            destination.addPlayer(player);
            player.setCurrentRoom(destination);

            // Exit combat
            player.getCombatState().setFleeing(true);
            player.getCombatState().exitCombat();

            return CommandResult.builder()
                    .success(true)
                    .privateMessage("You successfully flee to " + destination.getName() + "!")
                    .roomMessage(player.getFullName() + " flees to the " + fleeDirection.name().toLowerCase() + "!")
                    .build();
        }

        return CommandResult.builder()
                .success(false)
                .privateMessage("You fail to escape!")
                .roomMessage(player.getFullName() + " tries to flee but fails!")
                .build();
    }
}