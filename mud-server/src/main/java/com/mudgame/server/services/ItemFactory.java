package com.mudgame.server.services;

import com.mudgame.entities.*;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public class ItemFactory {
    private final DataSource dataSource;
    private final Map<UUID, Item> itemCache = new ConcurrentHashMap<>();

    public ItemFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Main item creation methods
    public Item createFromDatabase(UUID id, Map<String, Object> data) {
        ItemType type = ItemType.valueOf((String) data.get("type"));

        switch (type) {
            case WEAPON:
                return createWeapon(data);
            case ARMOR:
                return createArmor(data);
            case CONSUMABLE:
                return createConsumable(data);
            default:
                return createBaseItem(data);
        }
    }

    private Item createBaseItem(Map<String, Object> data) {
        return new Item(
                (String) data.get("name"),
                (String) data.get("description"),
                ItemRarity.valueOf((String) data.get("rarity")),
                ItemType.valueOf((String) data.get("type")),  // Added ItemType
                ((Number) data.get("weight")).doubleValue(),
                ((Number) data.get("value")).intValue(),
                ((Number) data.get("level_required")).intValue(),
                ((Number) data.get("max_durability")).intValue(),
                data.get("slot") != null ? EquipmentSlot.valueOf((String) data.get("slot")) : null,
                (Boolean) data.get("stackable"),
                ((Number) data.get("max_stack_size")).intValue()
        );
    }

    private Weapon createWeapon(Map<String, Object> data) {
        return new Weapon(
                (String) data.get("name"),
                (String) data.get("description"),
                ItemRarity.valueOf((String) data.get("rarity")),
                ((Number) data.get("weight")).doubleValue(),
                ((Number) data.get("value")).intValue(),
                ((Number) data.get("level_required")).intValue(),
                ((Number) data.get("max_durability")).intValue(),
                EquipmentSlot.valueOf((String) data.get("slot")),
                ((Number) data.get("min_damage")).intValue(),
                ((Number) data.get("max_damage")).intValue(),
                ((Number) data.get("attack_speed")).doubleValue(),
                WeaponType.valueOf((String) data.get("weapon_type")),
                DamageType.valueOf((String) data.get("damage_type"))
        );
    }

    private Armor createArmor(Map<String, Object> data) {
        Map<DamageType, Integer> resistances = new EnumMap<>(DamageType.class);
        resistances.put(DamageType.PHYSICAL, ((Number) data.get("physical_resist")).intValue());
        resistances.put(DamageType.ENERGY, ((Number) data.get("energy_resist")).intValue());
        resistances.put(DamageType.THERMAL, ((Number) data.get("thermal_resist")).intValue());
        resistances.put(DamageType.CRYO, ((Number) data.get("cryo_resist")).intValue());
        resistances.put(DamageType.TOXIC, ((Number) data.get("toxic_resist")).intValue());
        resistances.put(DamageType.PSI, ((Number) data.get("psi_resist")).intValue());

        return new Armor(
                (String) data.get("name"),
                (String) data.get("description"),
                ItemRarity.valueOf((String) data.get("rarity")),
                ((Number) data.get("weight")).doubleValue(),
                ((Number) data.get("value")).intValue(),
                ((Number) data.get("level_required")).intValue(),
                ((Number) data.get("max_durability")).intValue(),
                EquipmentSlot.valueOf((String) data.get("slot")),
                ((Number) data.get("defense")).intValue(),
                ((Number) data.get("energy_shield")).intValue(),
                ArmorType.valueOf((String) data.get("armor_type")),
                resistances
        );
    }

    private Consumable createConsumable(Map<String, Object> data) {
        return new Consumable(
                (String) data.get("name"),
                (String) data.get("description"),
                ItemRarity.valueOf((String) data.get("rarity")),
                ((Number) data.get("weight")).doubleValue(),
                ((Number) data.get("value")).intValue(),
                ((Number) data.get("level_required")).intValue(),
                ConsumableType.valueOf((String) data.get("consumable_type")),
                ((Number) data.get("effect_power")).intValue(),
                ((Number) data.get("effect_duration")).intValue(),
                (String) data.get("effect_description"),
                (Boolean) data.get("stackable"),
                ((Number) data.get("max_stack_size")).intValue()
        );
    }

    // Database interaction methods
    public Collection<Item> getStarterItems() {
        try (Connection conn = dataSource.getConnection()) {
            return executeItemQuery(conn,
                    "SELECT i.*, " +
                            "w.min_damage, w.max_damage, w.attack_speed, w.weapon_type, w.damage_type, " +
                            "a.defense, a.energy_shield, a.armor_type, " +
                            "a.physical_resist, a.energy_resist, a.thermal_resist, " +
                            "a.cryo_resist, a.toxic_resist, a.psi_resist, " +
                            "c.consumable_type, c.effect_power, c.effect_duration, c.effect_description " +
                            "FROM items i " +
                            "LEFT JOIN weapon_properties w ON i.id = w.item_id " +
                            "LEFT JOIN armor_properties a ON i.id = a.item_id " +
                            "LEFT JOIN consumable_properties c ON i.id = c.item_id " +
                            "WHERE i.name LIKE 'Recruit''s%'",
                    null);
        } catch (SQLException e) {
            throw new RuntimeException("Error loading starter items", e);
        }
    }

    public Optional<Item> getItem(UUID itemId) {
        // Check cache first
        Item cachedItem = itemCache.get(itemId);
        if (cachedItem != null) {
            return Optional.of(cachedItem);
        }

        try (Connection conn = dataSource.getConnection()) {
            List<Item> items = executeItemQuery(conn,
                    "SELECT i.*, " +
                            "w.min_damage, w.max_damage, w.attack_speed, w.weapon_type, w.damage_type, " +
                            "a.defense, a.energy_shield, a.armor_type, " +
                            "a.physical_resist, a.energy_resist, a.thermal_resist, " +
                            "a.cryo_resist, a.toxic_resist, a.psi_resist, " +
                            "c.consumable_type, c.effect_power, c.effect_duration, c.effect_description " +
                            "FROM items i " +
                            "LEFT JOIN weapon_properties w ON i.id = w.item_id " +
                            "LEFT JOIN armor_properties a ON i.id = a.item_id " +
                            "LEFT JOIN consumable_properties c ON i.id = c.item_id " +
                            "WHERE i.id = ?",
                    stmt -> stmt.setObject(1, itemId));

            return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
        } catch (SQLException e) {
            throw new RuntimeException("Error loading item " + itemId, e);
        }
    }

    public Inventory loadPlayerInventory(Player player) {
        Inventory inventory = new Inventory(100.0, 20); // Default values

        try (Connection conn = dataSource.getConnection()) {
            Collection<Item> items = executeItemQuery(conn,
                    "SELECT i.*, ii.current_durability, ii.current_stack_size, " +
                            "w.min_damage, w.max_damage, w.attack_speed, w.weapon_type, w.damage_type, " +
                            "a.defense, a.energy_shield, a.armor_type, " +
                            "a.physical_resist, a.energy_resist, a.thermal_resist, " +
                            "a.cryo_resist, a.toxic_resist, a.psi_resist, " +
                            "c.consumable_type, c.effect_power, c.effect_duration, c.effect_description " +
                            "FROM inventory_items ii " +
                            "JOIN items i ON ii.item_id = i.id " +
                            "LEFT JOIN weapon_properties w ON i.id = w.item_id " +
                            "LEFT JOIN armor_properties a ON i.id = a.item_id " +
                            "LEFT JOIN consumable_properties c ON i.id = c.item_id " +
                            "WHERE ii.player_id = ? AND ii.equipped = false",
                    stmt -> stmt.setObject(1, player.getId()));

            for (Item item : items) {
                inventory.addItem(item);
            }

            return inventory;
        } catch (SQLException e) {
            throw new RuntimeException("Error loading inventory for player " + player.getId(), e);
        }
    }

    public Equipment loadPlayerEquipment(Player player) {
        Equipment equipment = new Equipment(player);

        try (Connection conn = dataSource.getConnection()) {
            Collection<Item> items = executeItemQuery(conn,
                    "SELECT i.*, ii.current_durability, " +
                            "w.min_damage, w.max_damage, w.attack_speed, w.weapon_type, w.damage_type, " +
                            "a.defense, a.energy_shield, a.armor_type, " +
                            "a.physical_resist, a.energy_resist, a.thermal_resist, " +
                            "a.cryo_resist, a.toxic_resist, a.psi_resist " +
                            "FROM inventory_items ii " +
                            "JOIN items i ON ii.item_id = i.id " +
                            "LEFT JOIN weapon_properties w ON i.id = w.item_id " +
                            "LEFT JOIN armor_properties a ON i.id = a.item_id " +
                            "WHERE ii.player_id = ? AND ii.equipped = true",
                    stmt -> stmt.setObject(1, player.getId()));

            for (Item item : items) {
                equipment.equipItem(item);
            }

            return equipment;
        } catch (SQLException e) {
            throw new RuntimeException("Error loading equipment for player " + player.getId(), e);
        }
    }

    public void giveStarterItems(Player player) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT give_starter_items(?)"
             )) {

            System.out.println("Giving starter items to player: " + player.getId());
            stmt.setObject(1, player.getId());  // Make sure we're using the same ID

            // Log the SQL that would be executed
            System.out.println("Executing SQL with player ID: " + player.getId());

            stmt.execute();

            // Load inventory and equipment
            try {
                Inventory newInventory = loadPlayerInventory(player);
                System.out.println("Loaded inventory with " + newInventory.getUsedSlots() + " items");
                player.setInventory(newInventory);

                Equipment newEquipment = loadPlayerEquipment(player);
                System.out.println("Loaded equipment");
                player.setEquipment(newEquipment);
            } catch (Exception e) {
                System.err.println("Error reloading inventory/equipment: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Database error in giveStarterItems: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error giving starter items to player " + player.getId(), e);
        }
    }

    // Helper methods
    private List<Item> executeItemQuery(Connection conn, String sql,
                                        StatementPreparer preparer) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (preparer != null) {
                preparer.prepare(stmt);
            }
            ResultSet rs = stmt.executeQuery();
            List<Item> items = new ArrayList<>();

            while (rs.next()) {
                UUID itemId = UUID.fromString(rs.getString("id"));
                Item item = itemCache.computeIfAbsent(itemId,
                        id -> createFromResultSet(rs));
                items.add(item);
            }

            return items;
        }
    }

    private Item createFromResultSet(ResultSet rs) {
        try {
            Map<String, Object> data = new HashMap<>();

            // Map all the base item fields
            data.put("id", UUID.fromString(rs.getString("id")));
            data.put("name", rs.getString("name"));
            data.put("description", rs.getString("description"));
            data.put("rarity", rs.getString("rarity"));
            data.put("type", rs.getString("type"));
            data.put("weight", rs.getDouble("weight"));
            data.put("value", rs.getInt("value"));
            data.put("level_required", rs.getInt("level_required"));
            data.put("max_durability", rs.getInt("max_durability"));
            data.put("slot", rs.getString("slot"));
            data.put("stackable", rs.getBoolean("stackable"));
            data.put("max_stack_size", rs.getInt("max_stack_size"));

            // Map type-specific properties
            String type = rs.getString("type");
            if (type != null) {
                switch (ItemType.valueOf(type)) {
                    case WEAPON:
                        addWeaponProperties(data, rs);
                        break;
                    case ARMOR:
                        addArmorProperties(data, rs);
                        break;
                    case CONSUMABLE:
                        addConsumableProperties(data, rs);
                        break;
                }
            }

            return createFromDatabase((UUID)data.get("id"), data);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating item from ResultSet", e);
        }
    }

    private void addWeaponProperties(Map<String, Object> data, ResultSet rs) throws SQLException {
        data.put("min_damage", rs.getInt("min_damage"));
        data.put("max_damage", rs.getInt("max_damage"));
        data.put("attack_speed", rs.getDouble("attack_speed"));
        data.put("weapon_type", rs.getString("weapon_type"));
        data.put("damage_type", rs.getString("damage_type"));
    }

    private void addArmorProperties(Map<String, Object> data, ResultSet rs) throws SQLException {
        data.put("defense", rs.getInt("defense"));
        data.put("energy_shield", rs.getInt("energy_shield"));
        data.put("armor_type", rs.getString("armor_type"));
        data.put("physical_resist", rs.getInt("physical_resist"));
        data.put("energy_resist", rs.getInt("energy_resist"));
        data.put("thermal_resist", rs.getInt("thermal_resist"));
        data.put("cryo_resist", rs.getInt("cryo_resist"));
        data.put("toxic_resist", rs.getInt("toxic_resist"));
        data.put("psi_resist", rs.getInt("psi_resist"));
    }

    private void addConsumableProperties(Map<String, Object> data, ResultSet rs) throws SQLException {
        data.put("consumable_type", rs.getString("consumable_type"));
        data.put("effect_power", rs.getInt("effect_power"));
        data.put("effect_duration", rs.getInt("effect_duration"));
        data.put("effect_description", rs.getString("effect_description"));
    }

    @FunctionalInterface
    private interface StatementPreparer {
        void prepare(PreparedStatement stmt) throws SQLException;
    }
}