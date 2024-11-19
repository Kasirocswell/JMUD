package com.mudgame.server.resources;

import com.mudgame.api.commands.CommandResult;
import com.mudgame.api.commands.GameCommand;
import com.mudgame.entities.*;
import com.mudgame.server.core.GameState;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

@Path("/game")
@Produces(MediaType.APPLICATION_JSON)
public class GameResource {
    private final GameState gameState;

    public GameResource(GameState gameState) {
        this.gameState = gameState;
    }

    // Character Management Endpoints
    @GET
    @Path("/characters/{ownerId}")
    public Response getCharacters(@PathParam("ownerId") String ownerId) {
        try {
            List<Player> characters = gameState.getPlayersByOwnerId(ownerId);
            return Response.ok(characters).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/characters")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCharacter(CreateCharacterRequest request) {
        try {
            Player player = gameState.createCharacter(
                    request.ownerId,
                    request.firstName,
                    request.lastName,
                    request.race,
                    request.characterClass,
                    request.attributes
            );
            return Response.ok(player).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/characters/get/{characterId}")
    public Response getCharacter(@PathParam("characterId") String characterId) {
        Player player = gameState.getPlayer(characterId);
        if (player != null) {
            return Response.ok(player).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Character not found"))
                .build();
    }

    @POST
    @Path("/attributes/roll")
    public Response rollAttributes() {
        try {
            Map<Attributes, Integer> rolls = new EnumMap<>(Attributes.class);
            Random random = new Random();

            // Roll for each attribute (3d6)
            for (Attributes attr : Attributes.values()) {
                // Roll 4d6, drop lowest
                int[] diceRolls = new int[4];
                for (int i = 0; i < 4; i++) {
                    diceRolls[i] = random.nextInt(6) + 1;
                }
                // Find the lowest roll
                int lowestRoll = Arrays.stream(diceRolls).min().getAsInt();
                // Sum all dice except the lowest
                int total = Arrays.stream(diceRolls).sum() - lowestRoll;

                rolls.put(attr, total);
            }

            // Validate minimum total
            int totalAttributes = rolls.values().stream().mapToInt(Integer::intValue).sum();
            if (totalAttributes < 65) {  // Minimum total requirement
                return rollAttributes();  // Recursively try again if total is too low
            }

            return Response.ok(new AttributeRollResponse(rolls)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    // Game Session Management Endpoints
    @POST
    @Path("/join")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response joinGame(JoinRequest request) {
        try {
            System.out.println("Join request received - playerId: " + request.getPlayerId() + ", userId: " + request.getUserId());
            Player player = gameState.joinGame(request.getUserId(), request.getPlayerId());
            if (player == null) {
                System.out.println("Player not found in game state");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Character not found"))
                        .build();
            }
            return Response.ok(new JoinResponse(player.getId(),
                    "Welcome back, " + player.getFullName() + "!")).build();
        } catch (IllegalStateException e) {
            System.out.println("Error joining game: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/leave/{playerId}")
    public Response leaveGame(@PathParam("playerId") String playerId) {
        try {
            gameState.leaveGame(playerId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    // Game Command Endpoint
    @POST
    @Path("/command")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeCommand(CommandRequest request) {
        Player player = gameState.getPlayer(request.getPlayerId());
        if (player == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Player not found"))
                    .build();
        }

        if (!player.isOnline()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Player is not online"))
                    .build();
        }

        String[] parts = request.getCommand().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

        Optional<GameCommand> command = gameState.getCommandRegistry().getCommand(commandName);
        if (command.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Unknown command: " + commandName))
                    .build();
        }

        CommandResult result = command.get().execute(player, args);
        return Response.ok(new CommandResponse(result)).build();
    }

    // Request/Response Classes
    public static class CreateCharacterRequest {
        private String ownerId;
        private String firstName;
        private String lastName;
        private Race race;
        private CharacterClass characterClass;
        private Map<Attributes, Integer> attributes;

        // Getters and setters
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public Race getRace() { return race; }
        public void setRace(Race race) { this.race = race; }
        public CharacterClass getCharacterClass() { return characterClass; }
        public void setCharacterClass(CharacterClass characterClass) { this.characterClass = characterClass; }
        public Map<Attributes, Integer> getAttributes() { return attributes; }
        public void setAttributes(Map<Attributes, Integer> attributes) { this.attributes = attributes; }
    }

    public static class JoinRequest {
        private String userId;
        private String playerId;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
    }

    public static class JoinResponse {
        private final String playerId;
        private final String message;

        public JoinResponse(String playerId, String message) {
            this.playerId = playerId;
            this.message = message;
        }

        public String getPlayerId() { return playerId; }
        public String getMessage() { return message; }
    }

    public static class CommandRequest {
        private String playerId;
        private String command;

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
    }

    public static class CommandResponse {
        private final CommandResult result;

        public CommandResponse(CommandResult result) {
            this.result = result;
        }

        public CommandResult getResult() { return result; }
    }

    public static class AttributeRollResponse {
        private final Map<Attributes, Integer> rolls;

        public AttributeRollResponse(Map<Attributes, Integer> rolls) {
            this.rolls = rolls;
        }

        public Map<Attributes, Integer> getRolls() {
            return rolls;
        }
    }

    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
    }
}