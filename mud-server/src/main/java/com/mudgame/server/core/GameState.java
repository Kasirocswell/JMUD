package com.mudgame.server.core;

import com.mudgame.entities.GameMap;
import com.mudgame.entities.Player;
import com.mudgame.server.commands.DefaultCommandRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    private final GameMap gameMap;
    private final Map<String, Player> players;
    private final int maxPlayers;
    private DefaultCommandRegistry commandRegistry;

    public GameState(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        this.gameMap = GameMap.createTestMap();
        this.players = new ConcurrentHashMap<>();
    }

    public Player joinGame(String playerName) {
        if (players.size() >= maxPlayers) {
            throw new IllegalStateException("Game is full");
        }

        Player player = new Player(playerName);
        player.setCurrentRoom(gameMap.getStartingRoom());
        gameMap.getStartingRoom().addPlayer(player);
        players.put(player.getId(), player);

        return player;
    }

    public void leaveGame(String playerId) {
        Player player = players.remove(playerId);
        if (player != null && player.getCurrentRoom() != null) {
            player.getCurrentRoom().removePlayer(player);
        }
    }

    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void setCommandRegistry(DefaultCommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    public DefaultCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean isPlayerOnline(String playerId) {
        return players.containsKey(playerId);
    }
}