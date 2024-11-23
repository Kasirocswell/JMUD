package com.mudgame.server.core;

import com.mudgame.entities.*;
import com.mudgame.server.commands.DefaultCommandRegistry;
import com.mudgame.server.services.ItemFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameState {
    private final GameMap gameMap;
    private final Map<UUID, Player> players;
    private final int maxPlayers;
    private DefaultCommandRegistry commandRegistry;
    private final ItemFactory itemFactory;
    private final DataSource dataSource;

    public GameState(int maxPlayers, DataSource dataSource) {
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.gameMap = GameMap.createTestMap();
        this.dataSource = dataSource;
        this.itemFactory = new ItemFactory(dataSource);
        System.out.println("Game world initialized with " + gameMap.getAllRooms().size() + " rooms");
        System.out.println("Starting room: " + gameMap.getStartingRoom().getName());
    }

    // Player Management Methods
    public Player getPlayer(UUID playerId) {
        return players.get(playerId);
    }

    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    public List<Player> getOnlinePlayers() {
        return players.values().stream()
                .filter(Player::isOnline)
                .collect(Collectors.toList());
    }

    public List<Player> getPlayersByOwnerId(UUID ownerId) {
        return players.values().stream()
                .filter(player -> player.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getOnlinePlayerCount() {
        return (int) players.values().stream()
                .filter(Player::isOnline)
                .count();
    }

    public boolean isPlayerOnline(UUID playerId) {
        Player player = players.get(playerId);
        return player != null && player.isOnline();
    }

    // Character Creation and Management
    public Player createCharacter(UUID id, UUID ownerId, String firstName, String lastName,
                                  Race race, CharacterClass characterClass,
                                  Map<Attributes, Integer> attributes) {
        try {
            // Create initial inventory and equipment
            Inventory inventory = new Inventory(100.0, 20);
            Equipment equipment = new Equipment(null);

            // Create new player using the provided ID, matching the constructor exactly
            Player newPlayer = new Player(
                    id,                 // id
                    ownerId,           // ownerId
                    firstName,         // firstName
                    lastName,          // lastName
                    race,             // race
                    characterClass,    // characterClass
                    inventory,         // inventory
                    equipment,         // equipment
                    100,              // credits
                    null,             // currentRoomId
                    1,                // level
                    100,              // health
                    100,              // maxHealth
                    100,              // energy
                    100,              // maxEnergy
                    System.currentTimeMillis()  // lastSeen
            );

            // Add to players map FIRST
            players.put(newPlayer.getId(), newPlayer);

            try {
                // Give starter items (now the player will exist in the map)
                itemFactory.giveStarterItems(newPlayer);
            } catch (Exception e) {
                // If item giving fails, remove from players map
                players.remove(newPlayer.getId());
                throw e;
            }

            return newPlayer;
        } catch (Exception e) {
            System.err.println("Error creating character: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating character: " + e.getMessage(), e);
        }
    }

    // The rest of your existing methods remain unchanged
    public Player loadPlayer(UUID playerId) {
        Player player = players.get(playerId);
        if (player != null) {
            Room room;

            // If player has a current room, try to load it
            String roomId = player.getCurrentRoomId();
            if (roomId != null) {
                room = gameMap.getRoom(roomId);
            } else {
                // If no room is set or room doesn't exist, start at spaceport
                room = gameMap.getStartingRoom();
                System.out.println("Placing new player in starting room: " + room.getName());
            }

            // Update player's current room
            player.setCurrentRoom(room);
            room.addPlayer(player);

            // Load inventory and equipment
            player.setInventory(itemFactory.loadPlayerInventory(player));
            player.setEquipment(itemFactory.loadPlayerEquipment(player));

            // Mark player as online
            player.setOnline(true);

            System.out.println("Player " + player.getFullName() + " loaded into " + room.getName() +
                    " with " + player.getInventory().getUsedSlots() + " items");
        }
        return player;
    }

    public void unloadPlayer(UUID playerId) {
        Player player = players.get(playerId);
        if (player != null) {
            // Remove player from current room
            Room currentRoom = player.getCurrentRoom();
            if (currentRoom != null) {
                currentRoom.removePlayer(player);
            }

            // Mark player as offline but keep in players map
            player.setOnline(false);
        }
    }

    public Player joinGame(UUID userId, UUID playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new IllegalStateException("Character not found");
        }

        if (!player.getOwnerId().equals(userId)) {
            throw new IllegalStateException("Not authorized to use this character");
        }

        return loadPlayer(playerId);
    }

    public void leaveGame(UUID playerId) {
        unloadPlayer(playerId);
    }

    // Item Management Methods
    public Optional<Item> getItem(UUID itemId) {
        return itemFactory.getItem(itemId);
    }

    public Collection<Item> getStarterItems() {
        return itemFactory.getStarterItems();
    }

    // Movement and Room Management
    public boolean movePlayer(UUID playerId, Direction direction) {
        Player player = players.get(playerId);
        if (player != null && player.isOnline()) {
            Room currentRoom = player.getCurrentRoom();
            if (currentRoom != null) {
                Room nextRoom = currentRoom.getExit(direction);
                if (nextRoom != null) {
                    currentRoom.removePlayer(player);
                    nextRoom.addPlayer(player);
                    player.setCurrentRoom(nextRoom);
                    return true;
                }
            }
        }
        return false;
    }

    // Command Registry Management
    public void setCommandRegistry(DefaultCommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    public DefaultCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    // Game State Validation
    public boolean validateGameState() {
        boolean valid = true;
        for (Player player : getOnlinePlayers()) {
            if (player.getCurrentRoom() == null) {
                valid = false;
                Room startingRoom = gameMap.getStartingRoom();
                player.setCurrentRoom(startingRoom);
                startingRoom.addPlayer(player);
            }

            // Validate inventory and equipment
            if (player.getInventory() == null) {
                valid = false;
                player.setInventory(itemFactory.loadPlayerInventory(player));
            }
            if (player.getEquipment() == null) {
                valid = false;
                player.setEquipment(itemFactory.loadPlayerEquipment(player));
            }
        }
        return valid;
    }

    // Cleanup
    public void cleanup() {
        for (Player player : players.values()) {
            if (player.isOnline()) {
                unloadPlayer(player.getId());
            }
        }
    }

    // Map Access
    public GameMap getGameMap() {
        return gameMap;
    }
}