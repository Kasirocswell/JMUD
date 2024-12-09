package com.mudgame.server.core;

import com.mudgame.entities.*;
import com.mudgame.server.commands.DefaultCommandRegistry;
import com.mudgame.server.services.ItemFactory;
import com.mudgame.server.services.NPCSpawner;
import com.mudgame.events.EventListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameState {
    private final GameMap gameMap;
    private final EventListener eventListener;
    private final Map<UUID, Player> players;
    private final int maxPlayers;
    private DefaultCommandRegistry commandRegistry;
    private final ItemFactory itemFactory;
    private final DataSource dataSource;
    private final NPCSpawner npcSpawner;
    private long lastNPCTick = System.currentTimeMillis();
    private static final long NPC_TICK_INTERVAL = 1000; // 1 second tick rate

    public GameState(int maxPlayers, DataSource dataSource, EventListener eventListener) {
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.gameMap = GameMap.createTestMap();
        this.dataSource = dataSource;
        this.eventListener = Objects.requireNonNull(eventListener, "EventListener cannot be null");
        this.itemFactory = new ItemFactory(dataSource);

        // Initialize NPCSpawner with EventListener
        this.npcSpawner = new NPCSpawner(gameMap, eventListener);

        // Spawn initial NPCs
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

            // Set starting room name
            String startingRoomName = gameMap.getStartingRoom().getName();

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
                    startingRoomName,
                    1,
                    100,
                    100,
                    100,
                    100,
                    System.currentTimeMillis(),
                    null
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

    public Player joinGame(UUID userId, UUID playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new IllegalStateException("Character not found");
        }

        if (!player.getOwnerId().equals(userId)) {
            throw new IllegalStateException("Not authorized to use this character");
        }

        System.out.println("Joining game - Player: " + player.getFullName() +
                ", Saved room name: " + player.getRoomName());

        // Get the saved room name
        String roomName = player.getRoomName();
        Room targetRoom = null;

        // Try to find the saved room
        if (roomName != null && !roomName.trim().isEmpty()) {
            targetRoom = gameMap.getRoomByName(roomName);
            System.out.println("Looking for saved room: " + roomName +
                    ", found: " + (targetRoom != null));
        }

        // Only use starting room if no saved room or saved room not found
        if (targetRoom == null) {
            targetRoom = gameMap.getStartingRoom();
            player.setRoomName(targetRoom.getName());
            System.out.println("Using starting room: " + targetRoom.getName());
        }

        // Set the current room and add player to it
        player.setCurrentRoom(targetRoom);
        targetRoom.addPlayer(player);

        // Load inventory and equipment
        player.setInventory(itemFactory.loadPlayerInventory(player));
        player.setEquipment(itemFactory.loadPlayerEquipment(player));
        player.setOnline(true);

        System.out.println("Player " + player.getFirstName() +
                " joined in room: " + targetRoom.getName());
        return player;
    }


    public Player loadPlayer(UUID playerId) {
        Player player = players.get(playerId);
        if (player != null) {
            Room room;

            String roomName = player.getRoomName();
            if (roomName != null) {
                room = gameMap.getRoomByName(roomName);
                if (room == null) {
                    room = gameMap.getStartingRoom();
                    player.setRoomName(room.getName());
                    System.out.println("Room not found: " + roomName + ", placing player in starting room: " + room.getName());
                }
            } else {
                room = gameMap.getStartingRoom();
                player.setRoomName(room.getName());
                System.out.println("No room name found, placing new player in starting room: " + room.getName());
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
            savePlayerLocation(player); // Save location before unloading
        }
    }

    public void leaveGame(UUID playerId) {
        unloadPlayer(playerId);
    }


    public void savePlayerLocation(Player player) {
        if (player == null || player.getCurrentRoom() == null) return;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE character SET room_name = ? WHERE id = ?"
             )) {
            stmt.setString(1, player.getCurrentRoom().getName());
            stmt.setObject(2, player.getId());
            stmt.executeUpdate();
            System.out.println("Saved location for player " + player.getFullName() + ": " + player.getCurrentRoom().getName());
        } catch (SQLException e) {
            System.err.println("Error saving player location: " + e.getMessage());
        }
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

                    savePlayerLocation(player);

                    return true;
                }
            }
        }
        return false;
    }

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
        }

        return valid;
    }

    // Cleanup
    public void cleanup() {
        for (Player player : players.values()) {
            if (player.isOnline()) {
                savePlayerLocation(player);
                unloadPlayer(player.getId());
            }
        }

        new ArrayList<>(npcSpawner.getActiveNPCs()).forEach(npc ->
                npcSpawner.removeNPC(npc.getId()));
    }

    public GameMap getGameMap() {
        return gameMap;
    }
}
