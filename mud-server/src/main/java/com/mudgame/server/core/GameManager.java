package com.mudgame.server.core;

import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager implements Managed {
    private final GameState gameState;
    private final ScheduledExecutorService scheduler;
    private static final int TICK_RATE_MS = 1000; // 1 second tick rate

    public GameManager(GameState gameState) {
        this.gameState = gameState;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void start() {
        // Schedule the game tick task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Process NPC updates
                gameState.tickNPCs();

                // Validate game state periodically (every 5 minutes)
                if (System.currentTimeMillis() % (5 * 60 * 1000) < TICK_RATE_MS) {
                    gameState.validateGameState();
                }
            } catch (Exception e) {
                System.err.println("Error in game tick: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);

        System.out.println("Game manager started with tick rate: " + TICK_RATE_MS + "ms");
    }

    @Override
    public void stop() {
        // Shutdown the scheduler gracefully
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }

        // Cleanup game state
        gameState.cleanup();
        System.out.println("Game manager stopped");
    }
}