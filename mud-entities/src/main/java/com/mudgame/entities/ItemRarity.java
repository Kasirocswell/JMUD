package com.mudgame.entities;

// Enums
public enum ItemRarity {
    COMMON("\u001B[37m"),      // White
    UNCOMMON("\u001B[32m"),    // Green
    RARE("\u001B[34m"),        // Blue
    EPIC("\u001B[35m"),        // Purple
    LEGENDARY("\u001B[33m");   // Gold

    private final String colorCode;

    ItemRarity(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColor() {
        return colorCode;
    }
}
