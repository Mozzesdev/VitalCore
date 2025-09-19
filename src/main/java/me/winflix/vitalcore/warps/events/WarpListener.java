package me.winflix.vitalcore.warps.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.InventoryHolder;

import me.winflix.vitalcore.warps.WarpManager;
import me.winflix.vitalcore.warps.menu.WarpMenu;

/**
 * Listener para eventos relacionados con el sistema de warps
 */
public class WarpListener implements Listener {

    private final WarpManager warpManager;

    public WarpListener(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    /**
     * Maneja los clics en el menú de warps
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof WarpMenu) {
            event.setCancelled(true);
            
            if (event.getWhoClicked() instanceof Player) {
                WarpMenu menu = (WarpMenu) holder;
                menu.handleMenu(event);
            }
        }
    }

    /**
     * Cancela el warmup si el jugador se mueve demasiado
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Verificar si el jugador tiene un warmup activo
        if (warpManager.hasActiveWarmup(player)) {
            // Verificar si se movió significativamente
            if (event.getFrom().distance(event.getTo()) > 0.1) {
                warpManager.cancelWarmup(player);
            }
        }
    }
}
