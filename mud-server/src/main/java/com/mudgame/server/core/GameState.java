package com.mudgame.server.core;

import com.mudgame.entities.*;
import com.mudgame.server.commands.DefaultCommandRegistry;
import com.mudgame.server.services.ItemFactory;
import com.mudgame.server.services.NPCSpawner;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    private final NPCSpawner npcSpawner;
    private long lastNPCTick = System.currentTimeMillis();
    private static final long NPC_TICK_INTERVAL = 1000; // 1 second tick rate

    public GameState(int maxPlayers, DataSource dataSource) {
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.gameMap = GameMap.createTestMap();
        this.dataSource = dataSource;
        this.itemFactory = new ItemFactory(dataSource);
        this.npcSpawner = new NPCSpawner(gameMap);

        // Initialize NPCs
        npcSpawner.spawnInitialNPCs();

        System.out.println("Game world initialized with " + gameMap.getAllRooms().size() + " rooms");
        System.out.println("Starting room: " + gameMap.getStartingRoom().getName());
    }

    // DataSource Getter
    public DataSource getDataSource() {
        return dataSource;
    }

    // NPC Management Methods
    public void tickNPCs() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastNPCTick >= NPC_TICK_INTERVAL) {
            npcSpawner.getActiveNPCs().forEach(NPC::onTick);
            npcSpawner.handleRespawns();
            lastNPCTick = currentTime;
        }
    }

    public Optional<NPC> getNPC(UUID npcId) {
        return npcSpawner.getNPC(npcId);
    }

    public Collection<NPC> getAllNPCs() {
        return npcSpawner.getActiveNPCs();
    }

    public void removeNPC(UUID npcId) {
        npcSpawner.removeNPC(npcId);
    }

    public NPCSpawner getNpcSpawner() {
        return npcSpawner;
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
            Inventory inventory = new Inventory(100.0, 20);
            Equipment equipment = new Equipment(null);

            Player newPlayer = new Player(
                    id,
                    ownerId,
                    firstName,
                    lastName,
                    race,
                    characterClass,
                    inventory,
                    equipment,
                    100,
                    null,
                    1,
                    100,
                    100,
                    100,
                    100,
                    System.currentTimeMillis()
            );

            players.put(id, newPlayer);

            try {
                itemFactory.giveStarterItems(newPlayer);
            } catch (Exception e) {
                players.remove(id);
                throw e;
            }

            return newPlayer;
        } catch (Exception e) {
            System.err.println("Error creating character: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating character: " + e.getMessage(), e);
        }
    }

    public Player loadPlayer(UUID playerId) {
        Player player = players.get(playerId);
        if (player != null) {
            Room room;

            String roomId = player.getCurrentRoomId();
            if (roomId != null) {
                room = gameMap.getRoom(roomId);
            } else {
                room = gameMap.getStartingRoom();
                System.out.println("Placing new player in starting room: " + room.getName());
            }

            player.setCurrentRoom(room);
            room.addPlayer(player);

            player.setInventory(itemFactory.loadPlayerInventory(player));
            player.setEquipment(itemFactory.loadPlayerEquipment(player));

            player.setOnline(true);

            System.out.println("Player " + player.getFullName() + " loaded into " + room.getName() +
                    " with " + player.getInventory().getUsedSlots() + " items");
        }
        return player;
    }

    public void unloadPlayer(UUID playerId) {
        Player player = players.get(playerId);
        if (player != null) {
            Room currentRoom = player.getCurrentRoom();
            if (currentRoom != null) {
                currentRoom.removePlayer(player);
            }
            player.setOnline(false);
        }
    }

    // In GameState.java
    public Player joinGame(UUID userId, UUID playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new IllegalStateException("Character not found");
        }

        if (!player.getOwnerId().equals(userId)) {
            throw new IllegalStateException("Not authorized to use this character");
        }

        // First check if they have a saved room
        String savedRoomId = player.getCurrentRoomId();
        Room targetRoom;

        if (savedRoomId != null) {
            targetRoom = gameMap.getRoom(savedRoomId);
            // If saved room doesn't exist anymore, use starting room
            if (targetRoom == null) {
                targetRoom = gameMap.getStartingRoom();
                player.setCurrentRoomId(targetRoom.getId());
            }
        } else {
            // No saved room, use starting room
            targetRoom = gameMap.getStartingRoom();
            player.setCurrentRoomId(targetRoom.getId());
        }

        // Set the current room and add player to it
        player.setCurrentRoom(targetRoom);
        targetRoom.addPlayer(player);

        // Load inventory and equipment
        player.setInventory(itemFactory.loadPlayerInventory(player));
        player.setEquipment(itemFactory.loadPlayerEquipment(player));
        player.setOnline(true);

        System.out.println("Player " + player.getFullName() + " joined in room: " + targetRoom.getName());
        return player;
    }

    // Add method to save player's current location
    public void savePlayerLocation(Player player) {
        if (player == null || player.getCurrentRoom() == null) return;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE character SET current_location = ? WHERE id = ?"
             )) {
            stmt.setString(1, player.getCurrentRoom().getId());
            stmt.setObject(2, player.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving player location: " + e.getMessage());
        }
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
        // Validate players
        for (Player player : getOnlinePlayers()) {
            if (player.getCurrentRoom() == null) {
                valid = false;
                Room startingRoom = gameMap.getStartingRoom();
                player.setCurrentRoom(startingRoom);
                startingRoom.addPlayer(player);
            }

            if (player.getInventory() == null) {
                valid = false;
                player.setInventory(itemFactory.loadPlayerInventory(player));
            }
            if (player.getEquipment() == null) {
                valid = false;
                player.setEquipment(itemFactory.loadPlayerEquipment(player));
            }
        }

        // Validate NPCs
        for (NPC npc : npcSpawner.getActiveNPCs()) {
            if (npc.getCurrentRoom() == null) {
                valid = false;
                Room startingRoom = gameMap.getStartingRoom();
                npc.setCurrentRoom(startingRoom);
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

        // Clean up NPCs
        new ArrayList<>(npcSpawner.getActiveNPCs()).forEach(npc ->
                npcSpawner.removeNPC(npc.getId()));
    }

    // Map Access
    public GameMap getGameMap() {
        return gameMap;
    }
}