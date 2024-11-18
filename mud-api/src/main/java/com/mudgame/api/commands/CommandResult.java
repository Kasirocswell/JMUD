package com.mudgame.api.commands;

public class CommandResult {
    private final boolean success;
    private final String message;
    private final String privateMessage;
    private final String roomMessage;

    private CommandResult(boolean success, String message, String privateMessage, String roomMessage) {
        this.success = success;
        this.message = message;
        this.privateMessage = privateMessage;
        this.roomMessage = roomMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getPrivateMessage() {
        return privateMessage;
    }

    public String getRoomMessage() {
        return roomMessage;
    }

    public static CommandResult success(String message) {
        return new CommandResult(true, message, null, null);
    }

    public static CommandResult success(String privateMessage, String roomMessage) {
        return new CommandResult(true, null, privateMessage, roomMessage);
    }

    public static CommandResult failure(String message) {
        return new CommandResult(false, message, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String message;
        private String privateMessage;
        private String roomMessage;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder privateMessage(String privateMessage) {
            this.privateMessage = privateMessage;
            return this;
        }

        public Builder roomMessage(String roomMessage) {
            this.roomMessage = roomMessage;
            return this;
        }

        public CommandResult build() {
            return new CommandResult(success, message, privateMessage, roomMessage);
        }
    }
}