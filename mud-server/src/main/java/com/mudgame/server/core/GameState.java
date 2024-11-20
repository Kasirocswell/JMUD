package com.mudgame.server.core;

import com.mudgame.entities.*;
import com.mudgame.server.commands.DefaultCommandRegistry;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameState {
    private final GameMap gameMap;
    private final Map<String, Player> players;
    private final int maxPlayers;
    private DefaultCommandRegistry commandRegistry;

    public GameState(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.gameMap = GameMap.createTestMap();
        System.out.println("Game world initialized with " + gameMap.getAllRooms().size() + " rooms");
        System.out.println("Starting room: " + gameMap.getStartingRoom().getName());
    }

    // Player Management Methods
    public Player getPlayer(String playerId) {
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

    public List<Player> getPlayersByOwnerId(String ownerId) {
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

    public boolean isPlayerOnline(String playerId) {
        Player player = players.get(playerId);
        return player != null && player.isOnline();
    }

    // Character Creation and Management
    public Player createCharacter(String ownerId, String firstName, String lastName,
                                  Race race, CharacterClass characterClass, Map<Attributes, Integer> attributes) {
        Player newPlayer = new Player(ownerId, firstName, lastName, race, characterClass);
        players.put(newPlayer.getId(), newPlayer);
        return newPlayer;
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }

    public void removePlayer(String playerId) {
        Player player = players.get(playerId);
        if (player != null) {
            if (player.getCurrentRoom() != null) {
                player.getCurrentRoom().removePlayer(player);
            }
            players.remove(playerId);
        }
    }

    // Game Session Management
    public Player loadPlayer(String playerId) {
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

            // Mark player as online
            player.setOnline(true);

            System.out.println("Player " + player.getFullName() + " loaded into " + room.getName());
        }
        return player;
    }

    public void unloadPlayer(String playerId) {
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

    public Player joinGame(String userId, String playerId) {
        // Ensure playerId is treated as a String
        Player player = players.get(playerId);
        if (player == null) {
            throw new IllegalStateException("Character not found");
        }

        if (!player.getOwnerId().equals(userId)) {
            throw new IllegalStateException("Not authorized to use this character");
        }

        return loadPlayer(playerId);
    }


    public void leaveGame(String playerId) {
        unloadPlayer(playerId);
    }

    // Movement and Room Management
    public boolean movePlayer(String playerId, Direction direction) {
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

    // Map Access
    public GameMap getGameMap() {
        return gameMap;
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
        // Ensure all online players are in a room
        boolean valid = true;
        for (Player player : getOnlinePlayers()) {
            if (player.getCurrentRoom() == null) {
                valid = false;
                // Attempt to fix by placing in starting room
                Room startingRoom = gameMap.getStartingRoom();
                player.setCurrentRoom(startingRoom);
                startingRoom.addPlayer(player);
            }
        }
        return valid;
    }

    // Utility Methods
    public void broadcastToRoom(Room room, String message) {
        if (room != null) {
            for (Player player : room.getPlayers()) {
                // Here you would typically send the message to the player's client
                // Implementation depends on your networking setup
            }
        }
    }

    public void broadcastToAll(String message) {
        for (Player player : getOnlinePlayers()) {
            // Here you would typically send the message to the player's client
            // Implementation depends on your networking setup
        }
    }

    public void cleanup() {
        // Clean up any resources, save state, etc.
        for (Player player : players.values()) {
            if (player.isOnline()) {
                unloadPlayer(player.getId());
            }
        }
    }
}