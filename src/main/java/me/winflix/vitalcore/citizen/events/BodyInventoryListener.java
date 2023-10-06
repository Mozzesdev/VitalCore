package me.winflix.vitalcore.npc.events;

import me.winflix.vitalcore.npc.models.Body;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class BodyInventoryListener implements Listener {
    private final Player player;
    private final Body body;
    private final Inventory bodyInventory;

    public BodyInventoryListener(Player player, Body body, Inventory bodyInventory) {
        this.player = player;
        this.body = body;
        this.bodyInventory = bodyInventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inventoryClicked = e.getClickedInventory();
        InventoryView view = e.getView();

        if (inventoryClicked.equals(view.getBottomInventory()) && e.getCurrentItem() == null && e.getCursor() == null) {
            e.setCancelled(true);
        }

    }


}
