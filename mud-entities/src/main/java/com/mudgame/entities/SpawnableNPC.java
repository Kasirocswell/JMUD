package com.mudgame.entities;

import java.util.List;

/**
 * Interface for NPCs that defines their spawn behavior.
 * Every NPC type will implement this to control its own spawn logic.
 */
public interface SpawnableNPC {
    /**
     * Get spawn configuration for this NPC type
     */
    SpawnConfiguration getSpawnConfiguration();

    /**
     * Factory method to create a new instance of this NPC
     * @param level The level to spawn the NPC at
     * @return A new instance of the NPC
     */
    NPC createInstance(int level);

    /**
     * Configuration class that encapsulates spawn behavior
     */
    class SpawnConfiguration {
        private final String npcId;
        private final int maxInstances;
        private final int respawnTime;
        private final List<String> validRoomPatterns;
        private final int minLevel;
        private final int maxLevel;
        private final double spawnChance; // Percentage chance to spawn when eligible

        public SpawnConfiguration(
                String npcId,
                int maxInstances,
                int respawnTime,
                List<String> validRoomPatterns,
                int minLevel,
                int maxLevel,
                double spawnChance) {
            this.npcId = npcId;
            this.maxInstances = maxInstances;
            this.respawnTime = respawnTime;
            this.validRoomPatterns = validRoomPatterns;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.spawnChance = spawnChance;
        }

        public String getNpcId() { return npcId; }
        public int getMaxInstances() { return maxInstances; }
        public int getRespawnTime() { return respawnTime; }
        public List<String> getValidRoomPatterns() { return validRoomPatterns; }
        public int getMinLevel() { return minLevel; }
        public int getMaxLevel() { return maxLevel; }
        public double getSpawnChance() { return spawnChance; }
    }
}