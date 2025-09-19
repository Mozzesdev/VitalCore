package me.winflix.vitalcore.general.menu;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.winflix.vitalcore.general.utils.SkullUtils;
import net.md_5.bungee.api.ChatColor;

public abstract class PaginatedMenu extends Menu {
    protected int page = 0;
    protected final int ITEMS_PER_ROW = 7; // Items por fila
    protected final int CONTENT_START_ROW = 1; // Fila donde inicia el contenido
    private final int MAX_ITEMS_PER_PAGE = 28; // 4 filas de 7 items
    private int NAVIGATION_ROW;

    public PaginatedMenu(Player owner) {
        super(owner);
    }

    @Override
    public int getSlots() {
        int totalItems = Math.min(getAllItems().size(), MAX_ITEMS_PER_PAGE);
        int totalContentRows = (int) Math.ceil((double) totalItems / ITEMS_PER_ROW);
        int totalRows = totalContentRows + 2;
        NAVIGATION_ROW = totalRows - 1;
        return totalRows * 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta())
            return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        Player player = (Player) event.getWhoClicked();

        switch (displayName) {
            case "Previous page" -> {
                if (page > 0) {
                    page--;
                    open();
                }
            }
            case "Next page" -> {
                if (hasNextPage()) {
                    page++;
                    open();
                }
            }
            case "Close menu" -> player.closeInventory();
        }

        event.setCancelled(true);
    }

    private boolean hasNextPage() {
        return (page + 1) * MAX_ITEMS_PER_PAGE < getAllItems().size();
    }

    @Override
    public void setMenuItems() {
        addBorders(); // Primero los bordes
        addContentItems(); // Luego el contenido
        addNavigationRow(); // Finalmente la navegación
    }

    private void addBorders() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                if (i < CONTENT_START_ROW * 9 || i % 9 == 0 || i % 9 == 8 || i >= NAVIGATION_ROW * 9) {
                    inventory.setItem(i, FILLER_GLASS);
                }
            }
        }
    }

    private void addContentItems() {
        List<ItemStack> items = getAllItems();
        int startIndex = page * ITEMS_PER_ROW * (NAVIGATION_ROW - CONTENT_START_ROW);
        int endIndex = Math.min(startIndex + ITEMS_PER_ROW * (NAVIGATION_ROW - CONTENT_START_ROW), items.size());

        for (int i = startIndex; i < endIndex; i++) {
            int row = CONTENT_START_ROW + (i - startIndex) / ITEMS_PER_ROW;
            int column = (i - startIndex) % ITEMS_PER_ROW;
            int slot = row * 9 + column + 1; // +1 para evitar bordes laterales

            if (slot < inventory.getSize()) {
                inventory.setItem(slot, items.get(i));
            }
        }
    }

    private void addNavigationRow() {
        int navRowStart = NAVIGATION_ROW * 9;
        ItemStack previous = createButton("&ePrevious page",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==");
        ItemStack close = createButton("&cClose menu",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkMWFiYTczZjYzOWY0YmM0MmJkNDgxOTZjNzE1MTk3YmUyNzEyYzNiOTYyYzk3ZWJmOWU5ZWQ4ZWZhMDI1In19fQ==");
        ItemStack next = createButton("&eNext page",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");

        inventory.setItem(navRowStart + 3, previous);
        inventory.setItem(navRowStart + 4, close);
        inventory.setItem(navRowStart + 5, next);
    }

    private ItemStack createButton(String name, String texture) {
        return SkullUtils.createSkull(texture, name, List.of());
    }

    protected abstract List<ItemStack> getAllItems();
}
