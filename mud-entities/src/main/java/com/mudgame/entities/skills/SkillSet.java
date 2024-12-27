package com.mudgame.entities.skills;

import com.mudgame.entities.CharacterClass;
import com.mudgame.entities.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Container class to manage a character's skills
public class SkillSet {
    private final Map<String, Skill> skills = new HashMap<>();
    private final Player owner;

    public SkillSet(Player owner) {
        this.owner = owner;
        initializeDefaultSkills();
    }

    private void initializeDefaultSkills() {
        // Initialize based on character class
        CharacterClass charClass = owner.getCharacterClass();

        // Common skills for all classes
        addSkill(new Skill("survival", "Survival", "Basic survival skills", SkillCategory.SURVIVAL, 100));
        addSkill(new Skill("negotiation", "Negotiation", "Ability to negotiate better prices", SkillCategory.SOCIAL, 100));

        // Class-specific skills
        switch (charClass) {
            case HACKER:
                addSkill(new Skill("hacking", "System Hacking", "Ability to hack systems", SkillCategory.TECH, 100));
                addSkill(new Skill("encryption", "Encryption", "Knowledge of encryption systems", SkillCategory.TECH, 100));
                break;
            case SOLDIER:
                addSkill(new Skill("tactics", "Combat Tactics", "Advanced combat knowledge", SkillCategory.COMBAT, 100));
                addSkill(new Skill("weapons", "Weapons Mastery", "Expertise with weapons", SkillCategory.COMBAT, 100));
                break;
            // Add other class-specific skills
        }
    }

    public void addSkill(Skill skill) {
        skills.put(skill.getId(), skill);
    }

    public Optional<Skill> getSkill(String id) {
        return Optional.ofNullable(skills.get(id));
    }

    public Collection<Skill> getAllSkills() {
        return Collections.unmodifiableCollection(skills.values());
    }

    public Map<SkillCategory, List<Skill>> getSkillsByCategory() {
        Map<SkillCategory, List<Skill>> categorized = new EnumMap<>(SkillCategory.class);
        for (SkillCategory category : SkillCategory.values()) {
            categorized.put(category, new ArrayList<>());
        }

        skills.values().forEach(skill ->
                categorized.get(skill.getCategory()).add(skill)
        );

        return categorized;
    }

    public boolean improveSkill(String skillId, int experience) {
        Optional<Skill> skill = getSkill(skillId);
        if (skill.isPresent()) {
            boolean leveledUp = skill.get().addExperience(experience);
            // Trigger any level-up effects
            if (leveledUp) {
                onSkillLevelUp(skill.get());
            }
            return leveledUp;
        }
        return false;
    }

    private void onSkillLevelUp(Skill skill) {
        // Implement level-up effects (e.g., stat boosts, unlock abilities)
        switch (skill.getId()) {
            case "weapons":
                // Could increase damage with weapons
                break;
            case "hacking":
                // Could increase hacking success rate
                break;
            // Add other skill-specific effects
        }
    }

    public String getSkillsDisplay() {
        StringBuilder sb = new StringBuilder("Skills:\n");

        getSkillsByCategory().forEach((category, skillList) -> {
            if (!skillList.isEmpty()) {
                sb.append("\n").append(category.getDisplayName()).append(":\n");
                skillList.forEach(skill ->
                        sb.append(String.format("  %-20s Level %2d (%d/%d exp)\n",
                                skill.getName(),
                                skill.getLevel(),
                                skill.getExperience(),
                                skill.getRequiredExperience()))
                );
            }
        });

        return sb.toString();
    }
}
