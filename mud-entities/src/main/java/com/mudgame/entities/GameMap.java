package com.mudgame.entities;

import com.mudgame.entities.maps.WorldMapBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GameMap {
    private final Map<String, Room> rooms;
    private Room startingRoom;

    public GameMap() {
        this.rooms = new HashMap<>();
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
        if (startingRoom == null) {
            startingRoom = room;
        }
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public Room getStartingRoom() {
        return startingRoom;
    }

    public void setStartingRoom(Room room) {
        if (!rooms.containsKey(room.getId())) {
            addRoom(room);
        }
        this.startingRoom = room;
    }

    public void connectRooms(Room room1, Direction direction, Room room2, boolean bidirectional) {
        // Ensure both rooms are in the map
        if (!rooms.containsKey(room1.getId())) {
            addRoom(room1);
        }
        if (!rooms.containsKey(room2.getId())) {
            addRoom(room2);
        }

        // Connect the rooms
        room1.setExit(direction, room2);
        if (bidirectional) {
            room2.setExit(direction.opposite(), room1);
        }
    }

    public Set<Room> getAllRooms() {
        return new HashSet<>(rooms.values());
    }

    // Method to find a path between two rooms (useful for NPCs or automated movement)
    public List<Direction> findPath(Room start, Room destination) {
        if (start == destination) {
            return new ArrayList<>();
        }

        Map<Room, Room> previousRoom = new HashMap<>();
        Map<Room, Direction> previousDirection = new HashMap<>();
        Queue<Room> queue = new LinkedList<>();
        Set<Room> visited = new HashSet<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Room current = queue.poll();

            for (Map.Entry<Direction, Room> exit : current.getExits().entrySet()) {
                Room neighbor = exit.getValue();
                Direction direction = exit.getKey();

                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    previousRoom.put(neighbor, current);
                    previousDirection.put(neighbor, direction);
                    queue.offer(neighbor);

                    if (neighbor == destination) {
                        return reconstructPath(previousRoom, previousDirection, start, destination);
                    }
                }
            }
        }

        return null; // No path found
    }

    private List<Direction> reconstructPath(Map<Room, Room> previousRoom,
                                            Map<Room, Direction> previousDirection,
                                            Room start, Room destination) {
        List<Direction> path = new ArrayList<>();
        Room current = destination;

        while (current != start) {
            Room prev = previousRoom.get(current);
            path.add(0, previousDirection.get(current));
            current = prev;
        }

        return path;
    }

    // Factory method to create the game world
    public static GameMap createTestMap() {
        return new WorldMapBuilder().buildWorld();
    }

    // Helper method to get room by name (useful for debugging and testing)
    public Room getRoomByName(String name) {
        return rooms.values().stream()
                .filter(room -> room.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}