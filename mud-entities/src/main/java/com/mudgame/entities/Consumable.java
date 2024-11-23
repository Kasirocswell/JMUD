package com.mudgame.entities;

import static com.mudgame.entities.DamageType.ENERGY;

public class Consumable extends Item {
    private final ConsumableType consumableType;
    private final int effectPower;
    private final int effectDuration;
    private final String effectDescription;
    private int usesRemaining;

    public Consumable(String name, String description, ItemRarity rarity,
                      double weight, int value, int levelRequired,
                      ConsumableType consumableType, int effectPower,
                      int effectDuration, String effectDescription,
                      boolean stackable, int maxStackSize) {
        super(name, description, rarity, ItemType.CONSUMABLE, weight, value,
                levelRequired, 1, null, stackable, maxStackSize);
        this.consumableType = consumableType;
        this.effectPower = effectPower;
        this.effectDuration = effectDuration;
        this.effectDescription = effectDescription;
        this.usesRemaining = 1;
    }

    public ConsumableType getConsumableType() { return consumableType; }
    public int getEffectPower() { return effectPower; }
    public int getEffectDuration() { return effectDuration; }
    public String getEffectDescription() { return effectDescription; }
    public int getUsesRemaining() { return usesRemaining; }

    public boolean use(Player player) {
        if (usesRemaining <= 0) return false;

        // Apply effect based on consumable type
        switch (consumableType) {
            case HEALING:
                player.heal(effectPower);
                break;
            case ENERGY:
                player.restoreEnergy(effectPower);
                break;
            // Add more effect types as needed
        }

        usesRemaining--;
        return true;
    }

    @Override
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder(super.getDetailedDescription());
        sb.append(String.format("Type: %s\n", consumableType));
        sb.append(String.format("Effect: %s\n", effectDescription));
        if (effectPower > 0) {
            sb.append(String.format("Power: %d\n", effectPower));
        }
        if (effectDuration > 0) {
            sb.append(String.format("Duration: %d seconds\n", effectDuration));
        }
        return sb.toString();
    }
}

