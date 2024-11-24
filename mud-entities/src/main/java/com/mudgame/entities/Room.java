package com.mudgame.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Room {
    private final String id;
    private String name;
    private String description;
    private final Map<Direction, Room> exits;
    private final List<Item> items;
    private final Set<Player> players;
    private final Set<NPC> npcs = new HashSet<>();


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

        // Room name and description
        sb.append(name).append("\n");
        sb.append(description).append("\n\n");

        // Exits
        if (!exits.isEmpty()) {
            sb.append("Exits: ");
            sb.append(String.join(", ",
                    exits.keySet().stream()
                            .map(dir -> dir.name().toLowerCase())
                            .toArray(String[]::new)));
            sb.append("\n");
        }

        // Items
        if (!items.isEmpty()) {
            sb.append("\nItems here:");
            items.stream()
                    .collect(Collectors.groupingBy(Item::getName, Collectors.counting()))
                    .forEach((name, count) -> {
                        sb.append("\n  ");
                        if (count > 1) {
                            sb.append(name).append(" (x").append(count).append(")");
                        } else {
                            sb.append(name);
                        }
                    });
            sb.append("\n");
        }

        // NPCs
        if (!npcs.isEmpty()) {
            sb.append("\nNPCs here:");
            npcs.forEach(npc -> {
                sb.append("\n  ").append(npc.getName());
                if (npc.isHostile()) {
                    sb.append(" [Hostile]");
                }
                if (npc.getType() == NPCType.MERCHANT) {
                    sb.append(" [Merchant]");
                }
                if (npc.isDead()) {
                    sb.append(" [Dead]");
                }
            });
            sb.append("\n");
        }

        // Players
        if (!players.isEmpty()) {
            sb.append("\nPlayers here:");
            players.forEach(player -> {
                sb.append("\n  ").append(player.getFullName());
                if (player.isDead()) {
                    sb.append(" [Dead]");
                }
            });
            sb.append("\n");
        }

        return sb.toString();
    }

    public void addNPC(NPC npc) {
        npcs.add(npc);
    }

    public void removeNPC(NPC npc) {
        npcs.remove(npc);
    }

    public Set<NPC> getNPCs() {
        return Collections.unmodifiableSet(npcs);
    }
}