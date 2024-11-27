package com.mudgame.server.core;

import com.mudgame.events.EventListener;
import com.mudgame.server.services.RedisBroadcaster;
import io.dropwizard.lifecycle.Managed;

import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager implements Managed, EventListener {
    private final RedisBroadcaster redisBroadcaster;
    private final ScheduledExecutorService scheduler;
    private static final int TICK_RATE_MS = 1000; // 1-second tick
    private final GameState gameState;

    public GameManager(int maxPlayers, DataSource dataSource) {
        this.redisBroadcaster = new RedisBroadcaster();
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Pass this instance as the EventListener to GameState
        this.gameState = new GameState(maxPlayers, dataSource, this);

        // Example test event during initialization
        System.out.println("Testing EventListener implementation...");
        onEvent("room", "101", "A test message for room 101.");
        onEvent("player", "202", "A test message for player 202.");
        onEvent("system", null, "A test message for the entire system.");
        System.out.println("EventListener test complete.");
    }

    @Override
    public void onEvent(String eventType, String target, String message) {
        switch (eventType) {
            case "room":
                System.out.println("Broadcasting to room: " + target + " | Message: " + message);
                redisBroadcaster.broadcast("room:" + target, message);
                break;

            case "player":
                System.out.println("Sending private message to player: " + target + " | Message: " + message);
                redisBroadcaster.broadcast("player:" + target, message);
                break;

            case "system":
                System.out.println("Broadcasting system message: " + message);
                redisBroadcaster.broadcast("system", message);
                break;

            default:
                System.err.println("Unknown event type: " + eventType);
        }
    }

    @Override
    public void start() {
        // Schedule the game tick task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Perform game state updates
                gameState.tickNPCs();
                System.out.println("Game tick executed.");
            } catch (Exception e) {
                System.err.println("Error in game tick: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);

        System.out.println("Game manager started with tick rate: " + TICK_RATE_MS + "ms.");
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

        // Clean up Redis resources
        redisBroadcaster.close();
        System.out.println("Game manager stopped.");
    }

    // Expose GameState for external access if needed
    public GameState getGameState() {
        return gameState;
    }
}
