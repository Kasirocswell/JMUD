package com.mudgame.entities.maps.cities;

import com.mudgame.entities.GameMap;
import com.mudgame.entities.Room;
import com.mudgame.entities.maps.BaseMapBuilder;

public class OldLondonBuilder extends BaseMapBuilder {
    @Override
    public void buildMap(GameMap map) {
        // Implementation
        Room bigBen = createAndAddRoom(map,
                "Big Ben Plaza",
                "The ancient clock tower stands tall among modern buildings."
        );

        // Add more rooms and connections
    }
}