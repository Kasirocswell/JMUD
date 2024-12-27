package com.mudgame.entities.enemies;

import com.mudgame.events.EventListener;
import com.mudgame.entities.*;
import java.util.*;

public class SecurityBotNPC extends EnemyNPC implements SpawnableNPC {
    private static final Random random = new Random();
    private static final int MAX_ENERGY = 100;
    private static final double ENERGY_DRAIN_RATE = 1.0;
    private static final double ENERGY_RECHARGE_RATE = 2.0;
    private static final long SCAN_INTERVAL = 5000; // 5 seconds between scans
    private static final long PATROL_MOVE_INTERVAL = 5000; // 5 seconds between moves
    private static final long IDLE_MESSAGE_COOLDOWN = 10000; // 10 seconds between idle messages
    private static final int ENGAGE_RANGE = 1; // How many rooms away the bot will pursue targets
    private static final int BASE_CREDIT_DROP = 50;

    private int energyLevel;
    private int alertLevel; // 0-3: 0=normal, 1=suspicious, 2=alert, 3=combat
    private long lastScanTime;
    private Set<UUID> knownHostiles;
    private long lastIdleMessage;
    private List<Room> patrolRoute;
    private int currentPatrolPoint;
    private long lastPatrolMove;

    public SecurityBotNPC(int level, EventListener eventListener) {
        super(
                "Security Bot MK-" + (level + random.nextInt(999)),
                generateDescription(level),
                level,
                100 + (level * 20),  // maxHealth
                10 + (level * 2),    // attackPower - scales with level
                3,                   // attackSpeed - attacks every 3 ticks
                eventListener
        );

        this.energyLevel = MAX_ENERGY;
        this.alertLevel = 0;
        this.lastScanTime = System.currentTimeMillis();
        this.lastIdleMessage = System.currentTimeMillis();
        this.knownHostiles = new HashSet<>();
        this.patrolRoute = new ArrayList<>();
        this.currentPatrolPoint = 0;

        initializeSecurityBotAttributes();
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

    private void initializeSecurityBotAttributes() {
        // Security bots get enhanced speed and perception
        attributes.merge(Attributes.SPEED, 2, Integer::sum);
        attributes.merge(Attributes.PERCEPTION, 3, Integer::sum);

        // Scale combat attributes with level
        int levelBonus = getLevel() / 2;
        attributes.merge(Attributes.STRENGTH, levelBonus, Integer::sum);
        attributes.merge(Attributes.CONSTITUTION, levelBonus, Integer::sum);

        // Update combat stats
        combatState.updateAttackCooldown(attributes.get(Attributes.SPEED));
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
    public void onTick() {
        if (isDead()) return;

        updateEnergy();
        performRoomScan();
        updateAlertStatus();

        // Handle combat behavior
        if (combatState.isInCombat()) {
            handleCombatBehavior();
        } else {
            handlePatrolBehavior();
        }

        // Add random idle messages when not hostile
        long currentTime = System.currentTimeMillis();
        if (!isHostile() &&
                energyLevel > 20 &&
                (currentTime - lastIdleMessage) >= IDLE_MESSAGE_COOLDOWN &&
                random.nextInt(100) < 5) {

            broadcastToRoom(getRandomIdleMessage());
            lastIdleMessage = currentTime;
        }
    }

    private void handleCombatBehavior() {
        if (!combatState.canAttack() || energyLevel < 10) return;

        Room currentRoom = getCurrentRoom();
        if (currentRoom == null) return;

        // Get current target or find new one
        CombatEntity target = null;
        if (combatState.getCurrentTarget() != null) {
            target = findEntityById(combatState.getCurrentTarget());
        }

        // If current target is invalid, find new target
        if (target == null || target.isDead() || target.getCurrentRoom() != currentRoom) {
            Optional<Player> newTarget = selectTarget(currentRoom.getPlayers());
            if (newTarget.isPresent()) {
                target = newTarget.get();
                combatState.enterCombat(target.getId());
            } else {
                // No valid targets, exit combat
                combatState.exitCombat();
                return;
            }
        }

        // Attack target
        if (target != null && canAttack(target)) {
            attack(target);
            String attackMessage = generateAttackMessage(target);
            broadcastToRoom(attackMessage);
        }
    }

    private void handlePatrolBehavior() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPatrolMove < PATROL_MOVE_INTERVAL) return;

        // If no patrol route, create one
        if (patrolRoute.isEmpty() && getCurrentRoom() != null) {
            generatePatrolRoute();
        }

        // Move to next patrol point if we have a route
        if (!patrolRoute.isEmpty()) {
            Room nextRoom = patrolRoute.get(currentPatrolPoint);
            if (getCurrentRoom() != nextRoom) {
                moveToRoom(nextRoom);
            }
            currentPatrolPoint = (currentPatrolPoint + 1) % patrolRoute.size();
            lastPatrolMove = currentTime;
        }
    }

    private void generatePatrolRoute() {
        Room currentRoom = getCurrentRoom();
        if (currentRoom == null) return;

        patrolRoute.clear();
        patrolRoute.add(currentRoom);

        // Add connected security-related rooms
        for (Map.Entry<Direction, Room> exit : currentRoom.getExits().entrySet()) {
            Room connectedRoom = exit.getValue();
            if (isSecurityArea(connectedRoom)) {
                patrolRoute.add(connectedRoom);
            }
        }
    }

    private boolean isSecurityArea(Room room) {
        String roomName = room.getName().toLowerCase();
        return roomName.contains("security") ||
                roomName.contains("restricted") ||
                roomName.contains("checkpoint");
    }

    private void moveToRoom(Room targetRoom) {
        if (getCurrentRoom() == null || targetRoom == null) return;

        Room oldRoom = getCurrentRoom();
        setCurrentRoom(targetRoom);

        // Broadcast exit message to old room
        broadcastToRoom(getName() + " leaves on patrol.", oldRoom);
        // Broadcast entry message to new room
        broadcastToRoom(getName() + " enters on patrol.");
    }

    private Optional<Player> selectTarget(Collection<Player> possibleTargets) {
        return possibleTargets.stream()
                .filter(p -> !p.isDead() &&
                        (knownHostiles.contains(p.getId()) || alertLevel >= 2))
                .min((p1, p2) -> {
                    // Prioritize known hostiles
                    boolean h1 = knownHostiles.contains(p1.getId());
                    boolean h2 = knownHostiles.contains(p2.getId());
                    if (h1 != h2) return h1 ? -1 : 1;

                    // Then prioritize lower health targets
                    return Integer.compare(p1.getHealth(), p2.getHealth());
                });
    }

    private String generateAttackMessage(CombatEntity target) {
        String[] messages = {
                "fires its stun blasters at",
                "unleashes a barrage of energy bolts at",
                "activates its combat protocols against",
                "engages its weapon systems targeting",
                "deploys tactical countermeasures against",
                "initiates neutralization sequence on"
        };
        return getName() + " " + messages[random.nextInt(messages.length)] +
                " " + target.getName() + "!";
    }

    private String getRandomIdleMessage() {
        String[] idleMessages = {
                "performs a routine sensor sweep of the area.",
                "emits a low humming sound as it runs diagnostics.",
                "adjusts its positioning servos with a quiet whir.",
                "scans the surroundings with pulsing blue sensors.",
                "rotates slowly, monitoring the area.",
                "projects a holographic security grid briefly.",
                "recalibrates its internal systems.",
                "emits a series of soft beeping sounds.",
                "runs a quick systems check, status lights blinking.",
                "patrols the perimeter of the room."
        };
        return getName() + " " + idleMessages[random.nextInt(idleMessages.length)];
    }

    private void updateEnergy() {
        if (combatState.isInCombat()) {
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
                alertLevel = 1;
                broadcastToRoom(getName() + " is fully recharged and resumes patrolling.");
            }
        }
    }

    private void performRoomScan() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime > SCAN_INTERVAL) {
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

    // Status response methods
    private String getStatusResponse() {
        return String.format("Status Report - %s\nPower: %d%%\nAlert Level: %s\nSystems: %s",
                getName(),
                energyLevel,
                getAlertLevelString(),
                getHealth() > getMaxHealth() * 0.5 ? "Nominal" : "Warning");
    }

    private String getDiagnosticResponse() {
        return String.format("Diagnostic Results:\nModel: Security Bot MK-%d\n" +
                        "Power Systems: %d%%\nCombat Systems: %d%%\nSensor Array: %s\nThreat Level: %s",
                getLevel(),
                energyLevel,
                (getHealth() * 100) / getMaxHealth(),
                alertLevel > 0 ? "ACTIVE" : "Standard",
                getAlertLevelString());
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
                getAlertLevelString(),
                knownHostiles.size(),
                isHostile() ? "ENGAGE" : "MONITOR");
    }

    private String getAlertLevelString() {
        switch (alertLevel) {
            case 0: return "Normal";
            case 1: return "Elevated";
            case 2: return "High Alert";
            case 3: return "Combat Ready";
            default: return "Unknown";
        }
    }

    @Override
    public void onDeath(Player killer) {
        super.onDeath(killer);

        // Calculate credit drop based on level
        int creditDrop = BASE_CREDIT_DROP + (getLevel() * 10) + random.nextInt(100);
        killer.setCredits(killer.getCredits() + creditDrop);

        broadcastToRoom(String.format("%s emits a final warning tone before powering down permanently. " +
                "You salvage %d credits worth of parts.", getName(), creditDrop));
    }

    private CombatEntity findEntityById(UUID id) {
        Room room = getCurrentRoom();
        if (room != null) {
            // Check players first
            Optional<Player> player = room.getPlayers().stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst();
            if (player.isPresent()) return player.get();

            // Check NPCs
            Optional<NPC> npc = room.getNPCs().stream()
                    .filter(n -> n.getId().equals(id))
                    .findFirst();
            if (npc.isPresent()) return npc.get();
        }
        return null;
    }

    @Override
    public SpawnConfiguration getSpawnConfiguration() {
        return new SpawnConfiguration(
                "security_bot",
                3, // Max 3 per area
                300, // 5 minute respawn
                Arrays.asList(
                        "Shibuya Security Post",
                        "Security Checkpoint",
                        "Restricted Area",
                        ".*Security.*",
                        ".*Restricted.*"
                ),
                1, 10, // Level range
                0.8 // 80% spawn chance
        );
    }

    @Override
    public NPC createInstance(int level) {
        return new SecurityBotNPC(level, eventListener);
    }

    private void broadcastToRoom(String message, Room room) {
        if (room != null && eventListener != null) {
            eventListener.onEvent("room", room.getName(), message);
        }
    }

    protected void broadcastToRoom(String message) {
        broadcastToRoom(message, getCurrentRoom());
    }
}