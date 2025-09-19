package me.winflix.vitalcore.core.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager para gestionar las ubicaciones previas de los jugadores
 * Permite a los jugadores usar /back para regresar a su ubicación anterior
 */
public class BackManager {
    
    // Mapa para almacenar la ubicación previa de cada jugador
    private static final Map<UUID, Location> previousLocations = new HashMap<>();
    
    // Mapa para rastrear el cooldown del comando /back
    private static final Map<UUID, Long> backCooldowns = new HashMap<>();
    
    // Cooldown en segundos para usar /back
    private static final int BACK_COOLDOWN_SECONDS = 10;
    
    /**
     * Registra la ubicación actual del jugador como su ubicación previa
     * @param player El jugador cuya ubicación se va a registrar
     */
    public static void setPreviousLocation(Player player) {
        if (player != null && player.getLocation() != null) {
            previousLocations.put(player.getUniqueId(), player.getLocation().clone());
        }
    }
    
    /**
     * Obtiene la ubicación previa del jugador
     * @param player El jugador
     * @return La ubicación previa o null si no existe
     */
    public static Location getPreviousLocation(Player player) {
        return previousLocations.get(player.getUniqueId());
    }
    
    /**
     * Verifica si el jugador tiene una ubicación previa registrada
     * @param player El jugador
     * @return true si tiene ubicación previa, false en caso contrario
     */
    public static boolean hasPreviousLocation(Player player) {
        return previousLocations.containsKey(player.getUniqueId());
    }
    
    /**
     * Teletransporta al jugador a su ubicación previa (sistema A ↔ B)
     * @param player El jugador a teletransportar
     * @return true si el teletransporte fue exitoso, false en caso contrario
     */
    public static boolean teleportToPreviousLocation(Player player) {
        Location previousLocation = getPreviousLocation(player);
        
        if (previousLocation == null) {
            return false;
        }
        
        // Verificar que el mundo sigue siendo válido
        if (previousLocation.getWorld() == null) {
            previousLocations.remove(player.getUniqueId());
            return false;
        }
        
        // Guardar la ubicación actual como nueva ubicación previa
        Location currentLocation = player.getLocation().clone();
        
        // Teletransportar al jugador
        boolean teleportSuccess = player.teleport(previousLocation);
        
        if (teleportSuccess) {
            // Actualizar la ubicación previa con la ubicación desde donde vino (sistema A ↔ B)
            previousLocations.put(player.getUniqueId(), currentLocation);
        }
        
        return teleportSuccess;
    }
    
    /**
     * Verifica si el jugador puede usar el comando /back (cooldown)
     * @param player El jugador
     * @return true si puede usar el comando, false si está en cooldown
     */
    public static boolean canUseBack(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!backCooldowns.containsKey(playerId)) {
            return true;
        }
        
        long lastUsed = backCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = BACK_COOLDOWN_SECONDS * 1000L;
        
        return (currentTime - lastUsed) >= cooldownMillis;
    }
    
    /**
     * Obtiene el tiempo restante de cooldown en segundos
     * @param player El jugador
     * @return Segundos restantes de cooldown, 0 si no hay cooldown
     */
    public static int getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!backCooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long lastUsed = backCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = BACK_COOLDOWN_SECONDS * 1000L;
        long remainingMillis = cooldownMillis - (currentTime - lastUsed);
        
        return remainingMillis > 0 ? (int) (remainingMillis / 1000) + 1 : 0;
    }
    
    /**
     * Establece el cooldown para el jugador
     * @param player El jugador
     */
    public static void setBackCooldown(Player player) {
        backCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Remueve la ubicación previa del jugador (por ejemplo, cuando se desconecta)
     * @param player El jugador
     */
    public static void removePreviousLocation(Player player) {
        previousLocations.remove(player.getUniqueId());
        backCooldowns.remove(player.getUniqueId());
    }
    
    /**
     * Limpia todas las ubicaciones previas (por ejemplo, al reiniciar el servidor)
     */
    public static void clearAllPreviousLocations() {
        previousLocations.clear();
        backCooldowns.clear();
    }
}
