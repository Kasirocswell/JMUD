package com.mudgame.api.commands;

import com.mudgame.entities.NPC;
import com.mudgame.entities.NPCType;
import com.mudgame.entities.Player;
import com.mudgame.entities.Room;
import com.mudgame.entities.merchants.MerchantNPC;

import java.util.Optional;

public interface TalkCommand extends GameCommand {
    @Override
    default String getName() {
        return "talk";
    }

    @Override
    default String getHelp() {
        return "talk <npc> [action] - Talk to an NPC. For merchants: talk <merchant> greet/list/buy <item>/farewell";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("Who do you want to talk to?");
        }

        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            return CommandResult.failure("You are not in any room!");
        }

        // Get NPC name from first argument
        String npcName = args[0].toLowerCase();
        Optional<NPC> npc = currentRoom.getNPCs().stream()
                .filter(n -> n.getName().toLowerCase().contains(npcName))
                .findFirst();

        if (npc.isEmpty()) {
            return CommandResult.failure("You don't see them here.");
        }

        // Get the action (if provided)
        String action = args.length > 1 ?
                String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) :
                "greet";

        // Handle merchant-specific interactions
        if (npc.get().getType() == NPCType.MERCHANT) {
            MerchantNPC merchant = (MerchantNPC) npc.get();
            String response = merchant.handleTrade(player, action);
            return CommandResult.success(response);
        }

        // For non-merchant NPCs, use standard interact
        String response = npc.get().interact(player, action);
        return CommandResult.success(response);
    }
}