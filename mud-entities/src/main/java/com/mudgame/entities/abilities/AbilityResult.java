package com.mudgame.entities.abilities;

import java.util.HashMap;
import java.util.Map;

public class AbilityResult {
    private final boolean success;
    private final String message;
    private final String roomMessage;
    private final Map<String, Object> effects;

    private AbilityResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.roomMessage = builder.roomMessage;
        this.effects = builder.effects;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getRoomMessage() { return roomMessage; }
    public Map<String, Object> getEffects() { return effects; }

    public static Builder builder() {
        return new Builder();
    }

    public static AbilityResult success(String message, String roomMessage) {
        return builder()
                .success(true)
                .message(message)
                .roomMessage(roomMessage)
                .build();
    }

    public static AbilityResult failure(String message) {
        return builder()
                .success(false)
                .message(message)
                .build();
    }

    public static class Builder {
        private boolean success;
        private String message;
        private String roomMessage;
        private Map<String, Object> effects = new HashMap<>();

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder roomMessage(String roomMessage) {
            this.roomMessage = roomMessage;
            return this;
        }

        public Builder effects(Map<String, Object> effects) {
            this.effects = effects;
            return this;
        }

        public Builder addEffect(String key, Object value) {
            this.effects.put(key, value);
            return this;
        }

        public AbilityResult build() {
            return new AbilityResult(this);
        }
    }
}