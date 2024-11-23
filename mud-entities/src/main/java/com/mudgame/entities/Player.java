package com.mudgame.entities;

import java.util.Optional;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Player {
    private final UUID id;
    private final UUID ownerId;
    private String firstName;
    private String lastName;
    private Race race;
    private CharacterClass characterClass;
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
    private Inventory inventory;
    private Equipment equipment;

    // Constructor for new character creation
    public Player(
            @JsonProperty("ownerId") UUID ownerId,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("race") Race race,
            @JsonProperty("characterClass") CharacterClass characterClass,
            Inventory inventory,
            Equipment equipment) {
        this.id = UUID.randomUUID();
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
        this.inventory = inventory;  // Use the passed in inventory
        this.equipment = equipment;  // Use the passed in equipment
    }

    // Constructor for loading existing character
    // Constructor for loading existing character
    public Player(
            @JsonProperty("id") UUID id,  // Accept the ID parameter
            @JsonProperty("ownerId") UUID ownerId,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("race") Race race,
            @JsonProperty("characterClass") CharacterClass characterClass,
            @JsonProperty("inventory") Inventory inventory,
            @JsonProperty("equipment") Equipment equipment,
            @JsonProperty("credits") int credits,
            @JsonProperty("currentRoomId") String currentRoomId,
            @JsonProperty("level") int level,
            @JsonProperty("health") int health,
            @JsonProperty("maxHealth") int maxHealth,
            @JsonProperty("energy") int energy,
            @JsonProperty("maxEnergy") int maxEnergy,
            @JsonProperty("lastSeen") long lastSeen) {
        this.id = id;  // Use the provided ID
        this.ownerId = ownerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.race = race;
        this.characterClass = characterClass;
        this.credits = credits;
        this.currentRoomId = currentRoomId;
        this.level = level;
        this.health = health;
        this.maxHealth = maxHealth;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.isOnline = false;
        this.lastSeen = lastSeen;
        this.inventory = inventory != null ? inventory : new Inventory(100.0, 20);
        this.equipment = equipment != null ? equipment : new Equipment(this);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
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

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setEquipment(Equipment equipment) {
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

    // Add inventory methods
    public Inventory getInventory() {
        return inventory;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    // Add helper methods for item management
    public InventoryResult pickupItem(Item item) {
        return inventory.addItem(item);
    }

    public InventoryResult dropItem(UUID itemId, int amount) {
        return inventory.removeItem(itemId, amount);
    }

    public InventoryResult equipItem(Item item) {
        // First check if we have the item in inventory
        Optional<InventoryItem> invItem = inventory.getItem(item.getId());
        if (invItem.isEmpty()) {
            return InventoryResult.failure("You don't have that item");
        }

        // Try to equip the item
        InventoryResult result = equipment.equipItem(item);
        if (result.isSuccess()) {
            // Remove from inventory if equipped successfully
            inventory.removeItem(item.getId(), 1);
        }
        return result;
    }

    public InventoryResult unequipItem(EquipmentSlot slot) {
        Optional<Item> equipped = equipment.getEquippedItem(slot);
        if (equipped.isEmpty()) {
            return InventoryResult.failure("Nothing equipped in that slot");
        }

        // Check if we have inventory space
        InventoryResult canAdd = inventory.addItem(equipped.get());
        if (!canAdd.isSuccess()) {
            return InventoryResult.failure("Not enough inventory space to unequip");
        }

        return equipment.unequipItem(slot);
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