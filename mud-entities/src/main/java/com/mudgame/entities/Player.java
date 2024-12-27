package com.mudgame.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;

public class Player extends CombatEntity {
    private final UUID ownerId;
    private String firstName;
    private String lastName;
    private Race race;
    private CharacterClass characterClass;
    private int credits;
    private String roomName;
    @JsonIgnore
    private Room currentRoom;
    private int energy;
    private int maxEnergy;
    private boolean isOnline;
    private long lastSeen;
    private Inventory inventory;
    private Equipment equipment;
    private String specialization;

    // Constructor for new character creation
    public Player(
            @JsonProperty("ownerId") UUID ownerId,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("race") Race race,
            @JsonProperty("characterClass") CharacterClass characterClass,
            Inventory inventory,
            Equipment equipment) {
        super(UUID.randomUUID(), firstName, 1, 100);
        this.ownerId = ownerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.race = race;
        this.characterClass = characterClass;
        this.credits = 100;
        this.energy = 100;
        this.maxEnergy = 100;
        this.isOnline = false;
        this.lastSeen = System.currentTimeMillis();
        this.inventory = inventory;
        this.equipment = equipment;
        this.specialization = null;

        initializeAttributes();
    }

    // Constructor for loading existing character
    public Player(
            @JsonProperty("id") UUID id,
            @JsonProperty("ownerId") UUID ownerId,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("race") Race race,
            @JsonProperty("characterClass") CharacterClass characterClass,
            @JsonProperty("inventory") Inventory inventory,
            @JsonProperty("equipment") Equipment equipment,
            @JsonProperty("credits") int credits,
            @JsonProperty("room_name") String roomName,
            @JsonProperty("level") int level,
            @JsonProperty("health") int health,
            @JsonProperty("maxHealth") int maxHealth,
            @JsonProperty("energy") int energy,
            @JsonProperty("maxEnergy") int maxEnergy,
            @JsonProperty("lastSeen") long lastSeen,
            @JsonProperty("specialization") String specialization) {
        super(id, firstName, level, maxHealth);
        this.ownerId = ownerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.race = race;
        this.characterClass = characterClass;
        this.credits = credits;
        this.roomName = roomName;
        this.health = health;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.isOnline = false;
        this.lastSeen = lastSeen;
        this.inventory = inventory != null ? inventory : new Inventory(100.0, 20);
        this.equipment = equipment != null ? equipment : new Equipment(this);
        this.specialization = specialization;

        initializeAttributes();
    }

    private void initializeAttributes() {
        // Set base attributes based on race and class
        setBaseAttributes();
        // Apply racial bonuses
        applyRacialBonuses();
        // Apply class bonuses
        applyClassBonuses();
        // Update combat-related stats
        updateCombatStats();
    }

    private void setBaseAttributes() {
        for (Attributes attr : Attributes.values()) {
            attributes.put(attr, 10); // Base value of 10 for all attributes
        }
    }

    private void applyRacialBonuses() {
        switch (race) {
            case HUMAN:
                // Humans get +1 to all attributes
                attributes.replaceAll((k, v) -> v + 1);
                break;
            case DRACONIAN:
                // Draconians get +2 Strength, +2 Constitution, -1 Agility
                attributes.merge(Attributes.STRENGTH, 2, Integer::sum);
                attributes.merge(Attributes.CONSTITUTION, 2, Integer::sum);
                attributes.merge(Attributes.AGILITY, -1, Integer::sum);
                break;
            case SYNTH:
                // Synths get +2 Intelligence, +2 Speed, -1 Charisma
                attributes.merge(Attributes.INTELLIGENCE, 2, Integer::sum);
                attributes.merge(Attributes.SPEED, 2, Integer::sum);
                attributes.merge(Attributes.CHARISMA, -1, Integer::sum);
                break;
            // Add other racial bonuses
        }
    }

    private void applyClassBonuses() {
        switch (characterClass) {
            case SOLDIER:
                attributes.merge(Attributes.STRENGTH, 2, Integer::sum);
                attributes.merge(Attributes.CONSTITUTION, 2, Integer::sum);
                break;
            case HACKER:
                attributes.merge(Attributes.INTELLIGENCE, 2, Integer::sum);
                attributes.merge(Attributes.PERCEPTION, 2, Integer::sum);
                break;
            // Add other class bonuses
        }
    }

    private void updateCombatStats() {
        // Update attack speed based on Speed attribute
        combatState.updateAttackCooldown(attributes.get(Attributes.SPEED));

        // Update max health based on Constitution
        int constitutionBonus = (attributes.get(Attributes.CONSTITUTION) - 10) * 5;
        this.maxHealth = 100 + constitutionBonus + (level * 10);
        this.health = Math.min(health, maxHealth);
    }

    @Override
    protected void onDeath() {
        // Calculate credit loss (10% of current credits)
        int creditLoss = (int)(credits * 0.10);
        credits -= creditLoss;

        // Exit combat
        combatState.exitCombat();

        // Notify room of death
        if (currentRoom != null) {
            // You would need to implement the event system to broadcast this message
            String deathMessage = String.format("%s has been defeated!", getFullName());
            // Broadcast death message to room
        }
    }

    @Override
    protected Optional<Item> getEquippedWeapon() {
        return equipment.getEquippedItem(EquipmentSlot.MAIN_HAND);
    }

    @Override
    protected Optional<Item> getEquippedItem(EquipmentSlot slot) {
        return equipment.getEquippedItem(slot);
    }

    @Override
    public Room getCurrentRoom() {
        return currentRoom;
    }

    // Additional Player-specific methods
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + (lastName != null ? " " + lastName : ""); }
    public UUID getOwnerId() { return ownerId; }
    public Race getRace() { return race; }
    public CharacterClass getCharacterClass() { return characterClass; }
    public int getCredits() { return credits; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    public boolean isOnline() { return isOnline; }
    public long getLastSeen() { return lastSeen; }
    public Inventory getInventory() { return inventory; }
    public Equipment getEquipment() { return equipment; }
    public String getSpecialization() { return specialization; }

    @JsonProperty("room_name")
    public String getRoomName() { return roomName; }

    // Setters
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setCredits(int credits) { this.credits = credits; }
    public void setEnergy(int energy) { this.energy = Math.min(maxEnergy, Math.max(0, energy)); }
    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
        if (energy > maxEnergy) energy = maxEnergy;
    }
    public void setOnline(boolean online) {
        this.isOnline = online;
        if (!online) this.lastSeen = System.currentTimeMillis();
    }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    @JsonProperty("room_name")
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
        if (room != null) {
            this.roomName = room.getName();
        }
    }

    // Combat-related methods
    public void useEnergy(int amount) {
        setEnergy(energy - amount);
    }

    public void restoreEnergy(int amount) {
        setEnergy(energy + amount);
    }

    public boolean hasEnoughEnergy(int amount) {
        return energy >= amount;
    }

    // Equipment methods
    public InventoryResult equipItem(Item item) {
        Optional<InventoryItem> invItem = inventory.getItem(item.getId());
        if (invItem.isEmpty()) {
            return InventoryResult.failure("You don't have that item");
        }

        InventoryResult result = equipment.equipItem(item);
        if (result.isSuccess()) {
            inventory.removeItem(item.getId(), 1);
        }
        return result;
    }

    public InventoryResult unequipItem(EquipmentSlot slot) {
        Optional<Item> equipped = equipment.getEquippedItem(slot);
        if (equipped.isEmpty()) {
            return InventoryResult.failure("Nothing equipped in that slot");
        }

        InventoryResult canAdd = inventory.addItem(equipped.get());
        if (!canAdd.isSuccess()) {
            return InventoryResult.failure("Not enough inventory space to unequip");
        }

        return equipment.unequipItem(slot);
    }

    // Basic inventory manipulation
    public InventoryResult pickupItem(Item item) {
        return inventory.addItem(item);
    }

    public InventoryResult dropItem(Item item) {
        return inventory.removeItem(item.getId(), item.getCurrentStackSize());
    }

    public InventoryResult dropItem(UUID itemId, int amount) {
        return inventory.removeItem(itemId, amount);
    }

    // Helper methods for finding items
    public Optional<InventoryItem> findItem(String itemName) {
        return inventory.getItems().stream()
                .filter(item -> item.getItem().getName().toLowerCase().contains(itemName.toLowerCase()))
                .findFirst();
    }

    public Optional<InventoryItem> findItemById(UUID itemId) {
        return inventory.getItem(itemId);
    }

    // Quantity checking
    public boolean hasItem(UUID itemId) {
        return inventory.getItem(itemId).isPresent();
    }

    public boolean hasItem(String itemName) {
        return findItem(itemName).isPresent();
    }

    public boolean hasItems(UUID itemId, int amount) {
        Optional<InventoryItem> item = inventory.getItem(itemId);
        return item.map(inventoryItem -> inventoryItem.getCurrentStackSize() >= amount).orElse(false);
    }

    // Weight and space checking
    public boolean canCarryMore(double weight) {
        return inventory.getRemainingWeight() >= weight;
    }

    public boolean hasInventorySpace() {
        return inventory.getRemainingSlots() > 0;
    }
}