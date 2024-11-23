package com.mudgame.entities;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Armor extends Item {
    private final int defense;
    private final int energyShield;
    private final ArmorType armorType;
    private final Map<DamageType, Integer> resistances;

    public Armor(String name, String description, ItemRarity rarity,
                 double weight, int value, int levelRequired,
                 int maxDurability, EquipmentSlot slot,
                 int defense, int energyShield, ArmorType armorType,
                 Map<DamageType, Integer> resistances) {
        super(name, description, rarity, ItemType.ARMOR, weight, value,
                levelRequired, maxDurability, slot, false, 1);
        this.defense = defense;
        this.energyShield = energyShield;
        this.armorType = armorType;
        this.resistances = new EnumMap<>(DamageType.class);
        if (resistances != null) {
            this.resistances.putAll(resistances);
        }
    }

    public int getDefense() { return defense; }
    public int getEnergyShield() { return energyShield; }
    public ArmorType getArmorType() { return armorType; }
    public Map<DamageType, Integer> getResistances() {
        return Collections.unmodifiableMap(resistances);
    }

    @Override
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder(super.getDetailedDescription());
        sb.append(String.format("Defense: %d\n", defense));
        if (energyShield > 0) {
            sb.append(String.format("Energy Shield: %d\n", energyShield));
        }
        sb.append(String.format("Armor Type: %s\n", armorType));
        if (!resistances.isEmpty()) {
            sb.append("Resistances:\n");
            resistances.forEach((type, value) ->
                    sb.append(String.format("  %s: %d%%\n", type, value)));
        }
        return sb.toString();
    }
}
