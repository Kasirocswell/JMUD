package com.mudgame.entities.skills;

public enum SkillCategory {
    COMBAT("Combat"),
    TECH("Technology"),
    SOCIAL("Social"),
    SURVIVAL("Survival");

    private final String displayName;

    SkillCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
