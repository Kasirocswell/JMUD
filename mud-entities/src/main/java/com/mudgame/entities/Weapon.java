package com.mudgame.entities;

// Weapon class for weapons
public class Weapon extends Item {
    private final int minDamage;
    private final int maxDamage;
    private final double attackSpeed;
    private final WeaponType weaponType;
    private final DamageType damageType;

    public Weapon(String name, String description, ItemRarity rarity,
                  double weight, int value, int levelRequired,
                  int maxDurability, EquipmentSlot slot,
                  int minDamage, int maxDamage, double attackSpeed,
                  WeaponType weaponType, DamageType damageType) {
        super(name, description, rarity, ItemType.WEAPON, weight, value,
                levelRequired, maxDurability, slot, false, 1);
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.attackSpeed = attackSpeed;
        this.weaponType = weaponType;
        this.damageType = damageType;
    }

    public int getMinDamage() { return minDamage; }
    public int getMaxDamage() { return maxDamage; }
    public double getAttackSpeed() { return attackSpeed; }
    public WeaponType getWeaponType() { return weaponType; }
    public DamageType getDamageType() { return damageType; }

    @Override
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder(super.getDetailedDescription());
        sb.append(String.format("Damage: %d-%d %s\n", minDamage, maxDamage, damageType));
        sb.append(String.format("Attack Speed: %.1f\n", attackSpeed));
        sb.append(String.format("Weapon Type: %s\n", weaponType));
        return sb.toString();
    }
}