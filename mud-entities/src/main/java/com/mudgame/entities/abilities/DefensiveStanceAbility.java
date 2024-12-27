package com.mudgame.entities.abilities;

import com.mudgame.entities.Attributes;
import com.mudgame.entities.CharacterClass;
import com.mudgame.entities.Player;
import java.util.HashMap;

public class DefensiveStanceAbility extends Ability {
    public DefensiveStanceAbility() {
        super(
                "defensive_stance",
                "Defensive Stance",
                "Enter a defensive stance that reduces incoming damage",
                20, // energy cost
                60, // cooldown in ticks
                5,  // required level
                CharacterClass.SOLDIER
        );
    }

    @Override
    public AbilityResult use(Player user, String... args) {
        // Calculate damage reduction based on Constitution
        int constitution = user.getAttributes().getOrDefault(Attributes.CONSTITUTION, 10);
        int damageReduction = 10 + (constitution / 4); // Base 10% + up to 25% from constitution
        int duration = 30; // 30 second buff duration

        HashMap<String, Object> effects = new HashMap<>();
        effects.put("type", "BUFF");
        effects.put("stat", "DAMAGE_REDUCTION");
        effects.put("amount", damageReduction);
        effects.put("duration", duration);

        return AbilityResult.builder()
                .success(true)
                .message(String.format("You enter a defensive stance, reducing incoming damage by %d%%!", damageReduction))
                .roomMessage(String.format("%s assumes a defensive combat stance.", user.getFullName()))
                .effects(effects)
                .build();
    }
}