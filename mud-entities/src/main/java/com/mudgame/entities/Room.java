package com.mudgame.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Room {
    private final String id;
    private String name;
    private String description;
    private final Map<Direction, Room> exits;
    private final List<Item> items;
    private final Set<Player> players;

    public Room(String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.exits = new EnumMap<>(Direction.class);
        this.items = new ArrayList<>();
        this.players = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExit(Direction direction, Room room) {
        exits.put(direction, room);
    }

    public Room getExit(Direction direction) {
        return exits.get(direction);
    }

    public Map<Direction, Room> getExits() {
        return Collections.unmodifiableMap(exits);
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.setCurrentRoom(this);
    }

    public void removePlayer(Player player) {
        players.remove(player);
        if (player.getCurrentRoom() == this) {
            player.setCurrentRoom(null);
        }
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append(description).append("\n\n");

        if (!exits.isEmpty()) {
            sb.append("Exits: ");
            sb.append(String.join(", ",
                    exits.keySet().stream()
                            .map(Direction::name)
                            .toArray(String[]::new)));
            sb.append("\n");
        }

        if (!items.isEmpty()) {
            sb.append("Items: ");
            sb.append(String.join(", ",
                    items.stream()
                            .map(Item::getName)
                            .toArray(String[]::new)));
            sb.append("\n");
        }

        if (!players.isEmpty()) {
            sb.append("Players here: ");
            sb.append(String.join(", ",
                    players.stream()
                            .map(Player::getFullName)
                            .toArray(String[]::new)));
        }

        return sb.toString();
    }
}