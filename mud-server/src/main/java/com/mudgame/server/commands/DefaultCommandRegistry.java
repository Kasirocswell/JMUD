package com.mudgame.server.commands;


import com.mudgame.api.commands.CommandRegistry;
import com.mudgame.api.commands.CommandResult;
import com.mudgame.api.commands.DropCommand;
import com.mudgame.api.commands.EquipCommand;
import com.mudgame.api.commands.EquipmentCommand;
import com.mudgame.api.commands.ExamineCommand;
import com.mudgame.api.commands.GameCommand;
import com.mudgame.api.commands.GetCommand;
import com.mudgame.api.commands.InventoryCommand;
import com.mudgame.api.commands.LookCommand;
import com.mudgame.api.commands.MoveCommand;
import com.mudgame.api.commands.SayCommand;
import com.mudgame.api.commands.TalkCommand;
import com.mudgame.api.commands.UnequipCommand;
import com.mudgame.entities.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        // Register inventory commands
        registerCommand(new InventoryCommand() {});
        registerCommand(new EquipmentCommand() {});
        registerCommand(new GetCommand() {});
        registerCommand(new DropCommand() {});
        registerCommand(new EquipCommand() {});
        registerCommand(new UnequipCommand() {});
        registerCommand(new ExamineCommand() {});

        // Register Talk Command
        registerCommand(new TalkCommand() {});

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