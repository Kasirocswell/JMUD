package com.mudgame.entities;

import java.util.*;

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

    // Factory method to create a simple test map
    public static GameMap createTestMap() {
        GameMap map = new GameMap();

        // Create some rooms
        Room startRoom = new Room("Starting Room", "A well-lit room with stone walls.");
        Room northRoom = new Room("Northern Room", "A cold room with an icy floor.");
        Room eastRoom = new Room("Eastern Room", "A garden room filled with plants.");
        Room southRoom = new Room("Southern Room", "A warm room with a fireplace.");
        Room westRoom = new Room("Western Room", "A dark room with mysterious shadows.");

        // Add all rooms to the map
        map.addRoom(startRoom);
        map.addRoom(northRoom);
        map.addRoom(eastRoom);
        map.addRoom(southRoom);
        map.addRoom(westRoom);

        // Set the starting room
        map.setStartingRoom(startRoom);

        // Connect rooms bidirectionally
        map.connectRooms(startRoom, Direction.NORTH, northRoom, true);
        map.connectRooms(startRoom, Direction.EAST, eastRoom, true);
        map.connectRooms(startRoom, Direction.SOUTH, southRoom, true);
        map.connectRooms(startRoom, Direction.WEST, westRoom, true);

        return map;
    }
}