package com.mudgame.entities;

public enum Direction {
    NORTH("n") {
        @Override
        public Direction opposite() {
            return SOUTH;
        }
    },
    SOUTH("s") {
        @Override
        public Direction opposite() {
            return NORTH;
        }
    },
    EAST("e") {
        @Override
        public Direction opposite() {
            return WEST;
        }
    },
    WEST("w") {
        @Override
        public Direction opposite() {
            return EAST;
        }
    },
    UP("u") {
        @Override
        public Direction opposite() {
            return DOWN;
        }
    },
    DOWN("d") {
        @Override
        public Direction opposite() {
            return UP;
        }
    };

    private final String shortCommand;

    Direction(String shortCommand) {
        this.shortCommand = shortCommand;
    }

    public String getShortCommand() {
        return shortCommand;
    }

    public abstract Direction opposite();

    public static Direction fromCommand(String command) {
        for (Direction direction : values()) {
            if (direction.shortCommand.equalsIgnoreCase(command) ||
                    direction.name().equalsIgnoreCase(command)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Invalid direction: " + command);
    }
}