package com.mudgame.entities.maps;

import com.mudgame.entities.Direction;
import com.mudgame.entities.GameMap;
import com.mudgame.entities.Room;

public abstract class BaseMapBuilder implements MapBuilder {
    protected Room createAndAddRoom(GameMap map, String name, String description) {
        Room room = new Room(name, description);
        map.addRoom(room);
        return room;
    }

    protected void connectBidirectional(GameMap map, Room room1, Direction direction, Room room2) {
        map.connectRooms(room1, direction, room2, true);
    }

    // The buildMap method is left abstract as each specific builder will implement it
    @Override
    public abstract void buildMap(GameMap map);
}