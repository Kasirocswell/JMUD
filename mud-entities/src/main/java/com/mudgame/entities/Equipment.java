package com.mudgame.entities;

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
            sb.append(String.format("%s: %s\n",
                    slot.toString(),
                    equipped.containsKey(slot)
                            ? equipped.get(slot).getName()
                            : "Empty"));
        }

        return sb.toString();
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
