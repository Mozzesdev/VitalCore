package me.winflix.vitalcore.general.menu;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.dbassett.skullcreator.SkullCreator;
import me.winflix.vitalcore.general.interfaces.ConfirmMenuMessages;
import me.winflix.vitalcore.general.interfaces.ConfirmMessages;
import me.winflix.vitalcore.general.utils.Utils;

public class ConfirmMenu extends Menu implements ConfirmMenuMessages {

    ConfirmMessages confirmMessages;
    String menuName;

    public ConfirmMenu(PlayerMenuUtility playerMenuUtility, FileConfiguration messages,
            ConfirmMessages confirmMessages, String menuName) {
        super(playerMenuUtility);
        this.menuName = menuName;
        this.confirmMessages = confirmMessages;
    }

    @Override
    public String getMenuName() {
        return menuName;
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public String getConfirmMessages() {
        return confirmMessages.getConfirm();
    }

    @Override
    public List<String> getConfirmLore() {
        return confirmMessages.getConfirmLore();
    }

    @Override
    public String getDeniedMessages() {
        return confirmMessages.getCancel();
    }

    @Override
    public List<String> getDeniedLore() {
        return confirmMessages.getCancelLore();
    }

    public void handleMenu(InventoryClickEvent e) {
        ItemStack itemClicked = e.getCurrentItem();
        switch (itemClicked.getType()) {
            case PLAYER_HEAD:
                close(itemClicked.getItemMeta().getDisplayName()
                        .equalsIgnoreCase(Utils.useColors(getConfirmMessages())));
                break;
            default:
                break;
        }
    }

    @Deprecated
    @Override
    public void setMenuItems() {
        ItemStack emptyFill = createItem(Material.BLACK_STAINED_GLASS_PANE, "&7mc.overwild.com",
                Collections.emptyList());
        ItemStack okSkull = createSkullItem(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4NjA4NGRlYjdiZTcwYTEwMWFhZDdmZDY2ZjRjZDA5MTRiZGUxZmFkMzFkOWRkZDgxNGFiZDM4ZTlkYjg0NyJ9fX0=",
                getConfirmMessages(), getConfirmLore());
        ItemStack nopSkull = createSkullItem(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRkNDQ5MDdhNjc5ZjE3MzAzOTJmY2ZhOGZkYWRkZjJhYzc5OGVhZWI4YzRlMTA5MmQ0YmIwMDM3N2I2MTliOCJ9fX0=",
                getDeniedMessages(), getDeniedLore());

        inventory.setItem(11, okSkull);
        inventory.setItem(15, nopSkull);

        fillEmptySlots(emptyFill);
    }

    private ItemStack createItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.useColors(displayName));
        meta.setLore(lore.stream().map(Utils::useColors).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSkullItem(String base64Texture, String displayName, List<String> lore) {
        ItemStack item = SkullCreator.itemFromBase64(base64Texture);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.useColors(displayName));
        meta.setLore(lore.stream().map(Utils::useColors).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    private void fillEmptySlots(ItemStack fillerItem) {
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillerItem);
            }
        }
    }
}
