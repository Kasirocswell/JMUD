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
            int playerCount = gameState.getPlayerCount();
            return Result.healthy("Game is running with " + playerCount + " players online");
        } catch (Exception e) {
            return Result.unhealthy("Game state check failed: " + e.getMessage());
        }
    }
}