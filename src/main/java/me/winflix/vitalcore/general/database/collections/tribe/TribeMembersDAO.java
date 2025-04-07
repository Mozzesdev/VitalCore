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
import me.winflix.vitalcore.tribes.models.TribeMember;

public class TribeMembersDAO {

    private static final VitalCore plugin = VitalCore.getPlugin();

    // ==================== INICIALIZACIÓN ====================
    public static void initialize(Connection connection) throws SQLException {
        String createTable = """
                CREATE TABLE IF NOT EXISTS tribe_members (
                    player_id UUID PRIMARY KEY,
                    player_name VARCHAR(16) NOT NULL,
                    tribe_id UUID REFERENCES tribes(id) ON DELETE CASCADE,
                    rank_id UUID REFERENCES tribe_ranks(id) ON DELETE SET NULL
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
        }
    }

    // ==================== CREATE ====================
    public static void createTribeMember(TribeMember member, Connection conn) throws SQLException {
        String sql = """
                INSERT INTO tribe_members (player_id, player_name, tribe_id, rank_id)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, member.getPlayerId());
            pstmt.setString(2, member.getPlayerName());
            pstmt.setObject(3, member.getTribeId());
            pstmt.setObject(4, member.getRange() != null ? member.getRange().getId() : null);
            pstmt.executeUpdate();
        }
    }

    public static void createTribeMember(TribeMember member) {
        try {
            Connection conn = Database.getConnection();
            createTribeMember(member, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error creando miembro", e);
        }
    }

    // ==================== READ ====================
    public static TribeMember getTribeMemberById(UUID playerId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tribe_members WHERE player_id = ?";
        TribeMember member = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, playerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                member = mapTribeMemberFromResultSet(rs, conn);
            }
        }
        return member;
    }

    public static TribeMember getTribeMemberById(UUID playerId) {
        try {
            Connection conn = Database.getConnection();
            return getTribeMemberById(playerId, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo miembro", e);
            return null;
        }
    }

    public static List<TribeMember> getTribeMembersByTribe(UUID tribeId, Connection conn) throws SQLException {
        List<TribeMember> members = new ArrayList<>();
        String sql = "SELECT * FROM tribe_members WHERE tribe_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, tribeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                members.add(mapTribeMemberFromResultSet(rs, conn));
            }
        }
        return members;
    }

    public static List<TribeMember> getTribeMembersByTribe(UUID tribeId) {
        try {
            Connection conn = Database.getConnection();
            return getTribeMembersByTribe(tribeId, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo miembros", e);
            return new ArrayList<>();
        }
    }

    // ==================== UPDATE ====================
    public static void updateTribeMember(TribeMember member, Connection conn) throws SQLException {
        String sql = """
                UPDATE tribe_members
                SET player_name = ?, tribe_id = ?, rank_id = ?
                WHERE player_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, member.getPlayerName());
            pstmt.setObject(2, member.getTribeId());
            pstmt.setObject(3, member.getRange() != null ? member.getRange().getId() : null);
            pstmt.setObject(4, member.getPlayerId());
            pstmt.executeUpdate();
        }
    }

    public static void updateTribeMember(TribeMember member) {
        try {
            Connection conn = Database.getConnection();
            updateTribeMember(member, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error actualizando miembro", e);
        }
    }

    // ==================== DELETE ====================
    public static void deleteTribeMember(UUID playerId, Connection conn) throws SQLException {
        String sql = "DELETE FROM tribe_members WHERE player_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, playerId);
            pstmt.executeUpdate();
        }
    }

    public static void deleteTribeMember(UUID playerId) {
        try {
            Connection conn = Database.getConnection();
            deleteTribeMember(playerId, conn);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error eliminando miembro", e);
        }
    }

    // ==================== UTILIDADES ====================
    private static TribeMember mapTribeMemberFromResultSet(ResultSet rs, Connection conn) throws SQLException {
        TribeMember member = new TribeMember(
                rs.getString("player_name"),
                rs.getObject("player_id", UUID.class),
                rs.getObject("tribe_id", UUID.class));

        UUID rankId = rs.getObject("rank_id", UUID.class);
        if (rankId != null) {
            member.setRange(RanksDAO.getRankById(rankId, conn)); // Usa conexión compartida
        }

        return member;
    }
}