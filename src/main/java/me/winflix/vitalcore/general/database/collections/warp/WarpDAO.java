package me.winflix.vitalcore.general.database.collections.warp;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.Material;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.Database;
import me.winflix.vitalcore.warps.models.Warp;

public class WarpDAO {

    private static final VitalCore plugin = VitalCore.getPlugin();

    // ==================== INICIALIZACIÓN ====================
    public static void initialize(Connection connection) throws SQLException {
        String createWarpsTable = """
                CREATE TABLE IF NOT EXISTS warps (
                    name VARCHAR(32) PRIMARY KEY,
                    world_name VARCHAR(64) NOT NULL,
                    x DOUBLE PRECISION NOT NULL,
                    y DOUBLE PRECISION NOT NULL,
                    z DOUBLE PRECISION NOT NULL,
                    yaw REAL NOT NULL,
                    pitch REAL NOT NULL,
                    category VARCHAR(32) NOT NULL DEFAULT 'general',
                    icon VARCHAR(64) NOT NULL DEFAULT 'COMPASS',
                    cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                    cooldown INTEGER NOT NULL DEFAULT 0,
                    is_public BOOLEAN NOT NULL DEFAULT TRUE,
                    permission VARCHAR(128) DEFAULT NULL,
                    owner UUID NOT NULL,
                    created_at TIMESTAMP DEFAULT NOW(),
                    last_used TIMESTAMP DEFAULT NULL,
                    usage_count INTEGER NOT NULL DEFAULT 0
                );
                """;

        String createCategoriesTable = """
                CREATE TABLE IF NOT EXISTS warp_categories (
                    name VARCHAR(32) PRIMARY KEY,
                    display_name VARCHAR(64) NOT NULL,
                    icon VARCHAR(64) NOT NULL DEFAULT 'CHEST',
                    permission VARCHAR(128) DEFAULT NULL,
                    sort_order INTEGER NOT NULL DEFAULT 0
                );
                """;

        String createFavoritesTable = """
                CREATE TABLE IF NOT EXISTS warp_favorites (
                    player_uuid UUID NOT NULL,
                    warp_name VARCHAR(32) NOT NULL,
                    added_at TIMESTAMP DEFAULT NOW(),
                    PRIMARY KEY (player_uuid, warp_name),
                    FOREIGN KEY (warp_name) REFERENCES warps(name) ON DELETE CASCADE
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createWarpsTable);
            stmt.execute(createCategoriesTable);
            stmt.execute(createFavoritesTable);
            
            // Insertar categoría por defecto
            insertDefaultCategory(connection);
        }
    }

    private static void insertDefaultCategory(Connection connection) throws SQLException {
        String sql = """
                INSERT INTO warp_categories (name, display_name, icon, permission, sort_order)
                VALUES ('general', 'General', 'COMPASS', NULL, 0)
                ON CONFLICT (name) DO NOTHING;
                """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    // ==================== CREATE ====================
    public static boolean createWarp(Warp warp) {
        String sql = """
                INSERT INTO warps (name, world_name, x, y, z, yaw, pitch, category, icon, 
                                 cost, cooldown, is_public, permission, owner, created_at, usage_count)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 0);
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, warp.getName());
            stmt.setString(2, warp.getWorldName());
            stmt.setDouble(3, warp.getX());
            stmt.setDouble(4, warp.getY());
            stmt.setDouble(5, warp.getZ());
            stmt.setFloat(6, warp.getYaw());
            stmt.setFloat(7, warp.getPitch());
            stmt.setString(8, warp.getCategory());
            stmt.setString(9, warp.getIcon().name());
            stmt.setDouble(10, warp.getCost());
            stmt.setInt(11, warp.getCooldown());
            stmt.setBoolean(12, warp.isPublic());
            stmt.setString(13, warp.getPermission());
            stmt.setObject(14, warp.getOwner());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error creating warp: " + warp.getName(), e);
            return false;
        }
    }

    // ==================== READ ====================
    public static Warp getWarp(String name) {
        String sql = """
                SELECT name, world_name, x, y, z, yaw, pitch, category, icon, 
                       cost, cooldown, is_public, permission, owner, 
                       EXTRACT(EPOCH FROM created_at) * 1000 as created_at,
                       EXTRACT(EPOCH FROM last_used) * 1000 as last_used,
                       usage_count
                FROM warps WHERE name = ?;
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToWarp(rs);
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting warp: " + name, e);
        }
        return null;
    }

    public static List<Warp> getAllWarps() {
        String sql = """
                SELECT name, world_name, x, y, z, yaw, pitch, category, icon, 
                       cost, cooldown, is_public, permission, owner, 
                       EXTRACT(EPOCH FROM created_at) * 1000 as created_at,
                       EXTRACT(EPOCH FROM last_used) * 1000 as last_used,
                       usage_count
                FROM warps ORDER BY name;
                """;

        List<Warp> warps = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                warps.add(mapResultSetToWarp(rs));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting all warps", e);
        }
        return warps;
    }

    public static List<Warp> getPublicWarps() {
        String sql = """
                SELECT name, world_name, x, y, z, yaw, pitch, category, icon, 
                       cost, cooldown, is_public, permission, owner, 
                       EXTRACT(EPOCH FROM created_at) * 1000 as created_at,
                       EXTRACT(EPOCH FROM last_used) * 1000 as last_used,
                       usage_count
                FROM warps WHERE is_public = TRUE ORDER BY name;
                """;

        List<Warp> warps = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                warps.add(mapResultSetToWarp(rs));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting public warps", e);
        }
        return warps;
    }

    public static List<Warp> getWarpsByCategory(String category) {
        String sql = """
                SELECT name, world_name, x, y, z, yaw, pitch, category, icon, 
                       cost, cooldown, is_public, permission, owner, 
                       EXTRACT(EPOCH FROM created_at) * 1000 as created_at,
                       EXTRACT(EPOCH FROM last_used) * 1000 as last_used,
                       usage_count
                FROM warps WHERE category = ? ORDER BY name;
                """;

        List<Warp> warps = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                warps.add(mapResultSetToWarp(rs));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting warps by category: " + category, e);
        }
        return warps;
    }

    public static List<Warp> getWarpsByOwner(UUID owner) {
        String sql = """
                SELECT name, world_name, x, y, z, yaw, pitch, category, icon, 
                       cost, cooldown, is_public, permission, owner, 
                       EXTRACT(EPOCH FROM created_at) * 1000 as created_at,
                       EXTRACT(EPOCH FROM last_used) * 1000 as last_used,
                       usage_count
                FROM warps WHERE owner = ? ORDER BY name;
                """;

        List<Warp> warps = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setObject(1, owner);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                warps.add(mapResultSetToWarp(rs));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting warps by owner: " + owner, e);
        }
        return warps;
    }

    // ==================== UPDATE ====================
    public static boolean updateWarp(Warp warp) {
        String sql = """
                UPDATE warps SET 
                    world_name = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?,
                    category = ?, icon = ?, cost = ?, cooldown = ?, 
                    is_public = ?, permission = ?
                WHERE name = ?;
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, warp.getWorldName());
            stmt.setDouble(2, warp.getX());
            stmt.setDouble(3, warp.getY());
            stmt.setDouble(4, warp.getZ());
            stmt.setFloat(5, warp.getYaw());
            stmt.setFloat(6, warp.getPitch());
            stmt.setString(7, warp.getCategory());
            stmt.setString(8, warp.getIcon().name());
            stmt.setDouble(9, warp.getCost());
            stmt.setInt(10, warp.getCooldown());
            stmt.setBoolean(11, warp.isPublic());
            stmt.setString(12, warp.getPermission());
            stmt.setString(13, warp.getName());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error updating warp: " + warp.getName(), e);
            return false;
        }
    }

    public static boolean updateWarpUsage(String warpName) {
        String sql = """
                UPDATE warps SET 
                    last_used = NOW(),
                    usage_count = usage_count + 1
                WHERE name = ?;
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, warpName);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error updating warp usage: " + warpName, e);
            return false;
        }
    }

    // ==================== DELETE ====================
    public static boolean deleteWarp(String name) {
        String sql = "DELETE FROM warps WHERE name = ?;";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting warp: " + name, e);
            return false;
        }
    }

    // ==================== FAVORITOS ====================
    public static List<String> getFavoriteWarps(UUID playerId) {
        String sql = "SELECT warp_name FROM warp_favorites WHERE player_uuid = ? ORDER BY added_at;";
        List<String> favorites = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setObject(1, playerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                favorites.add(rs.getString("warp_name"));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting favorite warps for: " + playerId, e);
        }
        return favorites;
    }

    public static boolean addFavorite(UUID playerId, String warpName) {
        String sql = "INSERT INTO warp_favorites (player_uuid, warp_name) VALUES (?, ?) ON CONFLICT DO NOTHING;";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setObject(1, playerId);
            stmt.setString(2, warpName);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error adding favorite warp: " + warpName + " for " + playerId, e);
            return false;
        }
    }

    public static boolean removeFavorite(UUID playerId, String warpName) {
        String sql = "DELETE FROM warp_favorites WHERE player_uuid = ? AND warp_name = ?;";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setObject(1, playerId);
            stmt.setString(2, warpName);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing favorite warp: " + warpName + " for " + playerId, e);
            return false;
        }
    }

    // ==================== UTILIDADES ====================
    private static Warp mapResultSetToWarp(ResultSet rs) throws SQLException {
        Material icon;
        try {
            icon = Material.valueOf(rs.getString("icon"));
        } catch (IllegalArgumentException e) {
            icon = Material.COMPASS; // fallback
        }

        long createdAt = rs.getLong("created_at");
        long lastUsed = rs.getLong("last_used");
        if (rs.wasNull()) lastUsed = 0;

        return new Warp(
                rs.getString("name"),
                rs.getString("world_name"),
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
                rs.getFloat("yaw"),
                rs.getFloat("pitch"),
                rs.getString("category"),
                icon,
                rs.getDouble("cost"),
                rs.getInt("cooldown"),
                rs.getBoolean("is_public"),
                rs.getString("permission"),
                (UUID) rs.getObject("owner"),
                createdAt,
                lastUsed,
                rs.getInt("usage_count")
        );
    }

    public static List<String> getCategories() {
        String sql = "SELECT DISTINCT category FROM warps ORDER BY category;";
        List<String> categories = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting categories", e);
        }
        return categories;
    }

    public static boolean warpExists(String name) {
        String sql = "SELECT 1 FROM warps WHERE name = ? LIMIT 1;";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if warp exists: " + name, e);
            return false;
        }
    }
}
