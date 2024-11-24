package com.mudgame.entities.npcs;

import com.mudgame.entities.*;
import com.mudgame.entities.enemies.EnemyNPC;

import java.util.*;

public class SecurityBotNPC extends EnemyNPC implements SpawnableNPC {
    private static final Random random = new Random();
    private static final int MAX_ENERGY = 100;
    private static final double ENERGY_DRAIN_RATE = 1.0; // Energy lost per tick when hostile
    private static final double ENERGY_RECHARGE_RATE = 2.0; // Energy gained per tick when idle

    private int energyLevel;
    private int alertLevel; // 0-3: 0=normal, 1=suspicious, 2=alert, 3=combat
    private long lastScanTime;
    private Set<UUID> knownHostiles;

    public SecurityBotNPC(int level) {
        super(
                "Security Bot MK-" + (level + random.nextInt(999)),
                generateDescription(level),
                level,
                100 + (level * 20),  // Health scales with level
                10 + (level * 2),    // Attack power scales with level
                3                    // Attacks every 3 ticks
        );

        this.energyLevel = MAX_ENERGY;
        this.alertLevel = 0;
        this.lastScanTime = System.currentTimeMillis();
        this.knownHostiles = new HashSet<>();
        initializeResponses();
    }

    private static String generateDescription(int level) {
        StringBuilder desc = new StringBuilder();
        desc.append("A heavily armored security robot with ");

        // Appearance varies by level
        if (level <= 2) {
            desc.append("basic sensors and lightweight plating. ");
        } else if (level <= 5) {
            desc.append("advanced optical sensors and reinforced plating. ");
        } else {
            desc.append("military-grade targeting systems and heavy combat plating. ");
        }

        desc.append("Its chassis bears the station security insignia and warning symbols. ");
        desc.append("Model designation is clearly visible on its surface.");

        return desc.toString();
    }

    private void initializeResponses() {
        // Basic static responses
        addResponse("hello", "The security bot's sensors focus on you momentarily before emitting a low acknowledgment tone.");
        addResponse("help", "The security bot displays a scrolling message: 'FOR SECURITY ASSISTANCE, REMAIN IN PLACE.'");

        // Dynamic responses using lambdas
        addResponse("status", () -> getStatusResponse());
        addResponse("identify", () -> "Security Unit " + getName() + " - Authorization Level " + getLevel());
        addResponse("diagnostic", () -> getDiagnosticResponse());
        addResponse("systems", () -> getSystemsResponse());
        addResponse("stand down", () -> getStandDownResponse());
        addResponse("threat", () -> getThreatResponse());
    }

    @Override
    public SpawnConfiguration getSpawnConfiguration() {
        return new SpawnConfiguration(
                "security_bot",          // Unique ID for this NPC type
                3,                       // Max 3 instances at once
                300,                     // Respawn time in seconds
                Arrays.asList(           // Valid spawn locations
                        "Shibuya Crossing",
                        "Shibuya Security Post",
                        ".*Security.*",      // Any room with "Security" in the name
                        ".*Maintenance.*",   // Any room with "Maintenance" in the name
                        ".*Restricted.*",    // Any restricted areas
                        ".*Cargo Bay.*"      // Any cargo bays
                ),
                1,                      // Min level
                10,                     // Max level
                0.1                     // 10% spawn chance when eligible
        );
    }

    @Override
    public NPC createInstance(int level) {
        return new SecurityBotNPC(level);
    }

    @Override
    public void onTick() {
        if (isDead()) return;

        // Handle energy management
        updateEnergy();

        // Perform periodic room scan
        performRoomScan();

        // Update alert status
        updateAlertStatus();

        // Handle combat if hostile
        if (isHostile()) {
            super.onTick(); // Handle basic enemy behavior
        }
    }

    private void updateEnergy() {
        if (isHostile()) {
            energyLevel = Math.max(0, energyLevel - (int)ENERGY_DRAIN_RATE);
            if (energyLevel == 0) {
                setHostile(false);
                setState(NPCState.IDLE);
                alertLevel = 0;
                broadcastToRoom(getName() + " powers down, entering emergency conservation mode.");
            }
        } else if (energyLevel < MAX_ENERGY) {
            energyLevel = Math.min(MAX_ENERGY, energyLevel + (int)ENERGY_RECHARGE_RATE);
            if (energyLevel == MAX_ENERGY && alertLevel > 0) {
                alertLevel = 1; // Return to suspicious state when fully charged
            }
        }
    }

    private void performRoomScan() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime > 5000) { // Scan every 5 seconds
            Room room = getCurrentRoom();
            if (room != null) {
                // Look for suspicious players
                room.getPlayers().forEach(this::assessThreat);
            }
            lastScanTime = currentTime;
        }
    }

    private void assessThreat(Player player) {
        // TODO: Implement proper threat assessment
        // For now, just track known hostiles
        if (knownHostiles.contains(player.getId())) {
            alertLevel = Math.max(alertLevel, 2);
            setHostile(true);
        }
    }

    private void updateAlertStatus() {
        Room room = getCurrentRoom();
        if (room == null) return;

        // Reduce alert level over time if no threats
        if (alertLevel > 0 && room.getPlayers().isEmpty()) {
            if (random.nextInt(100) < 10) { // 10% chance per tick
                alertLevel = Math.max(0, alertLevel - 1);
            }
        }
    }

    @Override
    public String interact(Player player, String action) {
        if (isDead()) {
            return "The security bot lies motionless, occasional sparks shooting from its damaged circuits.";
        }

        switch (action.toLowerCase()) {
            case "scan":
                return performScan(player);
            case "hack":
                return attemptHack(player);
            case "deactivate":
                return attemptDeactivate(player);
            case "reboot":
                return attemptReboot(player);
            default:
                return getResponse(action);
        }
    }

    private String performScan(Player player) {
        StringBuilder result = new StringBuilder();
        result.append("Security Scan Results:\n");
        result.append(String.format("Unit: %s\n", getName()));
        result.append(String.format("Energy Level: %d%%\n", energyLevel));
        result.append(String.format("Alert Status: %s\n", getAlertStatusString()));
        result.append(String.format("Threat Assessment: %s\n",
                knownHostiles.contains(player.getId()) ? "HOSTILE" : "Neutral"));

        return result.toString();
    }

    private String attemptHack(Player player) {
        // TODO: Implement proper hacking mechanics with skill checks
        setHostile(true);
        alertLevel = 3;
        knownHostiles.add(player.getId());
        return "ALERT! Unauthorized access detected! Engaging defense protocols.";
    }

    private String attemptDeactivate(Player player) {
        if (alertLevel > 0) {
            setHostile(true);
            return "ERROR: Cannot deactivate while security protocols are active!";
        }
        // TODO: Implement proper deactivation mechanics
        return "Access denied. Security authorization required.";
    }

    private String attemptReboot(Player player) {
        if (energyLevel < 20) {
            energyLevel = MAX_ENERGY / 2;
            alertLevel = 0;
            setHostile(false);
            return getName() + " performs an emergency reboot, returning to standby mode.";
        }
        return "ERROR: Reboot only available in low power state.";
    }

    private String getStatusResponse() {
        return String.format("Status Report - %s\nPower: %d%%\nAlert Level: %s\nSystems: %s",
                getName(),
                energyLevel,
                getAlertStatusString(),
                getHealth() > getMaxHealth() * 0.5 ? "Nominal" : "Warning");
    }

    private String getDiagnosticResponse() {
        return String.format("Diagnostic Results:\nModel: Security Bot MK-%d\n" +
                        "Power Systems: %d%%\nCombat Systems: %d%%\nSensor Array: %s\nThreat Level: %s",
                getLevel(),
                energyLevel,
                (getHealth() * 100) / getMaxHealth(),
                alertLevel > 0 ? "ACTIVE" : "Standard",
                getAlertStatusString());
    }

    private String getSystemsResponse() {
        return String.format("Systems Status:\nMain Power: %s\nWeapon Systems: %s\n" +
                        "Defense Grid: %s\nSensor Array: %s",
                energyLevel > 50 ? "ONLINE" : "WARNING",
                isHostile() ? "ENGAGED" : "STANDBY",
                getHealth() > getMaxHealth() * 0.3 ? "ACTIVE" : "CRITICAL",
                alertLevel > 0 ? "ENHANCED" : "NORMAL");
    }

    private String getStandDownResponse() {
        if (alertLevel <= 1) {
            alertLevel = 0;
            setHostile(false);
            return getName() + " acknowledges command, returning to standard patrol mode.";
        }
        return "Unable to comply - threat level too high.";
    }

    private String getThreatResponse() {
        return String.format("Current Threat Assessment:\nAlert Level: %s\n" +
                        "Known Hostiles: %d\nResponse Protocol: %s",
                getAlertStatusString(),
                knownHostiles.size(),
                isHostile() ? "ENGAGE" : "MONITOR");
    }

    private String getAlertStatusString() {
        switch (alertLevel) {
            case 0: return "Normal";
            case 1: return "Elevated";
            case 2: return "High Alert";
            case 3: return "Combat Ready";
            default: return "Unknown";
        }
    }

    private void broadcastToRoom(String message) {
        // TODO: Implement room broadcast system
        System.out.println(message); // Temporary implementation
    }

    // Getters
    public int getEnergyLevel() { return energyLevel; }
    public int getAlertLevel() { return alertLevel; }

    @Override
    public void onDeath(Player killer) {
        super.onDeath(killer);

        // Drop bot-specific loot
        int creditDrop = 50 + (getLevel() * 10) + random.nextInt(100);
        killer.setCredits(killer.getCredits() + creditDrop);

        // TODO: Add specific loot drops with proper loot tables

        broadcastToRoom(String.format("%s emits a final warning tone before powering down permanently. " +
                "You salvage %d credits worth of parts.", getName(), creditDrop));
    }
}