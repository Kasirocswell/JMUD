package com.mudgame.entities.abilities;

import com.mudgame.entities.CharacterClass;
import com.mudgame.entities.Player;

public class TacticalStrikeAbility extends Ability {
    public TacticalStrikeAbility() {
        super("tactical_strike", "Tactical Strike",
                "Perform a precise attack that deals bonus damage",
                25, 45, 5, CharacterClass.SOLDIER);
    }

    @Override
    public AbilityResult use(Player user, String... args) {
        // Implement ability logic
        return AbilityResult.success(
                "You execute a perfect tactical strike.",
                user.getFullName() + " executes a devastating tactical strike."
        );
    }
}
