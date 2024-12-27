package com.mudgame.entities.abilities;

import com.mudgame.entities.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Container class to manage a character's abilities
public class AbilitySet {
    private final Map<String, Ability> abilities = new HashMap<>();
    private final Player owner;

    public AbilitySet(Player owner) {
        this.owner = owner;
        initializeDefaultAbilities();
    }

    private void initializeDefaultAbilities() {
        // Add class-specific abilities
        switch (owner.getCharacterClass()) {
            case HACKER:
                addAbility(new SystemOverrideAbility());
                addAbility(new DataExtractionAbility());
                break;
            case SOLDIER:
                addAbility(new TacticalStrikeAbility());
                addAbility(new DefensiveStanceAbility());
                break;
            // Add other class abilities
        }
    }

    public void addAbility(Ability ability) {
        if (ability.getRequiredClass() == owner.getCharacterClass() &&
                owner.getLevel() >= ability.getRequiredLevel()) {
            abilities.put(ability.getId(), ability);
        }
    }

    public Optional<Ability> getAbility(String id) {
        return Optional.ofNullable(abilities.get(id));
    }

    public Collection<Ability> getAllAbilities() {
        return Collections.unmodifiableCollection(abilities.values());
    }

    public void tickCooldowns() {
        abilities.values().forEach(Ability::tick);
    }

    public AbilityResult useAbility(String abilityId, String... args) {
        Optional<Ability> ability = getAbility(abilityId);
        if (ability.isEmpty()) {
            return AbilityResult.failure("You don't have that ability.");
        }

        Ability abil = ability.get();
        if (!abil.isReady()) {
            return AbilityResult.failure("That ability is still on cooldown.");
        }

        if (owner.getEnergy() < abil.getEnergyCost()) {
            return AbilityResult.failure("You don't have enough energy.");
        }

        AbilityResult result = abil.use(owner, args);
        if (result.isSuccess()) {
            owner.useEnergy(abil.getEnergyCost());
            abil.resetCooldown();
        }

        return result;
    }

    public String getAbilitiesDisplay() {
        StringBuilder sb = new StringBuilder("Abilities:\n\n");

        abilities.values().forEach(ability -> {
            sb.append(String.format("%-20s", ability.getName()));
            if (ability.getCurrentCooldown() > 0) {
                sb.append(String.format(" (Cooldown: %ds)", ability.getCurrentCooldown()));
            } else if (!ability.isReady()) {
                sb.append(" (Not Ready)");
            } else {
                sb.append(" (Ready)");
            }
            sb.append(String.format(" - %d energy\n", ability.getEnergyCost()));
            sb.append("  ").append(ability.getDescription()).append("\n\n");
        });

        return sb.toString();
    }
}
