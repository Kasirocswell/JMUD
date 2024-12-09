package com.mudgame.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class Item {
    // Core properties
    private final UUID id;
    private String name;
    private String description;
    private ItemRarity rarity;
    private double weight;
    private int value;
    private int levelRequired;
    private int durability;
    private int maxDurability;
    private ItemType type;
    private EquipmentSlot slot;
    private boolean stackable;
    private int maxStackSize;
    private int currentStackSize;

    // Constructor for new items
    public Item(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("rarity") ItemRarity rarity,
            @JsonProperty("type") ItemType type,
            @JsonProperty("weight") double weight,
            @JsonProperty("value") int value,
            @JsonProperty("levelRequired") int levelRequired,
            @JsonProperty("maxDurability") int maxDurability,
            @JsonProperty("slot") EquipmentSlot slot,
            @JsonProperty("stackable") boolean stackable,
            @JsonProperty("maxStackSize") int maxStackSize) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.type = type;
        this.weight = weight;
        this.value = value;
        this.levelRequired = levelRequired;
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
        this.slot = slot;
        this.stackable = stackable;
        this.maxStackSize = stackable ? maxStackSize : 1;
        this.currentStackSize = 1;
    }

    // Constructor for loading existing items
    public Item(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("rarity") ItemRarity rarity,
            @JsonProperty("type") ItemType type,
            @JsonProperty("weight") double weight,
            @JsonProperty("value") int value,
            @JsonProperty("levelRequired") int levelRequired,
            @JsonProperty("durability") int durability,
            @JsonProperty("maxDurability") int maxDurability,
            @JsonProperty("slot") EquipmentSlot slot,
            @JsonProperty("stackable") boolean stackable,
            @JsonProperty("maxStackSize") int maxStackSize,
            @JsonProperty("currentStackSize") int currentStackSize) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.type = type;
        this.weight = weight;
        this.value = value;
        this.levelRequired = levelRequired;
        this.durability = durability;
        this.maxDurability = maxDurability;
        this.slot = slot;
        this.stackable = stackable;
        this.maxStackSize = maxStackSize;
        this.currentStackSize = currentStackSize;
    }

    // Basic getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemRarity getRarity() { return rarity; }
    public ItemType getType() { return type; }
    public double getWeight() { return weight; }
    public int getValue() { return value; }
    public int getLevelRequired() { return levelRequired; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public EquipmentSlot getSlot() { return slot; }
    public boolean isStackable() { return stackable; }
    public int getMaxStackSize() { return maxStackSize; }
    public int getCurrentStackSize() { return currentStackSize; }

    // Setters for mutable properties
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setValue(int value) { this.value = value; }
    public void setWeight(double weight) { this.weight = weight; }

    // Stack management
    public boolean canStackWith(Item other) {
        return this.stackable && other.stackable &&
                this.name.equals(other.name) &&
                this.currentStackSize < this.maxStackSize;
    }

    public int addToStack(int amount) {
        int spaceAvailable = maxStackSize - currentStackSize;
        int amountToAdd = Math.min(amount, spaceAvailable);
        currentStackSize += amountToAdd;
        return amount - amountToAdd; // Returns leftover amount
    }

    public int removeFromStack(int amount) {
        int amountToRemove = Math.min(amount, currentStackSize);
        currentStackSize -= amountToRemove;
        return amountToRemove;
    }

    // Durability management
    public void damage(int amount) {
        this.durability = Math.max(0, this.durability - amount);
    }

    public void repair(int amount) {
        this.durability = Math.min(maxDurability, this.durability + amount);
    }

    @JsonIgnore
    public boolean isBroken() {
        return this.durability <= 0;
    }

    // Item state checks
    @JsonIgnore
    public boolean isEquippable() {
        return slot != null;
    }

    @JsonIgnore
    public double getTotalWeight() {
        return weight * currentStackSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (stackable && currentStackSize > 1) {
            sb.append(" (x").append(currentStackSize).append(")");
        }
        if (durability < maxDurability) {
            sb.append(" [").append(durability).append("/").append(maxDurability).append("]");
        }
        return sb.toString();
    }

    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(rarity.getColor()).append(name).append("\n");
        sb.append(description).append("\n");
        if (levelRequired > 1) {
            sb.append("Required Level: ").append(levelRequired).append("\n");
        }
        sb.append("Value: ").append(value).append(" credits\n");
        sb.append("Weight: ").append(weight).append(" kg\n");
        if (durability < maxDurability) {
            sb.append("Durability: ").append(durability).append("/").append(maxDurability).append("\n");
        }
        return sb.toString();
    }

    public String getDetailedDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-25s", name));
        if (stackable && currentStackSize > 1) {
            sb.append(String.format(" (x%d)", currentStackSize));
        }
        sb.append(String.format(" | Lvl %-2d | %4d cr | %.1f kg",
                levelRequired, value, weight));
        if (durability < maxDurability) {
            sb.append(String.format(" | %d%%", (durability * 100) / maxDurability));
        }
        return sb.toString();
    }
}

