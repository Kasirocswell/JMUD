package com.mudgame.entities.skills;

// Base class for all skills
public class Skill {
    private final String id;
    private final String name;
    private final String description;
    private final SkillCategory category;
    private int level;
    private int experience;
    private final int maxLevel;

    public Skill(String id, String name, String description, SkillCategory category, int maxLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.level = 1;
        this.experience = 0;
        this.maxLevel = maxLevel;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillCategory getCategory() { return category; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getMaxLevel() { return maxLevel; }

    // Experience and leveling methods
    public boolean addExperience(int amount) {
        if (level >= maxLevel) return false;

        experience += amount;
        int requiredExp = getRequiredExperience();

        if (experience >= requiredExp) {
            level++;
            experience -= requiredExp;
            return true;
        }
        return false;
    }

    public int getRequiredExperience() {
        return level * 100; // Simple exponential progression
    }
}

