package com.mudgame.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Inventory {
    private final UUID id;
    private final double maxWeight;
    private final int maxSlots;
    private final Map<UUID, InventoryItem> items;
    private double currentWeight;
    private int usedSlots;

    public Inventory(double maxWeight, int maxSlots) {
        this.id = UUID.randomUUID();
        this.maxWeight = maxWeight;
        this.maxSlots = maxSlots;
        this.items = new HashMap<>();
        this.currentWeight = 0;
        this.usedSlots = 0;
    }

    @JsonProperty("maxWeight")
    public double getMaxWeight() { return maxWeight; }

    @JsonProperty("maxSlots")
    public int getMaxSlots() { return maxSlots; }

    @JsonProperty("currentWeight")
    public double getCurrentWeight() { return currentWeight; }

    @JsonProperty("usedSlots")
    public int getUsedSlots() { return usedSlots; }

    @JsonIgnore
    public int getRemainingSlots() { return maxSlots - usedSlots; }

    @JsonIgnore
    public double getRemainingWeight() { return maxWeight - currentWeight; }

    public Collection<InventoryItem> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public Optional<InventoryItem> getItem(UUID itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    public InventoryResult addItem(Item item) {
        // Check if item can be stacked with existing items
        if (item.isStackable()) {
            Optional<InventoryItem> existingStack = items.values().stream()
                    .filter(invItem -> invItem.canStackWith(item))
                    .findFirst();

            if (existingStack.isPresent()) {
                return addToExistingStack(existingStack.get(), item);
            }
        }

        // If we can't stack, try to add as new item
        return addNewItem(item);
    }

    private InventoryResult addToExistingStack(InventoryItem existingStack, Item item) {
        // Check if adding would exceed weight limit
        double additionalWeight = item.getWeight() * item.getCurrentStackSize();
        if (currentWeight + additionalWeight > maxWeight) {
            return InventoryResult.failure("Adding this item would exceed weight limit");
        }

        // Try to add to stack
        int leftover = existingStack.addToStack(item.getCurrentStackSize());
        currentWeight += item.getWeight() * (item.getCurrentStackSize() - leftover);

        if (leftover > 0) {
            return InventoryResult.partial("Partially added item to existing stack",
                    item.getCurrentStackSize() - leftover);
        }
        return InventoryResult.success("Added item to existing stack");
    }

    private InventoryResult addNewItem(Item item) {
        // Check if we have space
        if (usedSlots >= maxSlots) {
            return InventoryResult.failure("No free inventory slots");
        }

        // Check weight limit
        double itemWeight = item.getTotalWeight();
        if (currentWeight + itemWeight > maxWeight) {
            return InventoryResult.failure("Adding this item would exceed weight limit");
        }

        // Add item
        InventoryItem invItem = new InventoryItem(item);
        items.put(item.getId(), invItem);
        currentWeight += itemWeight;
        usedSlots++;

        return InventoryResult.success("Added item to inventory");
    }

    public InventoryResult removeItem(UUID itemId, int amount) {
        InventoryItem item = items.get(itemId);
        if (item == null) {
            return InventoryResult.failure("Item not found in inventory");
        }

        int removed = item.removeFromStack(amount);
        currentWeight -= item.getItem().getWeight() * removed;

        // If stack is empty, remove the item entirely
        if (item.getCurrentStackSize() <= 0) {
            items.remove(itemId);
            usedSlots--;
        }

        if (removed < amount) {
            return InventoryResult.partial("Partially removed item", removed);
        }
        return InventoryResult.success("Removed item from inventory");
    }

    public String getInventoryDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Inventory (%d/%d slots, %.1f/%.1f kg):\n\n",
                usedSlots, maxSlots, currentWeight, maxWeight));

        if (items.isEmpty()) {
            sb.append("  Empty\n");
        } else {
            // Group items by type
            Map<ItemType, List<InventoryItem>> groupedItems = items.values().stream()
                    .collect(Collectors.groupingBy(item -> item.getItem().getType()));

            groupedItems.forEach((type, itemList) -> {
                sb.append(String.format("%s:\n", type));
                itemList.forEach(item ->
                        sb.append("  ").append(item.getItem().getDetailedDisplay()).append("\n")
                );
                sb.append("\n");
            });
        }

        return sb.toString();
    }
}

