package me.winflix.vitalcore.general.database.collections.tribe;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.Database;
import me.winflix.vitalcore.tribes.files.TribeFile;
import me.winflix.vitalcore.tribes.models.Rank;
import me.winflix.vitalcore.tribes.models.Tribe;
import me.winflix.vitalcore.tribes.models.TribeMember;
import me.winflix.vitalcore.tribes.utils.RankManager;

public class TribesDAO {

    private static final VitalCore plugin = VitalCore.getPlugin();

    // ==================== INICIALIZACIÓN ====================
    public static void initialize(Connection connection) throws SQLException {
        String createTribesTable = """
                CREATE TABLE IF NOT EXISTS tribes (
                    id UUID PRIMARY KEY,
                    name VARCHAR(32) NOT NULL UNIQUE,
                    description TEXT NOT NULL DEFAULT '',
                    home VARCHAR(255) NOT NULL DEFAULT '',
                    tag VARCHAR(10) NOT NULL DEFAULT '',
                    is_open BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT NOW()
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTribesTable);
            RanksDAO.initialize(connection);
            TribeMembersDAO.initialize(connection);
        }
    }

    // ==================== CREATE ====================
    public static Tribe createTribe(Player player, String name, String tag) {
        UUID tribeId = UUID.randomUUID();
        Tribe tribe = new Tribe(name, tribeId);
        tribe.setTag(tag);
        tribe.setOpen(true);

        TribeMember owner = new TribeMember(
                player.getName(),
                player.getUniqueId(),
                tribeId);

        Rank ownerRank = RankManager.OWNER_RANK;
        ownerRank.setTribeId(tribeId);
        owner.setRange(ownerRank);
        tribe.addMember(owner);

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);  // Inicio de transacción

            // 1. Insertar tribu
            String insertTribe = """
                    INSERT INTO tribes (id, name, tag, is_open)
                    VALUES (?, ?, ?, ?)
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(insertTribe)) {
                pstmt.setObject(1, tribeId);
                pstmt.setString(2, name);
                pstmt.setString(3, tag);
                pstmt.setBoolean(4, true);
                pstmt.executeUpdate();
            }

            // 2. Crear rangos iniciales
            for (Rank defaultRank : RankManager.DEFAULT_RANKS) {
                defaultRank.setTribeId(tribeId);
                RanksDAO.createRank(defaultRank, conn);  // Conexión compartida
            }

            // 3. Guardar miembro
            TribeMembersDAO.createTribeMember(owner, conn);

            conn.commit();  // Confirmar transacción

            // 4. Crear archivo YAML
            if (VitalCore.fileManager.getConfigFile().getBoolean("tribe.allow_yaml")) {
                TribeFile tribeFile = new TribeFile(plugin, tribeId.toString(), "tribes", tribe);
                VitalCore.fileManager.getTribesFiles().add(tribeFile);
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();  // Rollback en caso de error
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Error al hacer rollback", ex);
                }
            }
            plugin.getLogger().log(Level.SEVERE, "Error creando tribu", e);
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // Restaurar auto-commit
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error restableciendo auto-commit", e);
                }
            }
        }
        return tribe;
    }

    // ==================== READ ====================
    public static Tribe getTribeById(UUID id) {
        Tribe tribe = null;
        String tribeSql = "SELECT * FROM tribes WHERE id = ?";
        Connection conn = null;

        try {
            conn = Database.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(tribeSql)) {
                pstmt.setObject(1, id);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    tribe = mapTribeFromResultSet(rs);
                    tribe.setRanks(RanksDAO.getRanksByTribe(id, conn));  // Conexión persistente
                    tribe.setMembers(TribeMembersDAO.getTribeMembersByTribe(id, conn));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo tribu", e);
        }
        return tribe;
    }

    public static Tribe getTribeByName(String name) {
        Tribe tribe = null;
        String tribeSql = "SELECT * FROM tribes WHERE name = ?";
        Connection conn = null;

        try {
            conn = Database.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(tribeSql)) {
                pstmt.setObject(1, name);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    tribe = mapTribeFromResultSet(rs);
                    tribe.setRanks(RanksDAO.getRanksByTribe(tribe.getId(), conn));
                    tribe.setMembers(TribeMembersDAO.getTribeMembersByTribe(tribe.getId(), conn));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo tribu", e);
        }
        return tribe;
    }

    public static Tribe getTribeByMember(UUID memberId) {
        String sql = """
                SELECT t.*
                FROM tribes t
                INNER JOIN tribe_members tm ON t.id = tm.tribe_id
                WHERE tm.player_id = ?
                """;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setObject(1, memberId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    Tribe tribe = mapTribeFromResultSet(rs);
                    tribe.setMembers(TribeMembersDAO.getTribeMembersByTribe(tribe.getId(), conn));
                    tribe.setRanks(RanksDAO.getRanksByTribe(tribe.getId(), conn));
                    return tribe;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo tribu del miembro", e);
        }
        return null;
    }

    public static List<Tribe> getAllTribes() {
        List<Tribe> tribes = new ArrayList<>();
        String sql = "SELECT * FROM tribes";
        Connection conn = null;

        try {
            conn = Database.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Tribe tribe = mapTribeFromResultSet(rs);
                    tribe.setRanks(RanksDAO.getRanksByTribe(tribe.getId(), conn));
                    tribe.setMembers(TribeMembersDAO.getTribeMembersByTribe(tribe.getId(), conn));
                    tribes.add(tribe);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error obteniendo tribus", e);
        }
        return tribes;
    }

    // ==================== UPDATE ====================
    public static void saveTribe(Tribe tribe) {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);  // Iniciar transacción

            // 1. Actualizar datos básicos
            String updateTribe = """
                    UPDATE tribes
                    SET name = ?, description = ?, home = ?, tag = ?, is_open = ?
                    WHERE id = ?
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(updateTribe)) {
                pstmt.setString(1, tribe.getTribeName());
                pstmt.setString(2, tribe.getDescription());
                pstmt.setString(3, tribe.getTribeHome());
                pstmt.setString(4, tribe.getTag());
                pstmt.setBoolean(5, tribe.isOpen());
                pstmt.setObject(6, tribe.getId());
                pstmt.executeUpdate();
            }

            // 2. Sincronizar rangos y miembros
            syncRanks(tribe, conn);  // Conexión compartida
            syncMembers(tribe, conn);

            conn.commit();  // Confirmar cambios

            // 3. Actualizar YAML
            if (VitalCore.fileManager.getConfigFile().getBoolean("tribe.allow_yaml")) {
                updateYamlFile(tribe);
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            plugin.getLogger().log(Level.SEVERE, "Error actualizando tribu", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error restableciendo auto-commit", e);
                }
            }
        }
    }

    // ==================== DELETE ====================
    public static void deleteTribe(Tribe tribe) {
        String sql = "DELETE FROM tribes WHERE id = ?";
        Connection conn = null;

        try {
            conn = Database.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setObject(1, tribe.getId());
                pstmt.executeUpdate();
            }

            if (VitalCore.fileManager.getConfigFile().getBoolean("tribe.allow_yaml")) {
                TribeFile tribeFile = VitalCore.fileManager.getTribeFile(tribe.getId().toString());
                if (tribeFile.getFile().delete()) {
                    VitalCore.fileManager.getTribesFiles().remove(tribeFile);
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error eliminando tribu", e);
        }
    }

    // ==================== SYNC HELPERS ====================
    private static void syncRanks(Tribe tribe, Connection conn) throws SQLException {
        List<Rank> existingRanks = RanksDAO.getRanksByTribe(tribe.getId(), conn);
        for (Rank existing : existingRanks) {
            if (!tribe.getRanks().contains(existing)) {
                RanksDAO.deleteRank(existing.getId(), conn);
            }
        }

        for (Rank rank : tribe.getRanks()) {
            if (RanksDAO.getRankById(rank.getId(), conn) == null) {
                RanksDAO.createRank(rank, conn);
            } else {
                RanksDAO.updateRank(rank, conn);
            }
        }
    }

    private static void syncMembers(Tribe tribe, Connection conn) throws SQLException {
        List<TribeMember> existingMembers = TribeMembersDAO.getTribeMembersByTribe(tribe.getId(), conn);
        for (TribeMember existing : existingMembers) {
            if (!tribe.getMembers().contains(existing)) {
                TribeMembersDAO.deleteTribeMember(existing.getPlayerId(), conn);
            }
        }

        for (TribeMember member : tribe.getMembers()) {
            if (TribeMembersDAO.getTribeMemberById(member.getPlayerId(), conn) == null) {
                TribeMembersDAO.createTribeMember(member, conn);
            } else {
                TribeMembersDAO.updateTribeMember(member, conn);
            }
        }
    }

    // ==================== UTILITIES ====================
    private static Tribe mapTribeFromResultSet(ResultSet rs) throws SQLException {
        Tribe tribe = new Tribe(
                rs.getString("name"),
                rs.getObject("id", UUID.class));
        tribe.setDescription(rs.getString("description"));
        tribe.setTribeHome(rs.getString("home"));
        tribe.setTag(rs.getString("tag"));
        tribe.setOpen(rs.getBoolean("is_open"));
        return tribe;
    }

    private static void updateYamlFile(Tribe tribe) {
        TribeFile oldFile = VitalCore.fileManager.getTribeFile(tribe.getId().toString());
        if (oldFile.getFile().exists()) oldFile.getFile().delete();

        TribeFile newFile = new TribeFile(plugin, tribe.getId().toString(), "tribes", tribe);
        VitalCore.fileManager.getTribesFiles().add(newFile);
    }
}
