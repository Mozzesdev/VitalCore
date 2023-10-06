package me.winflix.vitalcore.citizen.listeners;

import me.winflix.vitalcore.citizen.models.DeathBody;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BodyInventoryListener implements Listener {
    private final Player whoOpen;
    private final DeathBody deathBody;
    private final Inventory bodyInventory;

    public BodyInventoryListener(Player whoOpen, DeathBody deathBody, Inventory bodyInventory) {
        this.deathBody = deathBody;
        this.bodyInventory = bodyInventory;
        this.whoOpen = whoOpen;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inventoryClicked = e.getClickedInventory();
        Inventory topInventory = e.getView().getTopInventory();
        ItemStack cursorItem = e.getCursor();

        if (e.getInventory().equals(bodyInventory)) {

            if(bodyInventory.isEmpty()){
                whoOpen.closeInventory();
                return;
            }

            if (inventoryClicked != null && inventoryClicked.equals(topInventory)) {

                if (e.getClick() == ClickType.NUMBER_KEY || !cursorItem.getType().isAir()) {
                    e.setCancelled(true);
                }
                List<ItemStack> itemsTaken = deathBody.getItemsTaken().get(whoOpen.getUniqueId());

                if (e.getCurrentItem() != null) {
                    itemsTaken.add(e.getCurrentItem());
                }
            }

            if (inventoryClicked != null && inventoryClicked.equals(e.getView().getBottomInventory()) &&
                    e.isShiftClick()) {
                e.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory.equals(bodyInventory)) {
            e.setCancelled(true);
        }
    }

}
