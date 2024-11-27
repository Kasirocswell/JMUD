package com.mudgame.entities.npcs;

import com.mudgame.events.EventListener;
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

    private final EventListener eventListener; // Event listener for broadcasting events

    public SecurityBotNPC(int level, EventListener eventListener) {
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
        this.eventListener = eventListener; // Save the event listener reference
        initializeResponses();
    }

    private static String generateDescription(int level) {
        StringBuilder desc = new StringBuilder();
        desc.append("A heavily armored security robot with ");

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
                "security_bot",
                3,
                300,
                Arrays.asList(
                        "Shibuya Crossing",
                        "Shibuya Security Post",
                        ".*Security.*",
                        ".*Maintenance.*",
                        ".*Restricted.*",
                        ".*Cargo Bay.*"
                ),
                1,
                10,
                0.1
        );
    }

    @Override
    public NPC createInstance(int level) {
        return new SecurityBotNPC(level, eventListener);
    }

    @Override
    public void onTick() {
        if (isDead()) return;

        updateEnergy();
        performRoomScan();
        updateAlertStatus();

        if (isHostile()) {
            super.onTick();
        }
    }

    private void updateEnergy() {
        if (isHostile()) {
            energyLevel = Math.max(0, energyLevel - (int) ENERGY_DRAIN_RATE);
            if (energyLevel == 0) {
                setHostile(false);
                setState(NPCState.IDLE);
                alertLevel = 0;
                broadcastToRoom(getName() + " powers down, entering emergency conservation mode.");
            }
        } else if (energyLevel < MAX_ENERGY) {
            energyLevel = Math.min(MAX_ENERGY, energyLevel + (int) ENERGY_RECHARGE_RATE);
            if (energyLevel == MAX_ENERGY && alertLevel > 0) {
                alertLevel = 1; // Return to suspicious state when fully charged
                broadcastToRoom(getName() + " is fully recharged and resumes patrolling.");
            }
        }
    }

    private void performRoomScan() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime > 5000) {
            Room room = getCurrentRoom();
            if (room != null) {
                room.getPlayers().forEach(this::assessThreat);
            }
            lastScanTime = currentTime;
        }
    }

    private void assessThreat(Player player) {
        if (knownHostiles.contains(player.getId())) {
            alertLevel = Math.max(alertLevel, 2);
            setHostile(true);
        }
    }

    private void updateAlertStatus() {
        Room room = getCurrentRoom();
        if (room == null) return;

        if (alertLevel > 0 && room.getPlayers().isEmpty()) {
            if (random.nextInt(100) < 10) {
                alertLevel = Math.max(0, alertLevel - 1);
            }
        }
    }

    private void broadcastToRoom(String message) {
        Room room = getCurrentRoom();
        if (room != null) {
            eventListener.onEvent("room", room.getId(), message);
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
        return String.format(
                "Security Scan Results:\nUnit: %s\nEnergy Level: %d%%\nAlert Status: %s\nThreat Assessment: %s",
                getName(),
                energyLevel,
                getAlertStatusString(),
                knownHostiles.contains(player.getId()) ? "HOSTILE" : "Neutral"
        );
    }

    private String attemptHack(Player player) {
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
            case 0:
                return "Normal";
            case 1:
                return "Elevated";
            case 2:
                return "High Alert";
            case 3:
                return "Combat Ready";
            default:
                return "Unknown";
        }
    }

    @Override
    public void onDeath(Player killer) {
        super.onDeath(killer);

        int creditDrop = 50 + (getLevel() * 10) + random.nextInt(100);
        killer.setCredits(killer.getCredits() + creditDrop);

        broadcastToRoom(String.format("%s emits a final warning tone before powering down permanently. " +
                "You salvage %d credits worth of parts.", getName(), creditDrop));
    }
}
