package me.winflix.vitalcore.warps.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.warps.WarpManager;
import me.winflix.vitalcore.warps.models.Warp;

/**
 * SubComando para listar warps
 */
public class WarpListCommand extends SubCommand {

    private final WarpManager warpManager;

    public WarpListCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getVariants() {
        return "lista";
    }

    @Override
    public String getPermission() {
        return "vitalcore.warp.use";
    }

    @Override
    public String getDescription(Player p) {
        return "Lista todos los warps disponibles";
    }

    @Override
    public String getSyntax(Player p) {
        return "/warp list [categoria]";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        if (args.length == 1) {
            // Autocompletar con categorías disponibles
            return warpManager.getCategories().stream()
                    .filter(category -> category.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        String category = args.length > 0 ? args[0] : null;
        
        List<Warp> warps;
        String title;

        if (category != null) {
            warps = warpManager.getWarpsByCategory(category).stream()
                    .filter(warp -> hasWarpAccess(player, warp))
                    .collect(Collectors.toList());
            title = "Warps en categoría '" + category + "'";
        } else {
            warps = warpManager.getPublicWarps().stream()
                    .filter(warp -> hasWarpAccess(player, warp))
                    .collect(Collectors.toList());
            title = "Todos los warps";
        }

        if (warps.isEmpty()) {
            Utils.infoMessage(player, "No hay warps disponibles" + 
                            (category != null ? " en la categoría '" + category + "'" : "") + ".");
            return;
        }

        Utils.infoMessage(player, "§6=== " + title + " ===");
        
        // Agrupar por categoría si no se especificó una
        if (category == null) {
            warps.stream()
                .collect(Collectors.groupingBy(Warp::getCategory))
                .forEach((cat, categoryWarps) -> {
                    player.sendMessage(Utils.useColors("§e" + cat.toUpperCase() + ":"));
                    categoryWarps.forEach(warp -> {
                        String favorite = warpManager.isFavorite(player, warp.getName()) ? " §e⭐" : "";
                        player.sendMessage(Utils.useColors("  §7- §a" + warp.getName() + favorite));
                    });
                });
        } else {
            warps.forEach(warp -> {
                String favorite = warpManager.isFavorite(player, warp.getName()) ? " §e⭐" : "";
                player.sendMessage(Utils.useColors("§7- §a" + warp.getName() + favorite));
            });
        }

        Utils.infoMessage(player, "§7Total: §f" + warps.size() + " warps");
        Utils.infoMessage(player, "§7Usa §f/warp <nombre> §7para teletransportarte.");
    }

    private boolean hasWarpAccess(Player player, Warp warp) {
        // Verificación básica de acceso (misma lógica que WarpCommand)
        if (player.hasPermission("vitalcore.warp.admin")) {
            return true;
        }

        if (!warp.isPublic() && !warp.getOwner().equals(player.getUniqueId())) {
            return false;
        }

        if (warp.getPermission() != null && !warp.getPermission().isEmpty()) {
            return player.hasPermission(warp.getPermission());
        }

        String categoryPerm = "vitalcore.warp.category." + warp.getCategory().toLowerCase();
        if (!player.hasPermission(categoryPerm)) {
            return false;
        }

        String warpPerm = "vitalcore.warp.warp." + warp.getName().toLowerCase();
        return player.hasPermission(warpPerm);
    }
}
