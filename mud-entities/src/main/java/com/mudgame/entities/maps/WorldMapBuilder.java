package com.mudgame.entities.maps;

import com.mudgame.entities.Direction;
import com.mudgame.entities.GameMap;
import com.mudgame.entities.Room;
import com.mudgame.entities.maps.cities.NeoTokyoBuilder;
import com.mudgame.entities.maps.cities.OldLondonBuilder;

import java.util.ArrayList;
import java.util.List;

public class WorldMapBuilder {
    private final List<MapBuilder> builders = new ArrayList<>();

    public WorldMapBuilder() {
        // Register all map builders
        builders.add(new NeoTokyoBuilder());
        builders.add(new OldLondonBuilder());
        // Add more builders as needed
    }

    public GameMap buildWorld() {
        GameMap map = new GameMap();

        // Create central hub/starting area
        Room spaceport = new Room(
                "Central Spaceport",
                "A massive spaceport connecting all major cities and colonies. " +
                        "Ships of all sizes dock and depart regularly."
        );
        map.addRoom(spaceport);
        map.setStartingRoom(spaceport);

        // Build all sub-maps
        for (MapBuilder builder : builders) {
            builder.buildMap(map);
        }

        // Create connections between areas
        createInterAreaConnections(map, spaceport);

        return map;
    }

    private void createInterAreaConnections(GameMap map, Room spaceport) {
        Room neoTokyoEntry = map.getRoomByName("Shibuya Crossing");
        Room oldLondonEntry = map.getRoomByName("Big Ben Plaza");

        map.connectRooms(spaceport, Direction.NORTH, neoTokyoEntry, false);
        map.connectRooms(spaceport, Direction.SOUTH, oldLondonEntry, false);
    }
}