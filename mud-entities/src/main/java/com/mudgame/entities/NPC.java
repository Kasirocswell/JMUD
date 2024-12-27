package com.mudgame.entities;

import java.util.*;
import com.mudgame.events.EventListener;

public abstract class NPC extends CombatEntity {
    private String description;
    private final NPCType type;
    private Room currentRoom;
    private boolean isHostile;
    private NPCState state;
    private Inventory inventory;
    private Map<String, NPCResponse> responses;
    protected final EventListener eventListener;

    public NPC(String name, String description, NPCType type, int level,
               int maxHealth, boolean isHostile, EventListener eventListener) {
        super(UUID.randomUUID(), name, level, maxHealth);
        this.description = description;
        this.type = type;
        this.isHostile = isHostile;
        this.state = NPCState.IDLE;
        this.inventory = new Inventory(100.0, 50); // NPCs get larger inventory
        this.responses = new HashMap<>();
        this.eventListener = eventListener;

        initializeAttributes();
    }

    private void initializeAttributes() {
        // Set base attributes based on NPC type and level
        setBaseAttributes();
        // Apply type-specific bonuses
        applyTypeAttributes();
        // Scale attributes with level
        scaleAttributesWithLevel();
        // Update combat stats
        updateCombatStats();
    }

    private void setBaseAttributes() {
        for (Attributes attr : Attributes.values()) {
            attributes.put(attr, 8); // NPCs start with slightly lower base stats
        }
    }

    private void applyTypeAttributes() {
        switch (type) {
            case ENEMY:
                // Enemies get combat-focused attributes
                attributes.merge(Attributes.STRENGTH, 2, Integer::sum);
                attributes.merge(Attributes.CONSTITUTION, 2, Integer::sum);
                attributes.merge(Attributes.SPEED, 1, Integer::sum);
                break;
            case MERCHANT:
                // Merchants get social attributes
                attributes.merge(Attributes.CHARISMA, 4, Integer::sum);
                attributes.merge(Attributes.INTELLIGENCE, 2, Integer::sum);
                break;
            case QUEST:
                // Quest NPCs get balanced attributes
                attributes.merge(Attributes.WISDOM, 3, Integer::sum);
                attributes.merge(Attributes.CHARISMA, 2, Integer::sum);
                break;
            case AMBIENT:
                // Ambient NPCs get minimal attribute bonuses
                attributes.merge(Attributes.PERCEPTION, 2, Integer::sum);
                break;
        }
    }

    private void scaleAttributesWithLevel() {
        // Scale primary attributes based on level
        int levelBonus = (level - 1) / 2; // +1 to primary attributes every 2 levels
        if (levelBonus > 0) {
            switch (type) {
                case ENEMY:
                    attributes.merge(Attributes.STRENGTH, levelBonus, Integer::sum);
                    attributes.merge(Attributes.CONSTITUTION, levelBonus, Integer::sum);
                    break;
                case MERCHANT:
                    attributes.merge(Attributes.CHARISMA, levelBonus, Integer::sum);
                    attributes.merge(Attributes.INTELLIGENCE, levelBonus, Integer::sum);
                    break;
                // Add scaling for other types as needed
            }
        }
    }

    private void updateCombatStats() {
        // Update attack speed based on Speed attribute
        combatState.updateAttackCooldown(attributes.get(Attributes.SPEED));

        // Update max health based on Constitution and level
        int constitutionBonus = (attributes.get(Attributes.CONSTITUTION) - 10) * 5;
        int levelBonus = level * 8;  // NPCs get slightly less health per level than players
        this.maxHealth = 80 + constitutionBonus + levelBonus; // NPCs start with lower base health
        this.health = Math.min(health, maxHealth);
    }

    @Override
    protected void onDeath() {
        state = NPCState.DEAD;
        combatState.exitCombat();

        // Get the killer if they're still in combat with us
        Optional<UUID> killerId = combatState.getAttackers().stream().findFirst();
        if (killerId.isPresent() && currentRoom != null) {
            // Find the killer in the room
            Optional<Player> killer = currentRoom.getPlayers().stream()
                    .filter(p -> p.getId().equals(killerId.get()))
                    .findFirst();

            if (killer.isPresent()) {
                onDeath(killer.get());
            }
        }

        // Broadcast death message
        if (currentRoom != null && eventListener != null) {
            String deathMessage = String.format("%s has been defeated!", getName());
            eventListener.onEvent("room", currentRoom.getName(), deathMessage);
        }
    }

    @Override
    protected Optional<Item> getEquippedWeapon() {
        // NPCs don't use the equipment system - they have innate weapons
        return Optional.empty();
    }

    @Override
    protected Optional<Item> getEquippedItem(EquipmentSlot slot) {
        // NPCs don't use the equipment system
        return Optional.empty();
    }

    @Override
    public int calculateDamage() {
        // NPCs use a simplified damage calculation based on level and strength
        int strength = attributes.getOrDefault(Attributes.STRENGTH, 10);
        int baseDamage = (strength / 2) + (level * 2);

        // Add randomness
        int variance = Math.max(1, baseDamage / 4);
        return baseDamage - variance + new Random().nextInt(variance * 2 + 1);
    }

    @Override
    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        if (this.currentRoom != null) {
            this.currentRoom.removeNPC(this);
        }
        this.currentRoom = room;
        if (room != null) {
            room.addNPC(this);
        }
    }

    // Basic interaction methods
    public String getResponse(String trigger) {
        NPCResponse response = responses.getOrDefault(trigger.toLowerCase(),
                () -> "The " + name + " doesn't seem interested in that.");
        return response.getMessage();
    }

    public void addResponse(String trigger, NPCResponse dynamicResponse) {
        responses.put(trigger.toLowerCase(), dynamicResponse);
    }

    public void addResponse(String trigger, String staticResponse) {
        responses.put(trigger.toLowerCase(), () -> staticResponse);
    }

    // Getters and setters
    public String getDescription() { return description; }
    public NPCType getType() { return type; }
    public boolean isHostile() { return isHostile; }
    public NPCState getState() { return state; }
    public Inventory getInventory() { return inventory; }

    public void setDescription(String description) { this.description = description; }
    public void setHostile(boolean hostile) { this.isHostile = hostile; }
    public void setState(NPCState state) { this.state = state; }

    // Abstract methods that specific NPC types must implement
    public abstract void onTick(); // Called each game tick
    public abstract String interact(Player player, String action);
    public abstract void onDeath(Player killer);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NPC npc = (NPC) o;
        return id.equals(npc.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}