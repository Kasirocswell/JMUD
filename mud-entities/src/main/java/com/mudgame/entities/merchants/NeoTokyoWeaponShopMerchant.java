package com.mudgame.entities.merchants;

import com.mudgame.entities.*;
import com.mudgame.events.EventListener;

import java.util.Collections;

public class NeoTokyoWeaponShopMerchant extends MerchantNPC implements SpawnableNPC {

    public NeoTokyoWeaponShopMerchant(int level, EventListener eventListener) {
        super(
                "Takeda",
                "A stern-looking merchant wearing cybernetic augmentations and a traditional kimono. Various weapons line the walls behind him.",
                level,
                100,
                eventListener
        );
        restockInventory();
    }

    @Override
    protected String getGreeting() {
        return getName() + " bows slightly. 'Welcome to my shop. I deal in only the finest weapons.'";
    }

    @Override
    protected String getFarewell() {
        return getName() + " bows. 'Return when you seek more firepower.'";
    }

    @Override
    protected void restockInventory() {
        addWeapon("Plasma Pistol",
                "A standard-issue energy weapon with reliable performance.",
                ItemRarity.COMMON,
                10, 150, 1,
                WeaponType.PISTOL, DamageType.ENERGY,
                8, 12, 1.5);

        addWeapon("Shock Rifle",
                "A reliable energy rifle with good range and stopping power.",
                ItemRarity.UNCOMMON,
                15, 300, 2,
                WeaponType.RIFLE, DamageType.ENERGY,
                12, 18, 1.2);

        addWeapon("Nano Blade",
                "A vibrating blade infused with nanites for enhanced cutting power.",
                ItemRarity.UNCOMMON,
                8, 250, 1,
                WeaponType.SWORD, DamageType.PHYSICAL,
                10, 15, 1.8);
    }

    private void addWeapon(String name, String description, ItemRarity rarity,
                           double weight, int value, int levelRequired,
                           WeaponType type, DamageType damageType,
                           int minDamage, int maxDamage, double attackSpeed) {
        Weapon weapon = new Weapon(
                name, description, rarity,
                weight, value, levelRequired,
                100,
                EquipmentSlot.MAIN_HAND,
                minDamage, maxDamage, attackSpeed,
                type, damageType
        );
        inventory.put(weapon.getId(), weapon);
    }

    @Override
    public SpawnConfiguration getSpawnConfiguration() {
        return new SpawnConfiguration(
                "weapon_merchant",
                1,
                -1,
                Collections.singletonList("Weapon Shop"),
                1, 10,
                1.0
        );
    }

    @Override
    public NPC createInstance(int level) {
        return new NeoTokyoWeaponShopMerchant(level, eventListener);
    }

    @Override
    public String interact(Player player, String action) {
        // First check if it's a basic merchant action
        String[] parts = action.toLowerCase().split("\\s+", 2);
        String command = parts[0];

        switch (command) {
            case "greet":
            case "list":
            case "buy":
            case "farewell":
                return handleTrade(player, action);

            case "background":
                return getName() + " adjusts his cybernetic eye. 'I've been selling weapons in Neo-Tokyo " +
                        "for over twenty years. Quality and honor - these are the principles I live by.'";

            case "discount":
                return getName() + " shakes his head firmly. 'My prices are fair. The quality speaks for itself.'";

            default:
                return "Available commands: greet, list, buy <item>, farewell, background, discount";
        }
    }
}