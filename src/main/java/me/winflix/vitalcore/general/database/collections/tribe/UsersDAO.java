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

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.Database;
import me.winflix.vitalcore.tribes.files.UserFile;
import me.winflix.vitalcore.tribes.models.Tribe;
import me.winflix.vitalcore.tribes.models.TribeMember;
import me.winflix.vitalcore.tribes.models.User;

public class UsersDAO {

    private static final VitalCore plugin = VitalCore.getPlugin();

    // ==================== INICIALIZACIÓN ====================
    public static void initialize(Connection connection) throws SQLException {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id UUID PRIMARY KEY,
                    player_name VARCHAR(16) NOT NULL,
                    tribe_id UUID REFERENCES tribes(id) ON DELETE SET NULL
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
        }
    }

    public static User createUser(Player player) {
        UUID userId = player.getUniqueId();
        User user = new User(player.getName(), userId, null);

        try (Connection conn = Database.getConnection()) {
            String sql = """
                    INSERT INTO users (id, player_name)
                    VALUES (?, ?)
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setObject(1, userId);
                pstmt.setString(2, user.getPlayerName());
                pstmt.executeUpdate();
            }

            saveYamlFile(user);

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error creando usuario", e);
        }
        return user;
    }

    public static User getUser(UUID userId) {
        String sql = """
                SELECT u.*, t.id as tribe_uuid, t.name as tribe_name
                FROM users u
                LEFT JOIN tribes t ON u.tribe_id = t.id
                WHERE u.id = ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapFullUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo usuario", e);
        }
        return null;
    }

    public static User saveUser(User user) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // Actualizar datos básicos
            String updateUser = """
                    UPDATE users
                    SET player_name = ?, tribe_id = ?
                    WHERE id = ?
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(updateUser)) {
                pstmt.setString(1, user.getPlayerName());
                pstmt.setObject(2, user.getTribe() != null ? user.getTribe().getId() : null);
                pstmt.setObject(3, user.getId());
                pstmt.executeUpdate();
            }

            conn.commit();
            saveYamlFile(user);

            VitalCore.Log.info("Usuario actualizado: " + user.getPlayerName());

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error actualizando usuario", e);
        }
        return user;
    }

    public static void deleteUser(UUID userId) {
        try (Connection conn = Database.getConnection()) {
            // Eliminar usuario
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM users WHERE id = ?")) {
                pstmt.setObject(1, userId);
                pstmt.executeUpdate();
            }

            // Eliminar archivo YAML
            if (VitalCore.fileManager.getConfigFile().getBoolean("tribe.allow_yaml")) {
                UserFile userFile = VitalCore.fileManager.getUserFile(userId.toString());
                if (userFile.getFile().delete()) {
                    VitalCore.fileManager.getUsersFiles().remove(userFile);
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error eliminando usuario", e);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================
    private static User mapFullUserFromResultSet(ResultSet rs) throws SQLException {
        UUID userId = rs.getObject("id", UUID.class);
        User user = new User(
                rs.getString("player_name"),
                userId,
                null);

        // Cargar tribu
        UUID tribeId = rs.getObject("tribe_uuid", UUID.class);
        if (tribeId != null) {
            user.setTribe(new Tribe(
                    rs.getString("tribe_name"),
                    tribeId));
        }

        return user;
    }

    private static void saveYamlFile(User user) {
        if (!VitalCore.fileManager.getConfigFile().getBoolean("tribe.allow_yaml"))
            return;

        UserFile userFile = new UserFile(
                plugin,
                user.getId().toString(),
                "users",
                user);

        VitalCore.fileManager.getUsersFiles().removeIf(f -> f.getUser().getId().equals(user.getId()));
        VitalCore.fileManager.getUsersFiles().add(userFile);
    }

    public static List<User> getUsersInTribe(UUID tribeId) {
        List<User> users = new ArrayList<>();

        // Obtener miembros de la tribu
        List<TribeMember> members = TribeMembersDAO.getTribeMembersByTribe(tribeId);

        for (TribeMember member : members) {
            // Obtener User completo desde la base de datos
            User user = UsersDAO.getUser(member.getPlayerId());

            if (user != null) {
                // Sincronizar datos específicos de la membresía
                user.setTribe(TribesDAO.getTribeById(tribeId)); // Cargar tribu completa
                user.getTribe().addMember(member); // Mantener relación bidireccional
                users.add(user);
            }
        }

        return users;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = """
                SELECT u.*, t.id AS tribe_id, t.name AS tribe_name
                FROM users u
                LEFT JOIN tribes t ON u.tribe_id = t.id
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Mapear datos base del usuario
                User user = new User(
                        rs.getString("player_name"),
                        rs.getObject("id", UUID.class),
                        null);

                // Cargar tribu si existe
                UUID tribeId = rs.getObject("tribe_id", UUID.class);
                if (tribeId != null) {
                    user.setTribe(new Tribe(
                            rs.getString("tribe_name"),
                            tribeId));
                }

                // Cargar invitaciones
                user.setInvitations(InvitationsDAO.getUserInvitations(user.getId()));

                users.add(user);
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo todos los usuarios", e);
        }
        return users;
    }
}