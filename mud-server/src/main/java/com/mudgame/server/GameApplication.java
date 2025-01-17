package com.mudgame.server;

import com.mudgame.server.config.GameConfiguration;
import com.mudgame.server.core.GameState;
import com.mudgame.server.core.GameManager;
import com.mudgame.server.health.GameHealthCheck;
import com.mudgame.server.resources.GameResource;
import com.mudgame.server.commands.DefaultCommandRegistry;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import org.postgresql.ds.PGSimpleDataSource;

public class GameApplication extends Application<GameConfiguration> {
    public static void main(String[] args) throws Exception {
        new GameApplication().run(args);
    }

    @Override
    public void run(GameConfiguration configuration, Environment environment) {
        // Create DataSource for Supabase
        PGSimpleDataSource dataSource = new PGSimpleDataSource();

        // Format: jdbc:postgresql://[host]:[port]/[database]
        String jdbcUrl = "jdbc:postgresql://" +
                "db.yilcjyoumyesbfqbjgpd.supabase.co:5432" +
                "/postgres";

        dataSource.setURL(jdbcUrl);
        dataSource.setUser("postgres");
        dataSource.setPassword(configuration.getDatabase().getPassword());

        // Set additional properties
        dataSource.setSsl(true);
        dataSource.setSslMode("require");

        // Create and register the GameManager (which initializes GameState and EventListener)
        GameManager gameManager = new GameManager(configuration.getMaxPlayers(), dataSource);
        environment.lifecycle().manage(gameManager);

        // Get GameState from GameManager
        GameState gameState = gameManager.getGameState();

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
