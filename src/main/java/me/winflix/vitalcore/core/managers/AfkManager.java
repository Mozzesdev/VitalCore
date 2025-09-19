package me.winflix.vitalcore.core.managers;

import me.winflix.vitalcore.VitalCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager para gestionar el estado AFK de los jugadores e inactividad automática
 */
public class AfkManager {
    
    // Tiempo de inactividad en milisegundos (5 minutos)
    private static final long INACTIVITY_TIME = 5 * 60 * 1000; // 5 minutos
    
    // Tiempo para mostrar en lista de inactivos (3 minutos)
    public static final long INACTIVITY_WARNING_TIME = 3 * 60 * 1000; // 3 minutos
    
    // Tiempo de verificación en ticks (cada 30 segundos)
    private static final long CHECK_INTERVAL = 30 * 20; // 30 segundos en ticks
    
    // Mapa para rastrear jugadores en modo AFK
    private static final Map<UUID, Boolean> afkStatus = new HashMap<>();
    
    // Mapa para almacenar el motivo del AFK
    private static final Map<UUID, String> afkReasons = new HashMap<>();
    
    // Mapa para rastrear el tiempo cuando entraron en AFK
    private static final Map<UUID, Long> afkStartTime = new HashMap<>();
    
    // Mapa para rastrear la última actividad de cada jugador
    private static final Map<UUID, Long> lastActivity = new HashMap<>();
    
    // Task para verificar inactividad
    private static BukkitTask inactivityTask;
    
    /**
     * Obtiene el estado AFK de un jugador
     * @param player El jugador
     * @return true si está AFK, false en caso contrario
     */
    public static boolean isAfk(Player player) {
        return afkStatus.getOrDefault(player.getUniqueId(), false);
    }
    
    /**
     * Establece el estado AFK de un jugador
     * @param player El jugador
     * @param afk true para activar AFK, false para desactivar
     * @param reason El motivo del AFK (puede ser null)
     */
    public static void setAfk(Player player, boolean afk, String reason) {
        UUID playerId = player.getUniqueId();
        
        if (afk) {
            afkStatus.put(playerId, true);
            afkStartTime.put(playerId, System.currentTimeMillis());
            if (reason != null && !reason.trim().isEmpty()) {
                afkReasons.put(playerId, reason.trim());
            } else {
                afkReasons.remove(playerId);
            }
        } else {
            afkStatus.remove(playerId);
            afkReasons.remove(playerId);
            afkStartTime.remove(playerId);
        }
    }
    
    /**
     * Alterna el estado AFK de un jugador
     * @param player El jugador
     * @param reason El motivo del AFK (solo se usa si se activa AFK)
     * @return El nuevo estado AFK
     */
    public static boolean toggleAfk(Player player, String reason) {
        boolean currentAfk = isAfk(player);
        boolean newAfk = !currentAfk;
        setAfk(player, newAfk, reason);
        return newAfk;
    }
    
    /**
     * Obtiene el motivo del AFK de un jugador
     * @param player El jugador
     * @return El motivo del AFK o null si no tiene motivo
     */
    public static String getAfkReason(Player player) {
        return afkReasons.get(player.getUniqueId());
    }
    
    /**
     * Obtiene el tiempo que lleva un jugador en AFK (en milisegundos)
     * @param player El jugador
     * @return Tiempo en AFK en milisegundos, 0 si no está AFK
     */
    public static long getAfkTime(Player player) {
        if (!isAfk(player)) {
            return 0;
        }
        Long startTime = afkStartTime.get(player.getUniqueId());
        if (startTime == null) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Obtiene el tiempo que lleva un jugador en AFK formateado
     * @param player El jugador
     * @return Tiempo formateado (ej: "5m 30s")
     */
    public static String getFormattedAfkTime(Player player) {
        long timeMs = getAfkTime(player);
        return formatTime(timeMs);
    }
    
    /**
     * Remueve el estado AFK al desconectarse
     * @param player El jugador
     */
    public static void removeAfk(Player player) {
        UUID playerId = player.getUniqueId();
        afkStatus.remove(playerId);
        afkReasons.remove(playerId);
        afkStartTime.remove(playerId);
        lastActivity.remove(playerId);
    }
    
    /**
     * Limpia todos los estados AFK (reinicio del servidor)
     */
    public static void clearAllAfk() {
        afkStatus.clear();
        afkReasons.clear();
        afkStartTime.clear();
        lastActivity.clear();
        
        // Detener el task de inactividad
        if (inactivityTask != null) {
            inactivityTask.cancel();
            inactivityTask = null;
        }
    }
    
    /**
     * Inicia el sistema de verificación de inactividad
     * @param plugin El plugin principal
     */
    public static void startInactivityChecker(VitalCore plugin) {
        // Detener y limpiar cualquier instancia anterior (importante para reloads)
        if (inactivityTask != null) {
            inactivityTask.cancel();
            inactivityTask = null;
        }
        
        // Limpiar todos los datos AFK anteriores al reiniciar el sistema
        afkStatus.clear();
        afkReasons.clear();
        afkStartTime.clear();
        lastActivity.clear();
        
        // Normalizar los nombres de todos los jugadores online (remover [AFK] si lo tienen)
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeAfkDisplayName(player);
        }
        
        inactivityTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkInactivity();
            }
        }.runTaskTimer(plugin, CHECK_INTERVAL, CHECK_INTERVAL);
    }
    
    /**
     * Detiene el sistema de verificación de inactividad
     */
    public static void stopInactivityChecker() {
        if (inactivityTask != null) {
            inactivityTask.cancel();
            inactivityTask = null;
        }
        
        // Normalizar los nombres de todos los jugadores online antes de limpiar
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeAfkDisplayName(player);
        }
        
        // Limpiar todos los datos AFK cuando se detiene el sistema
        afkStatus.clear();
        afkReasons.clear();
        afkStartTime.clear();
        lastActivity.clear();
    }
    
    /**
     * Actualiza la última actividad de un jugador
     * @param player El jugador
     */
    public static void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Obtiene el tiempo de inactividad de un jugador
     * @param player El jugador
     * @return Tiempo de inactividad en milisegundos
     */
    public static long getInactivityTime(Player player) {
        Long lastActivityTime = lastActivity.get(player.getUniqueId());
        if (lastActivityTime == null) {
            // Si no hay registro, consideramos que acaba de conectarse
            updateActivity(player);
            return 0;
        }
        return System.currentTimeMillis() - lastActivityTime;
    }
    
    /**
     * Verifica si un jugador está inactivo (más de 5 minutos sin actividad)
     * @param player El jugador
     * @return true si está inactivo, false en caso contrario
     */
    public static boolean isInactive(Player player) {
        return getInactivityTime(player) >= INACTIVITY_TIME;
    }
    
    /**
     * Obtiene el tiempo de inactividad formateado
     * @param player El jugador
     * @return Tiempo formateado (ej: "2m 30s")
     */
    public static String getFormattedInactivityTime(Player player) {
        long timeMs = getInactivityTime(player);
        return formatTime(timeMs);
    }
    
    /**
     * Formatea un tiempo en milisegundos a formato legible
     * @param timeMs Tiempo en milisegundos
     * @return Tiempo formateado (ej: "5m 30s")
     */
    private static String formatTime(long timeMs) {
        if (timeMs == 0) {
            return "0s";
        }
        
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds = seconds % 60;
        minutes = minutes % 60;
        
        StringBuilder timeStr = new StringBuilder();
        if (hours > 0) {
            timeStr.append(hours).append("h ");
        }
        if (minutes > 0) {
            timeStr.append(minutes).append("m ");
        }
        if (seconds > 0 || timeStr.length() == 0) {
            timeStr.append(seconds).append("s");
        }
        
        return timeStr.toString().trim();
    }
    
    /**
     * Establece el display name de AFK para un jugador
     * @param player El jugador
     */
    public static void setAfkDisplayName(Player player) {
        player.setDisplayName(ChatColor.GRAY + "[AFK] " + ChatColor.RESET + player.getName());
        player.setPlayerListName(ChatColor.GRAY + "[AFK] " + ChatColor.RESET + player.getName());
    }
    
    /**
     * Restaura el display name normal de un jugador
     * @param player El jugador
     */
    public static void removeAfkDisplayName(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
    }
    
    /**
     * Anuncia a todos los jugadores (excepto al especificado) un mensaje
     * @param excludePlayer El jugador a excluir del anuncio
     * @param message El mensaje a anunciar
     */
    public static void announceToOthers(Player excludePlayer, String message) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(excludePlayer)) {
                onlinePlayer.sendMessage(message);
            }
        }
    }
    
    /**
     * Verifica la inactividad de todos los jugadores conectados
     */
    private static void checkInactivity() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Solo verificar jugadores que no están ya en AFK
            if (!isAfk(player) && isInactive(player)) {
                setPlayerAfkForInactivity(player);
            }
        }
    }
    
    /**
     * Pone a un jugador en AFK por inactividad
     * @param player El jugador
     */
    private static void setPlayerAfkForInactivity(Player player) {
        // Activar AFK con motivo automático
        setAfk(player, true, "Inactividad");
        
        // Notificar al jugador
        player.sendMessage(ChatColor.YELLOW + "Has sido puesto en AFK automáticamente por inactividad.");
        
        // Anunciar a otros jugadores
        String announcement = ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " está AFK: " + ChatColor.WHITE + "Inactividad";
        announceToOthers(player, announcement);
        
        // Cambiar el display name para mostrar [AFK]
        setAfkDisplayName(player);
    }
}
