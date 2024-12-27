package com.mudgame.entities.specialization;

import com.mudgame.entities.*;
import com.mudgame.events.EventListener;
import java.util.*;

public class SpecializationMasterNPC extends NPC implements SpawnableNPC {
    private static final int SPECIALIZATION_LEVEL = 20;

    public SpecializationMasterNPC(int level, EventListener eventListener) {
        super(
                generateName(),
                "A wise figure who guides adventurers in choosing their path.",
                NPCType.QUEST,
                level,
                100,
                false,
                eventListener
        );
        initializeResponses();
    }

    private static String generateName() {
        String[] titles = {"Master", "Elder", "Sage", "Guide"};
        String[] names = {"Chen", "Silva", "Patel", "Novak", "Wong"};
        Random random = new Random();
        return titles[random.nextInt(titles.length)] + " " +
                names[random.nextInt(names.length)];
    }

    private void initializeResponses() {
        addResponse("greet", () -> String.format("%s nods thoughtfully. 'Welcome, seeker. " +
                "I can help you discover your true path in life, or perhaps grant you a family name " +
                "worthy of your deeds.'", getName()));

        addResponse("help", "Available commands: surname <name>, path (to learn about specializations), " +
                "choose <specialization>");
    }

    @Override
    public String interact(Player player, String action) {
        String[] parts = action.toLowerCase().split("\\s+", 2);
        String command = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "surname":
                return handleSurname(player, args);
            case "path":
                return handlePathInfo(player);
            case "choose":
                return handleChooseSpecialization(player, args);
            default:
                return getResponse(action);
        }
    }

    private String handleSurname(Player player, String surname) {
        if (surname.isEmpty()) {
            return "What surname would you like to take?";
        }

        if (surname.length() > 20 || !surname.matches("^[a-zA-Z'-]+$")) {
            return "That name would not be suitable. Please choose another.";
        }

        player.setLastName(surname);
        return String.format("So be it. You shall now be known as %s %s.",
                player.getFirstName(), player.getLastName());
    }

    private String handlePathInfo(Player player) {
        if (player.getLevel() < SPECIALIZATION_LEVEL) {
            return String.format("Return when you have reached level %d. " +
                    "You still have much to learn, young one.", SPECIALIZATION_LEVEL);
        }

        String currentSpec = player.getSpecialization();
        if (currentSpec != null && !currentSpec.isEmpty()) {
            return String.format("You have already chosen your path as a %s. " +
                    "Walk it with pride.", currentSpec);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("I sense you are ready to choose your path. For your class, you may become:\n\n");

        ClassSpecialization.getSpecializationsForClass(player.getCharacterClass())
                .forEach(spec -> {
                    sb.append(String.format("%s - %s\n", spec.getName(), spec.getDescription()));
                    sb.append("Special abilities:\n");
                    for (String ability : spec.getAbilities()) {
                        sb.append(String.format("  - %s\n", ability));
                    }
                    sb.append("\n");
                });

        sb.append("Choose wisely with 'choose <specialization>'.");
        return sb.toString();
    }

    private String handleChooseSpecialization(Player player, String specName) {
        if (player.getLevel() < SPECIALIZATION_LEVEL) {
            return String.format("You must reach level %d before choosing a specialization.",
                    SPECIALIZATION_LEVEL);
        }

        if (player.getSpecialization() != null && !player.getSpecialization().isEmpty()) {
            return "You have already chosen your path.";
        }

        Optional<ClassSpecialization> spec = ClassSpecialization.fromName(specName);
        if (spec.isEmpty()) {
            return "I do not know of such a path. Use 'path' to see your options.";
        }

        if (spec.get().getBaseClass() != player.getCharacterClass()) {
            return "That path is not available to one of your class.";
        }

        player.setSpecialization(spec.get().getName());
        broadcastToRoom(String.format("%s has chosen the path of the %s!",
                player.getFullName(), spec.get().getName()));

        return String.format("Your path is chosen. Walk proudly as a %s.\n\n%s",
                spec.get().getName(), spec.get().getDescription());
    }

    @Override
    public void onTick() {
        // Specialization masters don't need tick behavior
    }

    @Override
    public void onDeath(Player killer) {
        // Specialization masters cannot be killed
    }

    protected void broadcastToRoom(String message) {
        Room currentRoom = getCurrentRoom();
        if (currentRoom != null && eventListener != null) {
            eventListener.onEvent("room", currentRoom.getName(), message);
        }
    }

    @Override
    public SpawnConfiguration getSpawnConfiguration() {
        return new SpawnConfiguration(
                "specialization_master",
                1,  // Only one per location
                -1, // No respawn (permanent)
                Arrays.asList("Academy", "Training Grounds", "Guild Hall"),
                1, 1,  // Level range (always level 1)
                1.0    // 100% spawn chance
        );
    }

    @Override
    public NPC createInstance(int level) {
        return new SpecializationMasterNPC(level, eventListener);
    }
}