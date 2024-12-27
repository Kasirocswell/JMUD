package com.mudgame.api.commands;

import com.mudgame.entities.*;
import java.util.*;

public interface CombatCommand extends GameCommand {
    @Override
    default String getName() {
        return "combat";
    }

    @Override
    default String getHelp() {
        return "combat - Show your current combat status and statistics";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        CombatState combat = player.getCombatState();
        StringBuilder status = new StringBuilder();

        status.append("=== Combat Status ===\n");

        if (!combat.isInCombat()) {
            status.append("You are not in combat.\n");
            if (combat.getTotalDamageDealt() > 0 || combat.getTotalDamageTaken() > 0) {
                status.append("\nLast Combat Statistics:\n");
                appendCombatStats(status, combat);
            }
            return CommandResult.success(status.toString());
        }

        // Current combat status
        status.append("Status: In Combat\n");
        status.append(String.format("Health: %d/%d\n", player.getHealth(), player.getMaxHealth()));

        // Auto-attack status
        status.append(String.format("Auto-Attack: %s\n",
                combat.isAutoAttack() ? "Enabled" : "Disabled"));

        // Current target info
        if (combat.getCurrentTarget() != null) {
            Room room = player.getCurrentRoom();
            if (room != null) {
                // Look for target in room
                Optional<NPC> target = room.getNPCs().stream()
                        .filter(npc -> npc.getId().equals(combat.getCurrentTarget()))
                        .findFirst();

                if (target.isPresent()) {
                    NPC targetNPC = target.get();
                    status.append(String.format("\nCurrent Target: %s\n", targetNPC.getName()));
                    status.append(String.format("Target Health: %d/%d\n",
                            targetNPC.getHealth(), targetNPC.getMaxHealth()));
                }
            }
        }

        // Show current attackers
        Set<UUID> attackers = combat.getAttackers();
        if (!attackers.isEmpty()) {
            status.append("\nCurrently attacked by:\n");
            Room room = player.getCurrentRoom();
            if (room != null) {
                attackers.forEach(id -> {
                    Optional<NPC> attacker = room.getNPCs().stream()
                            .filter(npc -> npc.getId().equals(id))
                            .findFirst();
                    attacker.ifPresent(npc ->
                            status.append(String.format("- %s\n", npc.getName())));
                });
            }
        }

        // Combat statistics
        status.append("\nCurrent Combat Statistics:\n");
        appendCombatStats(status, combat);

        // Combat controls reminder
        status.append("\nCombat Commands:\n");
        status.append("- attack <target> - Attack a specific target\n");
        status.append("- flee - Attempt to escape combat\n");
        status.append("- autoattack - Toggle automatic attacks\n");

        return CommandResult.success(status.toString());
    }

    private void appendCombatStats(StringBuilder sb, CombatState combat) {
        sb.append(String.format("Damage Dealt: %d\n", combat.getTotalDamageDealt()));
        sb.append(String.format("Damage Taken: %d\n", combat.getTotalDamageTaken()));
        sb.append(String.format("Critical Hits: %d\n", combat.getCriticalHits()));
        sb.append(String.format("Dodges: %d\n", combat.getDodges()));
    }
}