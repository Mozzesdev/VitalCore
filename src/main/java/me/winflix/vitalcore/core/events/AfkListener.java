package me.winflix.vitalcore.core.events;

import me.winflix.vitalcore.core.managers.AfkManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener para quitar automáticamente el estado AFK cuando el jugador está activo
 */
public class AfkListener implements Listener {
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Registrar actividad del jugador
        AfkManager.updateActivity(player);
        
        // Solo procesar si el jugador se movió (no solo rotó la cabeza)
        if (event.getFrom().getX() != event.getTo().getX() || 
            event.getFrom().getZ() != event.getTo().getZ() || 
            event.getFrom().getY() != event.getTo().getY()) {
            
            removeAfkIfActive(player);
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Registrar actividad del jugador
        AfkManager.updateActivity(event.getPlayer());
        removeAfkIfActive(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Registrar actividad del jugador
        AfkManager.updateActivity(event.getPlayer());
        
        // No quitar AFK si el jugador usa el comando /afk
        if (!event.getMessage().toLowerCase().startsWith("/afk")) {
            removeAfkIfActive(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Inicializar actividad del jugador al conectarse
        AfkManager.updateActivity(event.getPlayer());
    }
    
    /**
     * Remueve el estado AFK si el jugador está actualmente AFK
     * @param player El jugador
     */
    private void removeAfkIfActive(Player player) {
        if (AfkManager.isAfk(player)) {
            String afkTime = AfkManager.getFormattedAfkTime(player);
            AfkManager.setAfk(player, false, null);
            
            player.sendMessage(ChatColor.GREEN + "Ya no estás AFK. " + ChatColor.GRAY + "(Estuviste AFK por " + afkTime + ")");
            
            // Anunciar a otros jugadores
            String announcement = ChatColor.GREEN + player.getName() + ChatColor.GRAY + " ya no está AFK.";
            AfkManager.announceToOthers(player, announcement);
            
            // Restaurar el display name normal
            AfkManager.removeAfkDisplayName(player);
        }
    }
}
