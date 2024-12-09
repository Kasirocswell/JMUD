package com.mudgame.server.resources;

import com.mudgame.api.commands.CommandResult;
import com.mudgame.api.commands.GameCommand;
import com.mudgame.entities.Attributes;
import com.mudgame.entities.CharacterClass;
import com.mudgame.entities.ClassSpecialization;
import com.mudgame.entities.GameMap;
import com.mudgame.entities.Player;
import com.mudgame.entities.Race;
import com.mudgame.entities.Room;
import com.mudgame.server.core.GameState;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Path("/game")
@Produces(MediaType.APPLICATION_JSON)
public class GameResource {
    private final GameState gameState;
    private final DataSource dataSource;

    public GameResource(GameState gameState) {
        this.gameState = gameState;
        this.dataSource = gameState.getDataSource();
    }

    @GET
    @Path("/characters/{ownerId}")
    public Response getCharacters(@PathParam("ownerId") UUID ownerId) {
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
            // Get room name, use starting room if none provided
            String roomName = request.getCurrentRoomName();
            if (roomName == null) {
                roomName = gameState.getGameMap().getStartingRoom().getName();
            }

            // Create the player with the specified ID
            Player player = gameState.createCharacter(
                    request.getId(),
                    request.getOwnerId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getRace(),
                    request.getCharacterClass(),
                    request.getAttributes()
            );

            // Set location using room name
            player.setRoomName(roomName);
            Room room = gameState.getGameMap().getRoomByName(roomName);
            if (room != null) {
                player.setCurrentRoom(room);
                room.addPlayer(player);
            } else {
                // If room not found, use starting room as fallback
                Room startingRoom = gameState.getGameMap().getStartingRoom();
                player.setRoomName(startingRoom.getName());
                player.setCurrentRoom(startingRoom);
                startingRoom.addPlayer(player);
                System.out.println("Room not found: " + roomName + ", placing player in starting room: " + startingRoom.getName());
            }

            return Response.ok(player).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error creating character: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/characters/get/{characterId}")
    public Response getCharacter(@PathParam("characterId") UUID characterId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT c.*, " +
                             "c.first_name, c.last_name, c.race, c.class, c.credits, " +
                             "c.room_name, c.level, c.health, c.max_health, " +
                             "c.energy, c.max_energy, c.last_seen, c.specialization, " +
                             "c.owner_id " +
                             "FROM character c WHERE c.id = ?")) {

            stmt.setObject(1, characterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Player player = new Player(
                        characterId,
                        UUID.fromString(rs.getString("owner_id")),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        Race.valueOf(rs.getString("race")),
                        CharacterClass.valueOf(rs.getString("class")),
                        null,  // inventory will be loaded separately
                        null,  // equipment will be loaded separately
                        rs.getInt("credits"),
                        rs.getString("room_name"),
                        rs.getInt("level"),
                        rs.getInt("health"),
                        rs.getInt("max_health"),
                        rs.getInt("energy"),
                        rs.getInt("max_energy"),
                        rs.getLong("last_seen"),
                        rs.getString("specialization")  // Load specialization
                );

                return Response.ok(player).build();
            }

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Character not found"))
                    .build();

        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Database error: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/attributes/roll")
    public Response rollAttributes() {
        try {
            Map<Attributes, Integer> rolls = new EnumMap<>(Attributes.class);
            Random random = new Random();

            // Roll for each attribute (4d6, drop lowest)
            for (Attributes attr : Attributes.values()) {
                int[] diceRolls = new int[4];
                for (int i = 0; i < 4; i++) {
                    diceRolls[i] = random.nextInt(6) + 1;
                }
                int lowestRoll = Arrays.stream(diceRolls).min().getAsInt();
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

            // Get room name for welcome message
            String locationName = player.getCurrentRoom() != null ?
                    player.getCurrentRoom().getName() :
                    "an unknown location";

            // Create character name based on whether last name exists
            String characterName = player.getLastName() != null && !player.getLastName().trim().isEmpty() ?
                    String.format("%s %s", player.getFirstName(), player.getLastName()) :
                    player.getFirstName();

            // Create a detailed welcome message
            String welcomeMessage = String.format(
                    "Welcome back, %s! You find yourself in %s. " +
                            "[Health: %d/%d | Energy: %d/%d | Credits: %d]",
                    characterName,
                    locationName,
                    player.getHealth(),
                    player.getMaxHealth(),
                    player.getEnergy(),
                    player.getMaxEnergy(),
                    player.getCredits()
            );

            return Response.ok(new JoinResponse(player.getId(), welcomeMessage)).build();
        } catch (IllegalStateException e) {
            System.out.println("Error joining game: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/leave/{playerId}")
    public Response leaveGame(@PathParam("playerId") UUID playerId) {
        try {
            gameState.leaveGame(playerId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/characters/{characterId}")
    public Response deleteCharacter(@PathParam("characterId") UUID characterId) {
        try {
            Player player = gameState.getPlayer(characterId);
            if (player == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Character not found"))
                        .build();
            }

            // Remove from game state
            gameState.unloadPlayer(characterId);

            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error removing character from game state: " + e.getMessage()))
                    .build();
        }
    }

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
        private UUID id;
        private UUID ownerId;
        private String firstName;
        private String lastName;
        private Race race;
        private CharacterClass characterClass;
        private Map<Attributes, Integer> attributes;
        private String currentRoomName;

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getOwnerId() { return ownerId; }
        public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
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
        public String getCurrentRoomName() { return currentRoomName; }
        public void setCurrentRoomName(String currentRoomName) { this.currentRoomName = currentRoomName; }
    }

    public static class JoinRequest {
        private UUID userId;
        private UUID playerId;

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public UUID getPlayerId() { return playerId; }
        public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    }

    public static class JoinResponse {
        private final UUID playerId;
        private final String message;

        public JoinResponse(UUID playerId, String message) {
            this.playerId = playerId;
            this.message = message;
        }

        public UUID getPlayerId() { return playerId; }
        public String getMessage() { return message; }
    }

    public static class CommandRequest {
        private UUID playerId;
        private String command;

        public UUID getPlayerId() { return playerId; }
        public void setPlayerId(UUID playerId) { this.playerId = playerId; }
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