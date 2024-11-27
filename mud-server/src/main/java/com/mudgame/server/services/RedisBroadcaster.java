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
     * Closes the Redis connection.
     */
    public void close() {
        redis.close();
    }
}
