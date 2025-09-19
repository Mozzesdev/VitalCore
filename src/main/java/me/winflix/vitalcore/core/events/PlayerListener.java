package me.winflix.vitalcore.core.events;

import me.winflix.vitalcore.core.managers.AfkManager;
import me.winflix.vitalcore.core.managers.BackManager;
import me.winflix.vitalcore.core.managers.GodManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener para eventos relacionados con jugadores
 * Maneja la limpieza de datos cuando los jugadores se desconectan
 * y registra ubicaciones de muerte para el comando /back
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BackManager.removePreviousLocation(event.getPlayer());
        GodManager.removeGodMode(event.getPlayer());
        AfkManager.removeAfk(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        BackManager.setPreviousLocation(event.getEntity());
    }
}
