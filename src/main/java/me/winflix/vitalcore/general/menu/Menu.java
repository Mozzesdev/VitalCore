package me.winflix.vitalcore.general.menu;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.winflix.vitalcore.general.utils.Utils;

public abstract class Menu implements InventoryHolder {
    protected Inventory inventory;
    protected PlayerMenuUtility playerMenuUtility;
    protected Consumer<Boolean> afterCloseCallback;

    public Menu(PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
    }

    protected ItemStack FILLER_GLASS = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

    public abstract String getMenuName();

    public abstract int getSlots();

    public abstract void handleMenu(InventoryClickEvent e);

    public abstract void setMenuItems();

    public void afterClose(Consumer<Boolean> callback) {
        afterCloseCallback = callback;
    }

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), Utils.useColors(getMenuName()));
        this.setMenuItems();
        playerMenuUtility.getOwner().openInventory(inventory);
    }

    public void close(boolean condition) {
        playerMenuUtility.getOwner().closeInventory();
        if (afterCloseCallback != null) {
            afterCloseCallback.accept(condition);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
