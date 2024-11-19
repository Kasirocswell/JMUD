package com.mudgame.entities;

import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Player {
    private final String id;
    private final String ownerId;
    private String firstName;
    private String lastName;
    private Race race;
    private CharacterClass characterClass;
    private JsonNode inventory;
    private JsonNode equipment;
    private int credits;
    private String currentRoomId;
    @JsonIgnore
    private Room currentRoom;
    private int level;
    private int health;
    private int maxHealth;
    private int energy;
    private int maxEnergy;
    private boolean isOnline;
    private long lastSeen;

    // Constructor for new character creation
    public Player(
            @JsonProperty("ownerId") String ownerId,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("race") Race race,
            @JsonProperty("characterClass") CharacterClass characterClass) {
        this.id = UUID.randomUUID().toString();
        this.ownerId = ownerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.race = race;
        this.characterClass = characterClass;
        this.credits = 100;
        this.level = 1;
        this.health = 100;
        this.maxHealth = 100;
        this.energy = 100;
        this.maxEnergy = 100;
        this.isOnline = false;
        this.lastSeen = System.currentTimeMillis();
    }

    // Constructor for loading existing character
    public Player(
            @JsonProperty("id") String id,
            @JsonProperty("ownerId") String ownerId,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("race") Race race,
            @JsonProperty("characterClass") CharacterClass characterClass,
            @JsonProperty("inventory") JsonNode inventory,
            @JsonProperty("equipment") JsonNode equipment,
            @JsonProperty("credits") int credits,
            @JsonProperty("currentRoomId") String currentRoomId,
            @JsonProperty("level") int level,
            @JsonProperty("health") int health,
            @JsonProperty("maxHealth") int maxHealth,
            @JsonProperty("energy") int energy,
            @JsonProperty("maxEnergy") int maxEnergy,
            @JsonProperty("lastSeen") long lastSeen) {
        this.id = id;
        this.ownerId = ownerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.race = race;
        this.characterClass = characterClass;
        this.inventory = inventory;
        this.equipment = equipment;
        this.credits = credits;
        this.currentRoomId = currentRoomId;
        this.level = level;
        this.health = health;
        this.maxHealth = maxHealth;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.isOnline = false;
        this.lastSeen = lastSeen;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Race getRace() {
        return race;
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    public JsonNode getInventory() {
        return inventory;
    }

    public JsonNode getEquipment() {
        return equipment;
    }

    public int getCredits() {
        return credits;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    @JsonIgnore
    public Room getCurrentRoom() {
        return currentRoom;
    }

    public int getLevel() {
        return level;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public long getLastSeen() {
        return lastSeen;
    }



    // Setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setInventory(JsonNode inventory) {
        this.inventory = inventory;
    }

    public void setEquipment(JsonNode equipment) {
        this.equipment = equipment;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setCurrentRoomId(String roomId) {
        this.currentRoomId = roomId;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
        if (room != null) {
            this.currentRoomId = room.getId();
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setHealth(int health) {
        this.health = Math.min(maxHealth, Math.max(0, health));
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(maxEnergy, Math.max(0, energy));
    }

    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
        if (energy > maxEnergy) {
            energy = maxEnergy;
        }
    }

    public void setOnline(boolean online) {
        this.isOnline = online;
        if (!online) {
            this.lastSeen = System.currentTimeMillis();
        }
    }

    // Game-related methods
    public void heal(int amount) {
        setHealth(health + amount);
    }

    public void damage(int amount) {
        setHealth(health - amount);
    }

    public void restoreEnergy(int amount) {
        setEnergy(energy + amount);
    }

    public void useEnergy(int amount) {
        setEnergy(energy - amount);
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean hasEnoughEnergy(int amount) {
        return energy >= amount;
    }

    @Override
    public String toString() {
        return String.format("%s %s - Level %d %s %s (HP: %d/%d, EP: %d/%d) [%s]",
                firstName, lastName, level, race, characterClass,
                health, maxHealth, energy, maxEnergy,
                isOnline ? "Online" : "Offline");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}