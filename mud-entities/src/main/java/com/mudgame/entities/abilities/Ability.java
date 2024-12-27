package com.mudgame.entities.abilities;

import com.mudgame.entities.CharacterClass;
import com.mudgame.entities.Player;

// Base class for all abilities
public abstract class Ability {
    private final String id;
    private final String name;
    private final String description;
    private final int energyCost;
    private final int cooldown; // in game ticks
    private int currentCooldown;
    private final int requiredLevel;
    private final CharacterClass requiredClass;

    public Ability(String id, String name, String description,
                   int energyCost, int cooldown, int requiredLevel,
                   CharacterClass requiredClass) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.energyCost = energyCost;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
        this.requiredLevel = requiredLevel;
        this.requiredClass = requiredClass;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getEnergyCost() { return energyCost; }
    public int getCooldown() { return cooldown; }
    public int getCurrentCooldown() { return currentCooldown; }
    public int getRequiredLevel() { return requiredLevel; }
    public CharacterClass getRequiredClass() { return requiredClass; }

    // Cooldown management
    public void tick() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    public void resetCooldown() {
        currentCooldown = cooldown;
    }

    public boolean isReady() {
        return currentCooldown == 0;
    }

    // Abstract method for ability effects
    public abstract AbilityResult use(Player user, String... args);
}

