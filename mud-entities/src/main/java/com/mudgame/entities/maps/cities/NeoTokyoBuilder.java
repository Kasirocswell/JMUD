package com.mudgame.entities.maps.cities;

import com.mudgame.entities.GameMap;
import com.mudgame.entities.Room;
import com.mudgame.entities.maps.BaseMapBuilder;
import com.mudgame.entities.Direction;

public class NeoTokyoBuilder extends BaseMapBuilder {
    @Override
    public void buildMap(GameMap map) {
        // Main areas
        Room shibuyaCrossing = createAndAddRoom(map,
                "Shibuya Crossing",
                "A massive intersection filled with holographic advertisements and crowds."
        );

        Room securityPost = createAndAddRoom(map,
                "Shibuya Security Post",
                "A reinforced security station overlooking the crossing. Security bots patrol the area."
        );

        Room maintenanceBay = createAndAddRoom(map,
                "Shibuya Maintenance Bay",
                "A cluttered maintenance area filled with spare parts and repair equipment."
        );

        // Connect the rooms
        connectBidirectional(map, shibuyaCrossing, Direction.NORTH, securityPost);
        connectBidirectional(map, securityPost, Direction.EAST, maintenanceBay);

        // Add more rooms and connections as needed
    }
}