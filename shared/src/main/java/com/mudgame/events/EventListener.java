package com.mudgame.events;

/**
 * Interface for handling game events.
 */
public interface EventListener {
    /**
     * Handles an event.
     *
     * @param eventType The type of the event (e.g., "room", "player", "system").
     * @param target    The target of the event (e.g., room ID, player ID).
     * @param message   The message to process.
     */
    void onEvent(String eventType, String target, String message);
}
