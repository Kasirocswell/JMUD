package com.mudgame.entities;

import java.util.UUID;

public class Item {
    private final String id;
    private String name;
    private String description;
    private boolean isPickable;

    public Item(String name, String description, boolean isPickable) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.isPickable = isPickable;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPickable() {
        return isPickable;
    }

    public void setPickable(boolean pickable) {
        isPickable = pickable;
    }

    @Override
    public String toString() {
        return name + ": " + description;
    }
}
