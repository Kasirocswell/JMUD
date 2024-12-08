package com.mudgame.server.services;

import redis.clients.jedis.Jedis;

public class RedisBroadcaster {
    private final Jedis redis;

    public RedisBroadcaster() {
        // Connect to Redis at localhost on default port 6379
        this.redis = new Jedis("localhost", 6379);
    }

    /**
     * Publishes a message to a specified Redis channel.
     * For room messages, the channel should be the room name (e.g., "room:Shibuya_Crossing")
     * For player messages, the channel should be the player ID (e.g., "player:123")
     * For system messages, the channel should be "system"
     *
     * @param channel The Redis channel to publish the message to.
     * @param message The message to broadcast.
     */
    public void broadcast(String channel, String message) {
        try {
            redis.publish(channel, message);
            System.out.println("Message broadcasted to channel [" + channel + "]: " + message);
        } catch (Exception e) {
            System.err.println("Error broadcasting message: " + e.getMessage());
        }
    }

    /**
     * Helper method for broadcasting to a room using room name
     */
    public void broadcastToRoom(String roomName, String message) {
        String channel = "room:" + roomName.replace(" ", "_");
        broadcast(channel, message);
    }

    /**
     * Helper method for broadcasting to a specific player
     */
    public void broadcastToPlayer(String playerId, String message) {
        broadcast("player:" + playerId, message);
    }

    /**
     * Helper method for broadcasting system messages
     */
    public void broadcastSystem(String message) {
        broadcast("system", message);
    }

    /**
     * Closes the Redis connection.
     */
    public void close() {
        redis.close();
    }
}