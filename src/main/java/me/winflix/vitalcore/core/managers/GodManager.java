package me.winflix.vitalcore.core.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager para gestionar el modo god de los jugadores
 */
public class GodManager {
    
    // Mapa para rastrear jugadores en modo god
    private static final Map<UUID, Boolean> godModeStatus = new HashMap<>();
    
    /**
     * Obtiene el estado del modo god de un jugador
     * @param player El jugador
     * @return true si está en modo god, false en caso contrario
     */
    public static boolean isGodMode(Player player) {
        return godModeStatus.getOrDefault(player.getUniqueId(), false);
    }
    
    /**
     * Establece el modo god de un jugador
     * @param player El jugador
     * @param godMode true para activar, false para desactivar
     */
    public static void setGodMode(Player player, boolean godMode) {
        godModeStatus.put(player.getUniqueId(), godMode);
        player.setInvulnerable(godMode);
    }
    
    /**
     * Alterna el modo god de un jugador
     * @param player El jugador
     * @return El nuevo estado del modo god
     */
    public static boolean toggleGodMode(Player player) {
        boolean currentGodMode = isGodMode(player);
        boolean newGodMode = !currentGodMode;
        setGodMode(player, newGodMode);
        return newGodMode;
    }
    
    /**
     * Remueve el estado de god mode al desconectarse
     * @param player El jugador
     */
    public static void removeGodMode(Player player) {
        godModeStatus.remove(player.getUniqueId());
    }
    
    /**
     * Limpia todos los estados de god mode (reinicio del servidor)
     */
    public static void clearAllGodModes() {
        godModeStatus.clear();
    }
}
