package com.mudgame.server.services;

import com.mudgame.entities.*;
import com.mudgame.entities.npcs.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NPCSpawner {
    private final GameMap gameMap;
    private final Map<UUID, NPC> activeNPCs = new ConcurrentHashMap<>();
    private final Map<String, SpawnableNPC> npcTypes = new HashMap<>();
    private final Random random = new Random();

    // Configuration constants
    private static final int SPAWN_CHECK_INTERVAL = 60; // seconds
    private static final double DEFAULT_SPAWN_CHANCE = 0.1; // 10% base chance
    private long lastSpawnCheck = System.currentTimeMillis();

    public NPCSpawner(GameMap gameMap) {
        this.gameMap = gameMap;
        registerNPCTypes();
    }

    /**
     * Register all available NPC types
     */
    private void registerNPCTypes() {
        try {
            // Register security bots
            registerNPCType(new SecurityBotNPC(1));

            // Additional NPC types will be registered here
            // registerNPCType(new MerchantBotNPC(1));
            // registerNPCType(new MaintenanceBotNPC(1));

            System.out.println("Registered " + npcTypes.size() + " NPC types");
        } catch (Exception e) {
            System.err.println("Error registering NPC types: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Register a new NPC type with the spawner
     */
    private void registerNPCType(SpawnableNPC npc) {
        SpawnableNPC.SpawnConfiguration config = npc.getSpawnConfiguration();
        if (config == null) {
            throw new IllegalArgumentException("NPC " + npc.getClass().getSimpleName() +
                    " has no spawn configuration");
        }

        npcTypes.put(config.getNpcId(), npc);
        System.out.println("Registered NPC type: " + config.getNpcId());
    }

    /**
     * Spawn initial NPCs when the game world starts
     */
    public void spawnInitialNPCs() {
        System.out.println("Initializing NPC spawns...");

        npcTypes.values().forEach(type -> {
            try {
                SpawnableNPC.SpawnConfiguration config = type.getSpawnConfiguration();
                int count = random.nextInt(config.getMaxInstances() + 1);

                System.out.printf("Spawning %d %s(s)...\n", count, config.getNpcId());

                for (int i = 0; i < count; i++) {
                    int level = generateLevel(config);
                    NPC spawned = spawnNPC(config.getNpcId(), level);
                    if (spawned != null) {
                        System.out.printf("Spawned %s (Level %d) in %s\n",
                                spawned.getName(),
                                spawned.getLevel(),
                                spawned.getCurrentRoom().getName());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error spawning " + type.getSpawnConfiguration().getNpcId() +
                        ": " + e.getMessage());
            }
        });

        System.out.println("Initial NPC spawning complete. Active NPCs: " + activeNPCs.size());
    }

    /**
     * Spawn a specific NPC type at a given level
     */
    public NPC spawnNPC(String npcId, int level) {
        try {
            SpawnableNPC npcType = npcTypes.get(npcId);
            if (npcType == null) {
                System.err.println("Unknown NPC type: " + npcId);
                return null;
            }

            SpawnableNPC.SpawnConfiguration config = npcType.getSpawnConfiguration();

            // Validate level
            if (level < config.getMinLevel() || level > config.getMaxLevel()) {
                System.err.println("Invalid level " + level + " for " + npcId);
                return null;
            }

            // Check instance limits
            long currentCount = countActiveNPCsByType(npcId);
            if (currentCount >= config.getMaxInstances()) {
                return null;
            }

            // Find valid spawn room
            Room spawnRoom = findSpawnRoom(config.getValidRoomPatterns());
            if (spawnRoom == null) {
                System.err.println("No valid spawn room found for " + npcId);
                return null;
            }

            // Create and place NPC
            NPC npc = npcType.createInstance(level);
            npc.setCurrentRoom(spawnRoom);
            activeNPCs.put(npc.getId(), npc);

            System.out.printf("Spawned %s (ID: %s) in %s\n",
                    npc.getName(), npc.getId(), spawnRoom.getName());
            return npc;

        } catch (Exception e) {
            System.err.println("Error spawning NPC: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find a suitable room for spawning an NPC
     */
    private Room findSpawnRoom(List<String> validRoomPatterns) {
        List<Room> validRooms = new ArrayList<>();

        for (Room room : gameMap.getAllRooms()) {
            for (String pattern : validRoomPatterns) {
                if (room.getName().matches(pattern)) {
                    validRooms.add(room);
                    break;
                }
            }
        }

        if (validRooms.isEmpty()) return null;

        // First try to find a room without other NPCs
        List<Room> emptyRooms = validRooms.stream()
                .filter(room -> room.getNPCs().isEmpty())
                .collect(Collectors.toList());

        if (!emptyRooms.isEmpty()) {
            return emptyRooms.get(random.nextInt(emptyRooms.size()));
        }

        // If all rooms have NPCs, just pick a random one
        return validRooms.get(random.nextInt(validRooms.size()));
    }

    /**
     * Remove an NPC from the game
     */
    public void removeNPC(UUID npcId) {
        NPC npc = activeNPCs.remove(npcId);
        if (npc != null) {
            if (npc.getCurrentRoom() != null) {
                npc.getCurrentRoom().removeNPC(npc);
            }
            System.out.println("Removed NPC: " + npc.getName() + " (ID: " + npcId + ")");
        }
    }

    /**
     * Handle NPC respawning
     */
    public void handleRespawns() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastSpawnCheck) / 1000 < SPAWN_CHECK_INTERVAL) {
            return; // Not time to check spawns yet
        }

        lastSpawnCheck = currentTime;

        npcTypes.values().forEach(type -> {
            try {
                SpawnableNPC.SpawnConfiguration config = type.getSpawnConfiguration();
                long currentCount = countActiveNPCsByType(config.getNpcId());

                if (currentCount < config.getMaxInstances() &&
                        random.nextDouble() < config.getSpawnChance()) {

                    int level = generateLevel(config);
                    NPC spawned = spawnNPC(config.getNpcId(), level);

                    if (spawned != null) {
                        System.out.printf("Respawned %s (Level %d) in %s\n",
                                spawned.getName(),
                                spawned.getLevel(),
                                spawned.getCurrentRoom().getName());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error handling respawn for " +
                        type.getSpawnConfiguration().getNpcId() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Generate a level for a new NPC based on its configuration
     */
    private int generateLevel(SpawnableNPC.SpawnConfiguration config) {
        return random.nextInt(config.getMaxLevel() - config.getMinLevel() + 1)
                + config.getMinLevel();
    }

    /**
     * Count active NPCs of a specific type
     */
    private long countActiveNPCsByType(String npcId) {
        return activeNPCs.values().stream()
                .filter(npc -> npc.getName().startsWith(npcId))
                .count();
    }

    /**
     * Get all active NPCs
     */
    public Collection<NPC> getActiveNPCs() {
        return Collections.unmodifiableCollection(activeNPCs.values());
    }

    /**
     * Get a specific NPC by ID
     */
    public Optional<NPC> getNPC(UUID id) {
        return Optional.ofNullable(activeNPCs.get(id));
    }

    /**
     * Get all NPCs of a specific type
     */
    public List<NPC> getNPCsByType(String npcId) {
        return activeNPCs.values().stream()
                .filter(npc -> npc.getName().startsWith(npcId))
                .collect(Collectors.toList());
    }

    /**
     * Clear all NPCs (useful for shutdown/reset)
     */
    public void clearAllNPCs() {
        List<UUID> npcIds = new ArrayList<>(activeNPCs.keySet());
        npcIds.forEach(this::removeNPC);
        System.out.println("Cleared all NPCs");
    }
}