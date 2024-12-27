package com.mudgame.entities;

import java.util.*;
import com.mudgame.events.EventListener;

/**
 * Base class for all entities that can engage in combat (Players and NPCs)
 */
public abstract class CombatEntity {
    protected final UUID id;
    protected CombatState combatState;
    protected Map<Attributes, Integer> attributes;
    protected String name;
    protected int level;
    protected int health;
    protected int maxHealth;

    protected CombatEntity(UUID id, String name, int level, int maxHealth) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attributes = new EnumMap<>(Attributes.class);
        this.combatState = new CombatState(this);
    }

        EventListener eventListener;

    /**
     * Initiates an attack against another entity
     */
    public boolean attack(CombatEntity target) {
        if (!canAttack(target)) {
            return false;
        }

        // Calculate base damage
        int damage = calculateDamage();

        // Apply critical hit if applicable
        boolean isCritical = calculateCriticalHit();
        if (isCritical) {
            damage *= 2;
            combatState.incrementCriticalHits();
        }

        // Check if target dodges
        if (target.attemptDodge(this)) {
            target.getCombatState().incrementDodges();
            String dodgeMessage = String.format("%s tries to attack %s, but they dodge!",
                    getName(), target.getName());
            broadcastToRoom(dodgeMessage);
            return false;
        }

        // Apply damage and record combat stats
        target.damage(damage);
        combatState.recordDamageDealt(target.getId(), damage);
        target.getCombatState().recordDamageTaken(this.getId(), damage);

        // Update combat state
        combatState.setLastAttackTime(System.currentTimeMillis());
        if (!combatState.isInCombat()) {
            combatState.enterCombat(target.getId());
        }

        // Ensure target is in combat with us
        if (!target.getCombatState().isInCombat()) {
            target.getCombatState().enterCombat(this.getId());
        }
        target.getCombatState().addAttacker(this.getId());

        // Generate detailed combat message
        String attackMessage = generateCombatMessage(target, damage, isCritical);
        String healthStatus = generateHealthStatus(target);
        broadcastToRoom(attackMessage);
        if (!healthStatus.isEmpty()) {
            broadcastToRoom(healthStatus);
        }

        return true;
    }

    protected String generateCombatMessage(CombatEntity target, int damage, boolean isCritical) {
        StringBuilder msg = new StringBuilder();
        msg.append(getName()).append(" ");

        if (this instanceof Player) {
            msg.append(isCritical ? "critically hits " : "hits ");
            msg.append(target.getName());
        } else {
            String[] attackVerbs = {
                    "strikes",
                    "attacks",
                    "hits"
            };
            msg.append(attackVerbs[new Random().nextInt(attackVerbs.length)]).append(" ");
            msg.append(target.getName());
        }

        msg.append(String.format(" for %d%s damage!",
                damage,
                isCritical ? " CRITICAL" : ""));

        return msg.toString();
    }

    private String generateHealthStatus(CombatEntity target) {
        int healthPercent = (target.getHealth() * 100) / target.getMaxHealth();

        if (healthPercent <= 25) {
            return String.format("%s is critically wounded! (%d/%d HP)",
                    target.getName(), target.getHealth(), target.getMaxHealth());
        } else if (healthPercent <= 50) {
            return String.format("%s is badly hurt. (%d/%d HP)",
                    target.getName(), target.getHealth(), target.getMaxHealth());
        }

        return ""; // Only show status for significant health loss
    }

    protected void broadcastToRoom(String message) {
        Room currentRoom = getCurrentRoom();
        if (currentRoom != null && eventListener != null) {
            eventListener.onEvent("room", currentRoom.getName(), message);
        }
    }

    /**
     * Checks if this entity can attack a target
     */
    protected boolean canAttack(CombatEntity target) {
        return target != null &&
                target != this &&
                !target.isDead() &&
                !this.isDead() &&
                combatState.canAttack() &&
                isInRange(target) &&
                hasLineOfSight(target);
    }

    /**
     * Calculates base damage for an attack
     */
    public int calculateDamage() {
        int strength = attributes.getOrDefault(Attributes.STRENGTH, 10);
        int baseDamage = strength / 2;

        // Add weapon damage if equipped
        Optional<Item> weapon = getEquippedWeapon();
        if (weapon.isPresent() && weapon.get() instanceof Weapon) {
            Weapon w = (Weapon) weapon.get();
            baseDamage += randomizeDamage(w.getMinDamage(), w.getMaxDamage());
        }

        return Math.max(1, baseDamage); // Minimum 1 damage
    }

    /**
     * Checks for critical hit based on luck and other factors
     */
    protected boolean calculateCriticalHit() {
        int luck = attributes.getOrDefault(Attributes.LUCK, 10);
        double critChance = luck * 0.5; // 0.5% per luck point
        return new Random().nextDouble() * 100 <= critChance;
    }

    /**
     * Attempts to dodge an incoming attack
     */
    protected boolean attemptDodge(CombatEntity attacker) {
        int agility = attributes.getOrDefault(Attributes.AGILITY, 10);
        double dodgeChance = agility * 0.3; // 0.3% per agility point
        return new Random().nextDouble() * 100 <= dodgeChance;
    }

    /**
     * Handles receiving damage
     */
    public void damage(int amount) {
        // Calculate damage reduction from armor
        int damageReduction = calculateDamageReduction();
        int finalDamage = Math.max(1, amount - damageReduction);

        this.health = Math.max(0, this.health - finalDamage);

        // Check for death
        if (this.health <= 0) {
            onDeath();
        }
    }

    /**
     * Calculates damage reduction from armor and other sources
     */
    protected int calculateDamageReduction() {
        int reduction = 0;

        // Add base defense from constitution
        int constitution = attributes.getOrDefault(Attributes.CONSTITUTION, 10);
        reduction += constitution / 5;

        // Add armor defense
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Optional<Item> item = getEquippedItem(slot);
            if (item.isPresent() && item.get() instanceof Armor) {
                Armor armor = (Armor) item.get();
                reduction += armor.getDefense();
            }
        }

        return reduction;
    }

    /**
     * Checks if target is in range for attack
     */
    protected boolean isInRange(CombatEntity target) {
        // For now, assume everything in same room is in range
        return getCurrentRoom() == target.getCurrentRoom();
    }

    /**
     * Checks if there's line of sight to target
     */
    protected boolean hasLineOfSight(CombatEntity target) {
        // For now, assume everything in same room has line of sight
        return getCurrentRoom() == target.getCurrentRoom();
    }

    /**
     * Called when entity dies
     */
    protected abstract void onDeath();

    /**
     * Gets the currently equipped weapon
     */
    protected abstract Optional<Item> getEquippedWeapon();

    /**
     * Gets item equipped in specified slot
     */
    protected abstract Optional<Item> getEquippedItem(EquipmentSlot slot);

    /**
     * Gets the current room
     */
    public abstract Room getCurrentRoom();

    protected int randomizeDamage(int min, int max) {
        return min + new Random().nextInt(max - min + 1);
    }

    // Getters and setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isDead() { return health <= 0; }
    public CombatState getCombatState() { return combatState; }
    public Map<Attributes, Integer> getAttributes() { return Collections.unmodifiableMap(attributes); }

    public void setHealth(int health) {
        this.health = Math.min(maxHealth, Math.max(0, health));
    }

    public void heal(int amount) {
        this.health = Math.min(maxHealth, this.health + amount);
    }
}