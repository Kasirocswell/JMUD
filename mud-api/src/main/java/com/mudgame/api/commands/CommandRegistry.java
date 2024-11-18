package com.mudgame.api.commands;

import java.util.List;
import java.util.Optional;

public interface CommandRegistry {
    void registerCommand(GameCommand command);
    Optional<GameCommand> getCommand(String name);
    List<GameCommand> getAllCommands();

    default String getHelpText() {
        StringBuilder help = new StringBuilder("Available commands:\n");
        getAllCommands().forEach(cmd ->
                help.append(cmd.getHelp()).append("\n")
        );
        return help.toString();
    }
}
