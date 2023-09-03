package me.winflix.vitalcore.general.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.dbassett.skullcreator.SkullCreator;
import me.winflix.vitalcore.general.utils.Utils;

public abstract class PaginatedMenu extends Menu {

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    protected int page = 0;

    protected int maxItemsPerPage = 29;

    protected int index = 0;

    public void addMenuBorder() {
        int totalSlots = getSlots();

        int[] whiteSlots = new int[] { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33,
                34, 37, 38, 39, 40, 41, 42, 43 };
        int[] controlsSlots = new int[] { 21, 22, 23, 30, 31, 32, 39, 40, 41, 48, 49, 50 };

        List<Integer> whitelist = new ArrayList<>();
        List<Integer> controlList = new ArrayList<>();

        for (int i : controlsSlots) {
            if (i < totalSlots && i > (totalSlots - 7)) {
                controlList.add(i);
            }
        }

        for (int i : whiteSlots) {
            if (i < (totalSlots - 8)) {
                whitelist.add(i);
            }
        }

        ItemStack previousPage = createSkullItem("&ePrevious page",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzYyNTkwMmIzODllZDZjMTQ3NTc0ZTQyMmRhOGY4ZjM2MWM4ZWI1N2U3NjMxNjc2YTcyNzc3ZTdiMWQifX19");
        ItemStack nextPage = createSkullItem("&eNext page",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDRiZThhZWVjMTE4NDk2OTdhZGM2ZmQxZjE4OWIxNjY0MmRmZjE5ZjI5NTVjMDVkZWFiYTY4YzlkZmYxYmUifX19");
        ItemStack close = createSkullItem("&cClose menu",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkMWFiYTczZjYzOWY0YmM0MmJkNDgxOTZjNzE1MTk3YmUyNzEyYzNiOTYyYzk3ZWJmOWU5ZWQ4ZWZhMDI1In19fQ==");

        ItemMeta fillerMeta = super.FILLER_GLASS.getItemMeta();
        fillerMeta.setDisplayName(Utils.useColors("&5mc.wiped.com"));
        super.FILLER_GLASS.setItemMeta(fillerMeta);

        int stated = 0;

        for (int controlSlot : controlList) {
            if (stated > 2) {
                break;
            }
            if (inventory.getItem(controlSlot) == null && controlSlot <= totalSlots) {
                inventory.setItem(controlSlot, stated == 0 ? previousPage : (stated == 1 ? close : nextPage));
                stated++;
            }
        }

        for (int i = 0; i < totalSlots; i++) {
            if (!whitelist.contains(i) && inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER_GLASS);
            }
        }
    }

    private ItemStack createSkullItem(String displayName, String base64Texture) {
        ItemStack skullItem = SkullCreator.itemFromBase64(base64Texture);
        ItemMeta skullMeta = skullItem.getItemMeta();
        skullMeta.setDisplayName(Utils.useColors(displayName));
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

}
