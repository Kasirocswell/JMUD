package com.mudgame.entities;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class Equipment {
    private final Map<EquipmentSlot, Item> equipped;
    private Player owner;

    public Equipment(Player owner) {
        this.owner = owner;
        this.equipped = new EnumMap<>(EquipmentSlot.class);
    }

    public Optional<Item> getEquippedItem(EquipmentSlot slot) {
        return Optional.ofNullable(equipped.get(slot));
    }

    public Map<EquipmentSlot, Item> getEquippedItems() {
        return Collections.unmodifiableMap(equipped);
    }

    public InventoryResult equipItem(Item item) {
        if (!item.isEquippable()) {
            return InventoryResult.failure("This item cannot be equipped");
        }

        if (item.getLevelRequired() > owner.getLevel()) {
            return InventoryResult.failure(
                    "You don't meet the level requirement for this item");
        }

        EquipmentSlot slot = item.getSlot();
        if (equipped.containsKey(slot)) {
            return InventoryResult.failure(
                    "You already have an item equipped in that slot");
        }

        equipped.put(slot, item);
        return InventoryResult.success("Item equipped successfully");
    }

    public InventoryResult unequipItem(EquipmentSlot slot) {
        if (!equipped.containsKey(slot)) {
            return InventoryResult.failure("No item equipped in that slot");
        }

        equipped.remove(slot);
        return InventoryResult.success("Item unequipped successfully");
    }

    public String getEquipmentDisplay() {
        StringBuilder sb = new StringBuilder("Equipment:\n");

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            sb.append(String.format("%-10s: ", slot.toString()));
            Optional<Item> equippedItem = getEquippedItem(slot);
            if (equippedItem.isPresent()) {
                sb.append(equippedItem.get().getDetailedDisplay());
            } else {
                sb.append("Empty");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
