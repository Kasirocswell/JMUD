package com.mudgame.entities;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the combat state of an entity (Player or NPC).
 * Tracks combat status, timers, and combat-related attributes.
 */
public class CombatState {
    // Core combat status
    private boolean inCombat;
    private UUID currentTarget;
    private final Set<UUID> attackers;
    private CombatEntity owner;

    // Combat timers and cooldowns
    private long lastAttackTime;
    private long combatStartTime;
    private long lastCombatAction;
    private double attackCooldown;
    private static final long COMBAT_TIMEOUT = 10000; // 10 seconds without action exits combat

    // Combat statistics
    private final Map<UUID, Integer> damageDealt;
    private final Map<UUID, Integer> damageTaken;
    private final Map<DamageType, Long> lastDamageTime;
    private int totalDamageDealt;
    private int totalDamageTaken;
    private int criticalHits;
    private int dodges;

    // Combat flags
    private boolean autoAttack;
    private boolean canAct;
    private boolean stunned;
    private boolean fleeing;

    public CombatState(CombatEntity owner) {
        this.owner = owner;
        this.inCombat = false;
        this.attackers = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.damageDealt = new ConcurrentHashMap<>();
        this.damageTaken = new ConcurrentHashMap<>();
        this.lastDamageTime = new EnumMap<>(DamageType.class);
        this.canAct = true;
        this.attackCooldown = 2000; // Default 2 second cooldown
    }

    /**
     * Checks if entity can perform an attack based on cooldowns and status
     */
    public boolean canAttack() {
        return canAct && !stunned && !fleeing &&
                System.currentTimeMillis() - lastAttackTime >= attackCooldown;
    }

    /**
     * Updates attack cooldown based on speed attribute
     * Higher speed = lower cooldown
     * @param speed The entity's speed attribute value
     */
    public void updateAttackCooldown(int speed) {
        // Base cooldown modified by speed
        // Speed of 50 is considered "normal" speed
        double speedModifier = Math.max(0.5, Math.min(2.0, 50.0 / Math.max(1, speed)));
        this.attackCooldown = 2000 * speedModifier; // 2000ms base cooldown
    }

    /**
     * Enters combat state with a target
     */
    public void enterCombat(UUID targetId) {
        this.inCombat = true;
        this.currentTarget = targetId;
        this.combatStartTime = System.currentTimeMillis();
        this.lastCombatAction = System.currentTimeMillis();
    }

    /**
     * Exits combat state and resets combat-related values
     */
    public void exitCombat() {
        this.inCombat = false;
        this.currentTarget = null;
        this.attackers.clear();
        this.autoAttack = false;
        this.fleeing = false;
        updateCombatStatistics();
    }

    /**
     * Records damage dealt to a target
     */
    public void recordDamageDealt(UUID targetId, int amount) {
        damageDealt.merge(targetId, amount, Integer::sum);
        totalDamageDealt += amount;
        lastCombatAction = System.currentTimeMillis();
    }

    /**
     * Records damage taken from an attacker
     */
    public void recordDamageTaken(UUID attackerId, int amount) {
        damageTaken.merge(attackerId, amount, Integer::sum);
        totalDamageTaken += amount;
        lastCombatAction = System.currentTimeMillis();
    }

    /**
     * Checks if combat should timeout due to inactivity
     */
    public boolean shouldTimeoutCombat() {
        return inCombat &&
                System.currentTimeMillis() - lastCombatAction >= COMBAT_TIMEOUT;
    }

    /**
     * Adds an attacker to the combat state
     */
    public void addAttacker(UUID attackerId) {
        attackers.add(attackerId);
        lastCombatAction = System.currentTimeMillis();
    }

    /**
     * Removes an attacker from the combat state
     */
    public void removeAttacker(UUID attackerId) {
        attackers.remove(attackerId);
        if (attackers.isEmpty() && !fleeing) {
            exitCombat();
        }
    }

    /**
     * Updates final combat statistics when leaving combat
     */
    private void updateCombatStatistics() {
        // Could add more complex stat tracking here
        long combatDuration = System.currentTimeMillis() - combatStartTime;
        // Reset combat stats
        damageDealt.clear();
        damageTaken.clear();
        totalDamageDealt = 0;
        totalDamageTaken = 0;
        criticalHits = 0;
        dodges = 0;
    }

    // Getters and setters
    public boolean isInCombat() { return inCombat; }
    public UUID getCurrentTarget() { return currentTarget; }
    public Set<UUID> getAttackers() { return Collections.unmodifiableSet(attackers); }
    public boolean isAutoAttack() { return autoAttack; }
    public void setAutoAttack(boolean autoAttack) { this.autoAttack = autoAttack; }
    public boolean isStunned() { return stunned; }
    public void setStunned(boolean stunned) { this.stunned = stunned; }
    public boolean isFleeing() { return fleeing; }
    public void setFleeing(boolean fleeing) { this.fleeing = fleeing; }
    public long getLastAttackTime() { return lastAttackTime; }
    public void setLastAttackTime(long time) { this.lastAttackTime = time; }
    public double getAttackCooldown() { return attackCooldown; }
    public int getTotalDamageDealt() { return totalDamageDealt; }
    public int getTotalDamageTaken() { return totalDamageTaken; }
    public void incrementCriticalHits() { this.criticalHits++; }
    public void incrementDodges() { this.dodges++; }
    public int getCriticalHits() { return criticalHits; }
    public int getDodges() { return dodges; }
}
