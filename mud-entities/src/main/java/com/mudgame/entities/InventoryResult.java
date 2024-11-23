package com.mudgame.entities;

public class InventoryResult {
    private final boolean success;
    private final String message;
    private final int amount;
    private final boolean partial;

    private InventoryResult(boolean success, String message, int amount, boolean partial) {
        this.success = success;
        this.message = message;
        this.amount = amount;
        this.partial = partial;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getAmount() { return amount; }
    public boolean isPartial() { return partial; }

    public static InventoryResult success(String message) {
        return new InventoryResult(true, message, 0, false);
    }

    public static InventoryResult failure(String message) {
        return new InventoryResult(false, message, 0, false);
    }

    public static InventoryResult partial(String message, int amount) {
        return new InventoryResult(true, message, amount, true);
    }
}
