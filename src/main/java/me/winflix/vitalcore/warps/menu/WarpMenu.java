package me.winflix.vitalcore.warps.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.winflix.vitalcore.general.menu.PaginatedMenu;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.warps.WarpManager;
import me.winflix.vitalcore.warps.models.Warp;

/**
 * Menú GUI para mostrar y seleccionar warps
 */
public class WarpMenu extends PaginatedMenu {

    private final WarpManager warpManager;
    private String currentCategory = null;
    private boolean showFavoritesOnly = false;

    public WarpMenu(Player owner, WarpManager warpManager) {
        super(owner);
        this.warpManager = warpManager;
    }

    public WarpMenu(Player owner, WarpManager warpManager, String category) {
        super(owner);
        this.warpManager = warpManager;
        this.currentCategory = category;
    }

    @Override
    public String getMenuName() {
        if (showFavoritesOnly) {
            return "§6⭐ Mis Favoritos";
        } else if (currentCategory != null) {
            return "§6Warps - " + currentCategory;
        } else {
            return "§6Lista de Warps";
        }
    }

    @Override
    protected List<ItemStack> getAllItems() {
        List<Warp> warps = getFilteredWarps();
        List<ItemStack> items = new ArrayList<>();
        
        for (Warp warp : warps) {
            items.add(createWarpItem(warp));
        }
        
        return items;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        super.handleMenu(e); // Manejar navegación básica
        
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Verificar si es un item de warp
        if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
            String displayName = clickedItem.getItemMeta().getDisplayName();
            
            // Extraer nombre del warp del display name
            String warpName = extractWarpName(displayName);
            if (warpName != null) {
                Warp warp = warpManager.getWarp(warpName);
                if (warp != null) {
                    handleWarpClick(player, warp, e);
                }
            }
        }
    }

    private ItemStack createWarpItem(Warp warp) {
        Material iconMaterial = warp.getIcon() != null ? warp.getIcon() : Material.ENDER_PEARL;
        ItemStack item = new ItemStack(iconMaterial);
        ItemMeta meta = item.getItemMeta();

        // Nombre del warp
        String displayName = "§a" + warp.getName();
        if (warpManager.isFavorite(owner, warp.getName())) {
            displayName += " §e⭐";
        }
        meta.setDisplayName(displayName);

        // Lore con información
        List<String> lore = new ArrayList<>();
        lore.add("§7Categoría: §f" + warp.getCategory());
        lore.add("§7Ubicación: §f" + warp.getWorldName() + " (" + 
                 (int)warp.getX() + ", " + (int)warp.getY() + ", " + (int)warp.getZ() + ")");
        lore.add("§7Tipo: " + (warp.isPublic() ? "§aPublico" : "§cPrivado"));
        
        if (warp.getCooldown() > 0) {
            lore.add("§7Cooldown: §f" + warp.getCooldown() + "s");
        }
        
        lore.add("§7Usos: §f" + warp.getUsageCount());
        lore.add("");
        lore.add("§e▶ Clic para teletransportarte");
        lore.add("§6▶ Clic derecho para información");
        
        if (warp.getOwner().equals(owner.getUniqueId()) || 
            owner.hasPermission("vitalcore.warp.admin")) {
            lore.add("§c▶ Shift + Clic para gestionar");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private List<Warp> getFilteredWarps() {
        List<Warp> warps = warpManager.getAllWarps();

        return warps.stream()
                .filter(warp -> hasWarpAccess(owner, warp))
                .filter(warp -> currentCategory == null || warp.getCategory().equalsIgnoreCase(currentCategory))
                .filter(warp -> !showFavoritesOnly || warpManager.isFavorite(owner, warp.getName()))
                .sorted(Comparator.comparing(Warp::getName))
                .collect(Collectors.toList());
    }

    private boolean hasWarpAccess(Player player, Warp warp) {
        // Admin bypass
        if (player.hasPermission("vitalcore.warp.admin")) {
            return true;
        }

        // Warp privado - solo el dueño
        if (!warp.isPublic() && !warp.getOwner().equals(player.getUniqueId())) {
            return false;
        }

        // Permiso básico
        if (!player.hasPermission("vitalcore.warp.use")) {
            return false;
        }

        // Permiso específico del warp
        if (warp.getPermission() != null && !warp.getPermission().isEmpty()) {
            return player.hasPermission(warp.getPermission());
        }

        // Permiso por categoría
        String categoryPerm = "vitalcore.warp.category." + warp.getCategory().toLowerCase();
        if (!player.hasPermission(categoryPerm)) {
            return false;
        }

        // Permiso específico del warp por nombre
        String warpPerm = "vitalcore.warp.warp." + warp.getName().toLowerCase();
        return player.hasPermission(warpPerm);
    }

    private void handleWarpClick(Player player, Warp warp, InventoryClickEvent e) {
        if (e.isShiftClick()) {
            // Gestión del warp (solo para dueño o admin)
            if (warp.getOwner().equals(player.getUniqueId()) || player.hasPermission("vitalcore.warp.admin")) {
                Utils.infoMessage(player, "§7Gestión de warps disponible via comandos: /warp set, /warp del");
            }
        } else if (e.isRightClick()) {
            // Mostrar información detallada
            player.closeInventory();
            player.performCommand("warp info " + warp.getName());
        } else {
            // Teletransporte
            player.closeInventory();
            warpManager.teleportToWarp(player, warp);
        }
    }

    private String extractWarpName(String displayName) {
        // Remover códigos de color y símbolos
        String clean = displayName.replaceAll("§[0-9a-fA-F]", "")
                                .replace("⭐", "")
                                .trim();
        
        if (clean.isEmpty()) {
            return null;
        }
        
        // Verificar que el warp existe
        Warp warp = warpManager.getWarp(clean);
        return warp != null ? clean : null;
    }

    // Métodos para filtros
    public void setCategory(String category) {
        this.currentCategory = category;
        this.page = 0;
    }

    public void toggleFavorites() {
        this.showFavoritesOnly = !showFavoritesOnly;
        this.page = 0;
    }

    public void clearFilters() {
        this.currentCategory = null;
        this.showFavoritesOnly = false;
        this.page = 0;
    }
}
