package com.mudgame.entities.merchants;

import com.mudgame.entities.*;
import com.mudgame.events.EventListener;
import java.util.*;

public abstract class MerchantNPC extends NPC {
    protected final Map<UUID, Item> inventory = new HashMap<>();
    protected final Random random = new Random();

    public MerchantNPC(String name, String description, int level,
                       int maxHealth, EventListener eventListener) {
        super(name, description, NPCType.MERCHANT, level, maxHealth, false, eventListener);
        initializeBaseResponses();
    }

    private void initializeBaseResponses() {
        addResponse("greet", getGreeting());
        addResponse("list", () -> getInventoryListing());
        addResponse("farewell", getFarewell());
    }

    // Abstract methods that specific merchants must implement
    protected abstract String getGreeting();
    protected abstract String getFarewell();
    protected abstract void restockInventory();

    public String handleTrade(Player player, String action) {
        String[] parts = action.toLowerCase().split("\\s+", 2);
        String command = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "buy":
                return handlePurchase(player, args);
            case "list":
                return getInventoryListing();
            case "greet":
                return getGreeting();
            case "farewell":
                return getFarewell();
            default:
                return "Available commands: greet, list, buy <item>, farewell";
        }
    }

    protected String getInventoryListing() {
        StringBuilder sb = new StringBuilder("Available items:\n");
        inventory.values().forEach(item -> {
            sb.append(String.format("%-20s - %d credits\n",
                    item.getName(),
                    item.getValue()));
            // Add item description on the next line
            sb.append(String.format("    %s\n", item.getDescription()));
        });
        return sb.toString();
    }

    protected String handlePurchase(Player player, String itemName) {
        if (itemName.isEmpty()) {
            return "What would you like to buy?";
        }

        Optional<Item> item = inventory.values().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName.toLowerCase()))
                .findFirst();

        if (item.isEmpty()) {
            return "I don't have that item.";
        }

        Item toBuy = item.get();
        if (player.getCredits() < toBuy.getValue()) {
            return String.format("You need %d credits for that, but only have %d.",
                    toBuy.getValue(), player.getCredits());
        }

        InventoryResult result = player.pickupItem(toBuy);
        if (result.isSuccess()) {
            player.setCredits(player.getCredits() - toBuy.getValue());
            String message = String.format("You purchased %s for %d credits.",
                    toBuy.getName(), toBuy.getValue());
            broadcastToRoom(String.format("%s purchases %s from %s.",
                    player.getFullName(), toBuy.getName(), getName()));
            return message;
        }

        return result.getMessage();
    }

    protected void broadcastToRoom(String message) {
        Room currentRoom = getCurrentRoom();
        if (currentRoom != null) {
            eventListener.onEvent("room", currentRoom.getName(), message);
        }
    }

    @Override
    public void onTick() {
        // Merchants don't need tick behavior by default
    }

    @Override
    public void onDeath(Player killer) {
        // Merchants don't die by default
    }
}