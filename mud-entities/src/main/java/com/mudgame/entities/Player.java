package com.mudgame.entities;

import java.util.UUID;

public class Player {
    private final String id;
    private String name;
    private Room currentRoom;
    private int health;
    private int maxHealth;

    public Player(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.health = 100;
        this.maxHealth = 100;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.min(maxHealth, Math.max(0, health));
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    @Override
    public String toString() {
        return name + " (HP: " + health + "/" + maxHealth + ")";
    }
}