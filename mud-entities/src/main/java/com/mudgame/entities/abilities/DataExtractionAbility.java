package com.mudgame.entities.abilities;

import com.mudgame.entities.Attributes;
import com.mudgame.entities.CharacterClass;
import com.mudgame.entities.Player;

public class DataExtractionAbility extends Ability {
    public DataExtractionAbility() {
        super(
                "data_extraction",
                "Data Extraction",
                "Extract valuable data from electronic systems for bonus credits",
                25, // energy cost
                45, // cooldown in ticks
                5,  // required level
                CharacterClass.HACKER
        );
    }

    @Override
    public AbilityResult use(Player user, String... args) {
        // Calculate credits based on Intelligence attribute
        int intelligence = user.getAttributes().getOrDefault(Attributes.INTELLIGENCE, 10);
        int baseCredits = 10 + (intelligence / 2);
        int bonusCredits = user.getLevel() * 2;
        int totalCredits = baseCredits + bonusCredits;

        // Add credits to player
        user.setCredits(user.getCredits() + totalCredits);

        return AbilityResult.success(
                String.format("You extract %d credits worth of valuable data!", totalCredits),
                String.format("%s extracts data from nearby systems.", user.getFullName())
        );
    }
}