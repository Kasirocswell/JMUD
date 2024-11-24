package com.mudgame.entities.enemies;

import com.mudgame.entities.NPC;
import com.mudgame.entities.NPCState;
import com.mudgame.entities.NPCType;
import com.mudgame.entities.Player;
import com.mudgame.entities.Room;

import java.util.Random;

public class EnemyNPC extends NPC {
    private int attackPower;
    private int attackSpeed; // Ticks between attacks
    private int ticksSinceLastAttack;
    private final Random random = new Random();

    public EnemyNPC(String name, String description, int level,
                    int maxHealth, int attackPower, int attackSpeed) {
        super(name, description, NPCType.ENEMY, level, maxHealth, true);
        this.attackPower = attackPower;
        this.attackSpeed = attackSpeed;
        this.ticksSinceLastAttack = 0;

        // Add some basic hostile responses
        addResponse("hello", "The " + name + " snarls menacingly!");
        addResponse("attack", "The " + name + " prepares to defend itself!");
    }

    @Override
    public void onTick() {
        if (isDead()) return;

        Room room = getCurrentRoom();
        if (room == null) return;

        ticksSinceLastAttack++;

        // If we're hostile and there are players in the room, maybe attack
        if (isHostile() && !room.getPlayers().isEmpty() &&
                ticksSinceLastAttack >= attackSpeed) {

            // Randomly select a player to attack
            Player target = room.getPlayers().stream()
                    .skip(random.nextInt(room.getPlayers().size()))
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                setState(NPCState.HOSTILE);
                attackPlayer(target);
                ticksSinceLastAttack = 0;
            }
        }
    }

    private void attackPlayer(Player player) {
        // Calculate damage (add some randomness)
        int damage = attackPower + random.nextInt(attackPower / 2);
        player.damage(damage);

        // Broadcast message to room
        String message = String.format("%s attacks %s for %d damage!",
                getName(), player.getFullName(), damage);
        // TODO: Add method to broadcast message to room
    }

    @Override
    public String interact(Player player, String action) {
        switch (action.toLowerCase()) {
            case "attack":
                setState(NPCState.HOSTILE);
                return "The " + getName() + " turns to face you, ready to fight!";
            default:
                return getResponse(action);
        }
    }

    @Override
    public void onDeath(Player killer) {
        // Drop loot
        Random random = new Random();
        int credits = 10 + random.nextInt(level * 10);
        killer.setCredits(killer.getCredits() + credits);

        // TODO: Add proper loot tables and drop system
        String message = String.format("%s has been defeated! You gain %d credits.",
                getName(), credits);
        // TODO: Broadcast death message to room
    }

    // Getters and setters for combat stats
    public int getAttackPower() { return attackPower; }
    public void setAttackPower(int attackPower) { this.attackPower = attackPower; }
    public int getAttackSpeed() { return attackSpeed; }
    public void setAttackSpeed(int attackSpeed) { this.attackSpeed = attackSpeed; }
}
