package com.mudgame.entities.maps.cities;

import com.mudgame.entities.GameMap;
import com.mudgame.entities.Room;
import com.mudgame.entities.maps.BaseMapBuilder;

public class NeoTokyoBuilder extends BaseMapBuilder {
    @Override
    public void buildMap(GameMap map) {
        // Implementation
        Room shibuya = createAndAddRoom(map,
                "Shibuya Crossing",
                "A massive intersection filled with holographic advertisements and crowds."
        );

        // Add more rooms and connections
    }
}