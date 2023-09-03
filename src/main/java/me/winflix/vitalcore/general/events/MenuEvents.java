package me.winflix.vitalcore.general.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import me.winflix.vitalcore.general.menu.Menu;


public class MenuEvents implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) {
            return;
        }
        
        InventoryHolder holder = e.getClickedInventory().getHolder();

        if (holder instanceof Menu) {
            e.setCancelled(true);

            Menu menu = (Menu) holder;
            menu.handleMenu(e);
        }
    }
}
