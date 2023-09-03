package me.winflix.vitalcore.tribe.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.dbassett.skullcreator.SkullCreator;
import me.winflix.vitalcore.general.menu.Menu;
import me.winflix.vitalcore.general.menu.PlayerMenuUtility;
import me.winflix.vitalcore.general.utils.PlayerUtils;
import me.winflix.vitalcore.general.utils.Utils;

public class TribeMenu extends Menu {

    public TribeMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Tribe Menu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack itemClicked = e.getCurrentItem();
        switch (itemClicked.getType()) {
            case PLAYER_HEAD:
                if (itemClicked.getItemMeta().getDisplayName().equals(Utils.useColors("&bMembers"))) {
                    new MembersMenu(playerMenuUtility).open();
                }
                break;
            default:
                break;
        }
    }

    @Deprecated
    @Override
    public void setMenuItems() {

        // Fill Item
        ItemStack emptyFill = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta emptyFillMeta = emptyFill.getItemMeta();
        emptyFillMeta.setDisplayName("mc.overwild.com");
        emptyFill.setItemMeta(emptyFillMeta);

        // Player Clicked Item
        ItemStack playerSkull = PlayerUtils.getPlayerSkull(playerMenuUtility.getOwner().getDisplayName(),
                playerMenuUtility.getOwner().getUniqueId());

        // Members Item
        String members64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjhlMTZiZjlkNTYxNTlkZjI1ODlmZjc2NTZmODdjYWYwZjc2MjQwZDE0ZGZhNTU2ZjJiN2FjZGUzNzYzMWY4ZCJ9fX0=";
        ItemStack membersSkull = SkullCreator.itemFromBase64(members64);
        ItemMeta membersMeta = membersSkull.getItemMeta();
        membersMeta.setDisplayName(Utils.useColors("&bMembers"));
        membersSkull.setItemMeta(membersMeta);

        // Blocks Placed Item
        String block64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE5MjZiZmE5Y2FmOGJjYTkwNjkyNzgwOTc4YjVjNzRkNzEzZTg2NWY1YmRkMzc5MjA5N2IxODc5OTk3ZTU1NyJ9fX0=";
        ItemStack blockSkull = SkullCreator.itemFromBase64(block64);
        ItemMeta blockMeta = blockSkull.getItemMeta();
        blockMeta.setDisplayName(Utils.useColors("&5Blocks Placed"));
        blockSkull.setItemMeta(blockMeta);

        inventory.setItem(10, playerSkull);
        inventory.setItem(12, membersSkull);
        inventory.setItem(14, blockSkull);

        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, emptyFill);
            }
        }
    }

}
