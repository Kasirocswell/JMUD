package com.mudgame.entities.abilities;

import com.mudgame.entities.CharacterClass;
import com.mudgame.entities.Player;

// Example ability implementations
public class SystemOverrideAbility extends Ability {
    public SystemOverrideAbility() {
        super("system_override", "System Override",
                "Temporarily disable electronic systems and security measures",
                30, 60, 5, CharacterClass.HACKER);
    }

    @Override
    public AbilityResult use(Player user, String... args) {
        // Implement ability logic
        return AbilityResult.success(
                "You override the local systems, causing them to malfunction.",
                user.getFullName() + " interferes with the local systems, causing them to spark and malfunction."
        );
    }
}
