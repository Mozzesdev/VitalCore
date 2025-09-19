package me.winflix.vitalcore.general.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.ChatColor;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.collections.tribe.TribesDAO;
import me.winflix.vitalcore.general.database.collections.tribe.UsersDAO;

public class Database {

    private static Connection connection;

    public static void connect() {
        try {
            // 1. Cargar explícitamente el driver (CRUCIAL para plugins)
            Class.forName("org.postgresql.Driver");

            // 2. Parámetros de conexión
            String url = "jdbc:postgresql://127.0.0.1:5432/overwilds";
            String user = "postgres";
            String password = "";

            // 3. Establecer conexión
            connection = DriverManager.getConnection(url, user, password);

            // 4. Verificar conexión
            if (connection != null && !connection.isClosed()) {
                TribesDAO.initialize(connection);
                UsersDAO.initialize(connection);
                VitalCore.Log.info(ChatColor.translateAlternateColorCodes('&', "&aConnected to PostgreSQL!"));
            }

        } catch (ClassNotFoundException e) {
            VitalCore.Log.info("Falta el driver PostgreSQL: " + e.getMessage());
        } catch (SQLException e) {
            VitalCore.Log.info("Error de conexión PostgreSQL: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect(); // Reconectar si es necesario
        }
        return connection;
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            VitalCore.Log.info("Error cerrando conexión: " + e.getMessage());
        }
    }
}