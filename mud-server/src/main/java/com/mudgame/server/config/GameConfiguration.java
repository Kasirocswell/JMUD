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

    public String getGameName() {
        return gameName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}