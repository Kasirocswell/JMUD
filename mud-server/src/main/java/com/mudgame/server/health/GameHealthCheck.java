package com.mudgame.server.health;

import com.codahale.metrics.health.HealthCheck;
import com.mudgame.server.core.GameState;

public class GameHealthCheck extends HealthCheck {
    private final GameState gameState;

    public GameHealthCheck(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    protected Result check() {
        try {
            int playerCount = gameState.getOnlinePlayerCount();
            int npcCount = gameState.getAllNPCs().size();

            StringBuilder status = new StringBuilder()
                    .append("Game is running with ")
                    .append(playerCount)
                    .append(" players online and ")
                    .append(npcCount)
                    .append(" active NPCs");

            // Perform basic validation
            if (!gameState.validateGameState()) {
                return Result.unhealthy("Game state validation failed");
            }

            return Result.healthy(status.toString());
        } catch (Exception e) {
            return Result.unhealthy("Game state check failed: " + e.getMessage());
        }
    }
}