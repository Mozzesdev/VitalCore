package me.winflix.vitalcore.general.database.collections.tribe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.Database;
import me.winflix.vitalcore.tribes.models.Invitation;
import me.winflix.vitalcore.tribes.models.User;

public class InvitationsDAO {

    private static final VitalCore plugin = VitalCore.getPlugin();

    public static void initialize(Connection connection) throws SQLException {
        String createTable = """
            CREATE TABLE IF NOT EXISTS invitations (
                id UUID PRIMARY KEY,
                type VARCHAR(16) NOT NULL CHECK (type IN ('TRIBE_TO_PLAYER', 'PLAYER_TO_TRIBE')),
                sender_id UUID NOT NULL,
                target_id UUID NOT NULL,
                created_at TIMESTAMP DEFAULT NOW()
            );
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_invitations 
                ON invitations (sender_id, target_id)
                """);
        }
    }

    public static List<Invitation> getUserInvitations(UUID userId) {
        List<Invitation> invitations = new ArrayList<>();
        String sql = """
            SELECT * FROM invitations 
            WHERE sender_id = ? OR target_id = ?
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, userId);
            pstmt.setObject(2, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                invitations.add(mapInvitationFromResultSet(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo invitaciones", e);
        }
        return invitations;
    }

    public static void updateUserInvitations(User user) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // Eliminar invitaciones antiguas
            deleteUserInvitations(conn, user.getId());

            // Insertar nuevas
            insertInvitations(conn, user.getInvitations());

            conn.commit();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error actualizando invitaciones", e);
        }
    }

    private static void deleteUserInvitations(Connection conn, UUID userId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
            "DELETE FROM invitations WHERE sender_id = ? OR target_id = ?")) 
        {
            pstmt.setObject(1, userId);
            pstmt.setObject(2, userId);
            pstmt.executeUpdate();
        }
    }

    private static void insertInvitations(Connection conn, List<Invitation> invitations) throws SQLException {
        String sql = """
            INSERT INTO invitations (id, type, sender_id, target_id, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Invitation inv : invitations) {
                pstmt.setObject(1, UUID.randomUUID());
                pstmt.setString(2, inv.getType().toString());
                pstmt.setObject(3, inv.getSenderId());
                pstmt.setObject(4, inv.getTargetId());
                pstmt.setTimestamp(5, new Timestamp(inv.getCreatedAt().getTime()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static Invitation mapInvitationFromResultSet(ResultSet rs) throws SQLException {
        Invitation inv = new Invitation();
        inv.setId(rs.getObject("id", UUID.class));
        inv.setType(Invitation.InvitationType.valueOf(rs.getString("type")));
        inv.setSenderId(rs.getObject("sender_id", UUID.class));
        inv.setTargetId(rs.getObject("target_id", UUID.class));
        inv.setCreatedAt(rs.getTimestamp("created_at"));
        return inv;
    }
}