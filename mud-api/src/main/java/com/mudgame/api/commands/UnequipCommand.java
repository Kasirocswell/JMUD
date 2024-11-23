package com.mudgame.api.commands;

import com.mudgame.entities.EquipmentSlot;
import com.mudgame.entities.InventoryResult;
import com.mudgame.entities.Item;
import com.mudgame.entities.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

// Command to unequip items
public interface UnequipCommand extends GameCommand {
    @Override
    default String getName() {
        return "unequip";
    }

    @Override
    default String getHelp() {
        return "unequip <slot> - Unequip an item from the specified slot";
    }

    @Override
    default CommandResult execute(Player player, String[] args) {
        if (args.length < 1) {
            return CommandResult.failure("Which slot do you want to unequip from?");
        }

        try {
            EquipmentSlot slot = EquipmentSlot.valueOf(args[0].toUpperCase());
            Optional<Item> equippedItem = player.getEquipment().getEquippedItem(slot);

            if (equippedItem.isEmpty()) {
                return CommandResult.failure("Nothing is equipped in that slot.");
            }

            InventoryResult result = player.unequipItem(slot);
            if (result.isSuccess()) {
                return CommandResult.builder()
                        .success(true)
                        .privateMessage("You unequipped " + equippedItem.get().getName())
                        .roomMessage(player.getFullName() + " unequips " + equippedItem.get().getName())
                        .build();
            }

            return CommandResult.failure(result.getMessage());
        } catch (IllegalArgumentException e) {
            return CommandResult.failure("Invalid equipment slot. Valid slots are: " +
                    String.join(", ", Arrays.stream(EquipmentSlot.values())
                            .map(EquipmentSlot::name)
                            .collect(Collectors.toList())));
        }
    }
}
