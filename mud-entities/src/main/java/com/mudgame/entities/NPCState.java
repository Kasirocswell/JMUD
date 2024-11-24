package com.mudgame.entities;

public enum NPCState {
    IDLE,       // Default state, not actively doing anything
    HOSTILE,    // Actively hostile/fighting
    BUSY,       // Engaged in some activity (trading, conversation, etc)
    MOVING,     // Moving between locations
    DEAD        // NPC has been killed
}
