package com.mudgame.entities;

import java.util.*;

public abstract class NPC {
    private final UUID id;
    private String name;
    private String description;
    private NPCType type;
    public int level;
    private int health;
    private int maxHealth;
    private Room currentRoom;
    private boolean isHostile;
    private NPCState state;
    private Inventory inventory;
    private Map<String, NPCResponse> responses = new HashMap<>();

    public NPC(String name, String description, NPCType type, int level, int maxHealth, boolean isHostile) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.type = type;
        this.level = level;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.isHostile = isHostile;
        this.state = NPCState.IDLE;
        this.responses = new HashMap<>();
        this.inventory = new Inventory(100.0, 50); // NPCs get larger inventory
    }

    // Core getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public NPCType getType() { return type; }
    public int getLevel() { return level; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public Room getCurrentRoom() { return currentRoom; }
    public boolean isHostile() { return isHostile; }
    public NPCState getState() { return state; }
    public Inventory getInventory() { return inventory; }

    // Core setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLevel(int level) { this.level = level; }
    public void setHealth(int health) {
        this.health = Math.min(maxHealth, Math.max(0, health));
        if (this.health <= 0) {
            state = NPCState.DEAD;
        }
    }
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (health > maxHealth) {
            health = maxHealth;
        }
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
    public void setHostile(boolean hostile) { this.isHostile = hostile; }
    public void setState(NPCState state) { this.state = state; }

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

    // Core gameplay methods
    public void heal(int amount) {
        setHealth(health + amount);
    }

    public void damage(int amount) {
        setHealth(health - amount);
    }

    public boolean isDead() {
        return health <= 0;
    }

    // Abstract methods that specific NPC types must implement
    public abstract void onTick(); // Called each game tick
    public abstract String interact(Player player, String action);
    public abstract void onDeath(Player killer);

    @Override
    public String toString() {
        return String.format("%s [Level %d %s] (%d/%d HP) [%s]",
                name, level, type, health, maxHealth, state);
    }

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