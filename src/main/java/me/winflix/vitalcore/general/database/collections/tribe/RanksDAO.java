package me.winflix.vitalcore.general.database.collections.tribe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.Database;
import me.winflix.vitalcore.tribes.models.Rank;

public class RanksDAO {

    private static final VitalCore plugin = VitalCore.getPlugin();

    // ==================== INICIALIZACIÓN ====================
    public static void initialize(Connection connection) throws SQLException {
        String createTable = """
                CREATE TABLE IF NOT EXISTS tribe_ranks (
                    id UUID PRIMARY KEY,
                    tribe_id UUID REFERENCES tribes(id) ON DELETE CASCADE,
                    name VARCHAR(16) NOT NULL,
                    tag VARCHAR(16) NOT NULL DEFAULT '',
                    can_invite BOOLEAN NOT NULL,
                    privilege INT NOT NULL
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
        }
    }

    // ==================== CREATE ====================
    public static void createRank(Rank rank) {
        try {
            Connection conn = Database.getConnection();
            createRank(rank, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error creando rango", e);
        }
    }

    public static void createRank(Rank rank, Connection conn) throws SQLException {
        String sql = """
                INSERT INTO tribe_ranks (id, tribe_id, name, tag, can_invite, privilege)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, rank.getId());
            pstmt.setObject(2, rank.getTribeId());
            pstmt.setString(3, rank.getName());
            pstmt.setString(4, rank.getTag());
            pstmt.setBoolean(5, rank.isCanInvite());
            pstmt.setInt(6, rank.getPrivilege());
            pstmt.executeUpdate();
        }
    }

    // ==================== READ ====================
    public static Rank getRankById(UUID rankId) {
        try {
            Connection conn = Database.getConnection();
            return getRankById(rankId, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo rango", e);
            return null;
        }
    }

    public static Rank getRankById(UUID rankId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tribe_ranks WHERE id = ?";
        Rank rank = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, rankId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                rank = mapRankFromResultSet(rs);
            }
        }
        return rank;
    }

    public static List<Rank> getRanksByTribe(UUID tribeId) {
        try {
            Connection conn = Database.getConnection();
            return getRanksByTribe(tribeId, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo rangos", e);
            return new ArrayList<>();
        }
    }

    public static List<Rank> getRanksByTribe(UUID tribeId, Connection conn) throws SQLException {
        List<Rank> ranks = new ArrayList<>();
        String sql = "SELECT * FROM tribe_ranks WHERE tribe_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, tribeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ranks.add(mapRankFromResultSet(rs));
            }
        }
        return ranks;
    }

    // ==================== UPDATE ====================
    public static void updateRank(Rank rank) {
        try {
            Connection conn = Database.getConnection();
            updateRank(rank, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error actualizando rango", e);
        }
    }

    public static void updateRank(Rank rank, Connection conn) throws SQLException {
        String sql = """
                UPDATE tribe_ranks
                SET name = ?, tag = ?, can_invite = ?, privilege = ?
                WHERE id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rank.getName());
            pstmt.setString(2, rank.getTag());
            pstmt.setBoolean(3, rank.isCanInvite());
            pstmt.setInt(4, rank.getPrivilege());
            pstmt.setObject(5, rank.getId());
            pstmt.executeUpdate();
        }
    }

    // ==================== DELETE ====================
    public static void deleteRank(UUID rankId) {
        try {
            Connection conn = Database.getConnection();
            deleteRank(rankId, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error eliminando rango", e);
        }
    }

    public static void deleteRank(UUID rankId, Connection conn) throws SQLException {
        String sql = "DELETE FROM tribe_ranks WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, rankId);
            pstmt.executeUpdate();
        }
    }

    // ==================== UTILIDADES ====================
    private static Rank mapRankFromResultSet(ResultSet rs) throws SQLException {
        Rank rank = new Rank(
                rs.getString("name"),
                rs.getString("tag"),
                rs.getBoolean("can_invite"),
                rs.getInt("privilege"),
                rs.getObject("tribe_id", UUID.class));
        rank.setId(rs.getObject("id", UUID.class));
        return rank;
    }
}