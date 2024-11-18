package com.mudgame.server.commands;

import com.mudgame.api.commands.*;
import com.mudgame.entities.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCommandRegistry implements CommandRegistry {
    private final Map<String, GameCommand> commands;

    public DefaultCommandRegistry() {
        this.commands = new ConcurrentHashMap<>();
        registerDefaultCommands();
    }

    private void registerDefaultCommands() {
        // Register basic movement command
        registerCommand(new MoveCommand() {});

        // Register look command
        registerCommand(new LookCommand() {});

        // Register say command
        registerCommand(new SayCommand() {});

        // Register help command
        registerCommand(new GameCommand() {
            @Override
            public CommandResult execute(Player player, String[] args) {
                return CommandResult.success(getHelpText());
            }

            @Override
            public String getHelp() {
                return "help - Display this help message";
            }

            @Override
            public String getName() {
                return "help";
            }
        });
    }

    @Override
    public void registerCommand(GameCommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    @Override
    public Optional<GameCommand> getCommand(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }

    @Override
    public List<GameCommand> getAllCommands() {
        return new ArrayList<>(commands.values());
    }
}