package com.mudgame.entities;

public class InventoryItem {
    private final Item item;
    private int currentStackSize;

    public InventoryItem(Item item) {
        this.item = item;
        this.currentStackSize = item.getCurrentStackSize();
    }

    public Item getItem() { return item; }
    public int getCurrentStackSize() { return currentStackSize; }

    public boolean canStackWith(Item other) {
        return item.canStackWith(other);
    }

    public int addToStack(int amount) {
        int maxAddable = item.getMaxStackSize() - currentStackSize;
        int toAdd = Math.min(amount, maxAddable);
        currentStackSize += toAdd;
        return amount - toAdd; // Return leftover amount
    }

    public int removeFromStack(int amount) {
        int toRemove = Math.min(amount, currentStackSize);
        currentStackSize -= toRemove;
        return toRemove;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getName());
        if (item.isStackable() && currentStackSize > 1) {
            sb.append(String.format(" (x%d)", currentStackSize));
        }
        if (item.getDurability() < item.getMaxDurability()) {
            sb.append(String.format(" [%d%%]",
                    (item.getDurability() * 100) / item.getMaxDurability()));
        }
        return sb.toString();
    }
}
