package com.mudgame.server.config;

import io.dropwizard.core.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GameConfiguration extends Configuration {
    @JsonProperty
    private String gameName = "Simple MUD";

    @JsonProperty
    private int maxPlayers = 100;

    @JsonProperty
    private boolean debugMode = false;

    @JsonProperty
    private DatabaseConfiguration database;

    public String getGameName() {
        return gameName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setDatabase(DatabaseConfiguration database) {
        this.database = database;
    }

    public static class DatabaseConfiguration {
        @JsonProperty
        private String url;

        @JsonProperty
        private String password;

        public String getUrl() {
            return url;
        }

        public String getPassword() {
            return password;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}