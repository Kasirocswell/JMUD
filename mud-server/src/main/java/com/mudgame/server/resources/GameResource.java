package com.mudgame.server.resources;

import com.mudgame.api.commands.CommandResult;
import com.mudgame.api.commands.GameCommand;
import com.mudgame.entities.Player;
import com.mudgame.server.core.GameState;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

@Path("/game")
@Produces(MediaType.APPLICATION_JSON)
public class GameResource {
    private final GameState gameState;

    public GameResource(GameState gameState) {
        this.gameState = gameState;
    }

    @POST
    @Path("/join")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response joinGame(JoinRequest request) {
        try {
            Player player = gameState.joinGame(request.getName());
            return Response.ok(new JoinResponse(player.getId(), "Welcome to the game, " + player.getName() + "!")).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
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

        String[] parts = request.getCommand().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

        Optional<GameCommand> command = gameState.getCommandRegistry().getCommand(commandName);
        if (!command.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Unknown command: " + commandName))
                    .build();
        }

        CommandResult result = command.get().execute(player, args);
        return Response.ok(new CommandResponse(result)).build();
    }

    @DELETE
    @Path("/leave/{playerId}")
    public Response leaveGame(@PathParam("playerId") String playerId) {
        gameState.leaveGame(playerId);
        return Response.ok().build();
    }

    // Request/Response classes
    public static class JoinRequest {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
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

    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
    }
}