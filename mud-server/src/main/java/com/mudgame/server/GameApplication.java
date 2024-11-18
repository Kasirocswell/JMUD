package com.mudgame.server;

import com.mudgame.server.config.GameConfiguration;
import com.mudgame.server.core.GameState;
import com.mudgame.server.health.GameHealthCheck;
import com.mudgame.server.resources.GameResource;
import com.mudgame.server.commands.DefaultCommandRegistry;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;

public class GameApplication extends Application<GameConfiguration> {
    public static void main(String[] args) throws Exception {
        new GameApplication().run(args);
    }

    @Override
    public void run(GameConfiguration configuration, Environment environment) {
        // Create game state
        GameState gameState = new GameState(configuration.getMaxPlayers());

        // Register commands
        DefaultCommandRegistry commandRegistry = new DefaultCommandRegistry();
        gameState.setCommandRegistry(commandRegistry);

        // Register health check
        GameHealthCheck healthCheck = new GameHealthCheck(gameState);
        environment.healthChecks().register("game", healthCheck);

        // Register resources
        GameResource gameResource = new GameResource(gameState);
        environment.jersey().register(gameResource);

        // Enable CORS
        environment.jersey().register(new CORSFilter());
    }
}