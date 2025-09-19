package me.winflix.vitalcore.warps.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.warps.WarpManager;
import me.winflix.vitalcore.warps.menu.WarpMenu;
import me.winflix.vitalcore.warps.models.Warp;

/**
 * Comando base de warps - se ejecuta cuando no hay subcomandos
 */
public class WarpCommand extends BaseCommand {

    private final WarpManager warpManager;

    public WarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public String getName() {
        return "warp";
    }

    @Override
    public String getVariants() {
        return "tp";
    }

    @Override
    public String getDescription() {
        return "Sistema de teletransporte con warps";
    }

    @Override
    public String getPermission() {
        return "vitalcore.warp.use";
    }

    @Override
    public String getSyntax() {
        return "/warp [nombre]";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        if (args.length == 1) {
            // Autocompletar nombres de warps disponibles
            return warpManager.getAllWarps().stream()
                    .filter(warp -> hasWarpAccess(player, warp))
                    .map(Warp::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length == 0) {
            // Abrir GUI principal
            new WarpMenu(player, warpManager).open();
            return;
        }

        // Si se especifica un nombre, intentar teletransportar
        String warpName = args[0];
        Warp warp = warpManager.getWarp(warpName);

        if (warp == null) {
            Utils.errorMessage(player, "El warp '" + warpName + "' no existe.");
            showAvailableWarps(player);
            return;
        }

        // Verificar acceso básico
        if (!hasWarpAccess(player, warp)) {
            Utils.errorMessage(player, "No tienes acceso a este warp.");
            return;
        }

        // Intentar teletransporte
        warpManager.teleportToWarp(player, warp);
    }

    private boolean hasWarpAccess(Player player, Warp warp) {
        // Verificación básica de acceso
        if (player.hasPermission("vitalcore.warp.admin")) {
            return true;
        }

        // Warp privado - solo el dueño o admin
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

    private void showAvailableWarps(Player player) {
        List<Warp> availableWarps = warpManager.getAllWarps().stream()
                .filter(warp -> hasWarpAccess(player, warp))
                .limit(10) // Mostrar solo los primeros 10
                .collect(Collectors.toList());

        if (availableWarps.isEmpty()) {
            Utils.infoMessage(player, "No hay warps disponibles para ti.");
            return;
        }

        Utils.infoMessage(player, "Warps disponibles:");
        for (Warp warp : availableWarps) {
            String category = warp.getCategory().equals("general") ? "" : " §7[" + warp.getCategory() + "]";
            player.sendMessage(Utils.useColors("§7- §a" + warp.getName() + category));
        }

        if (warpManager.getAllWarps().size() > 10) {
            Utils.infoMessage(player, "§7...y " + (warpManager.getAllWarps().size() - 10) + " más. Usa §f/warp gui §7para ver todos.");
        }
        
        Utils.infoMessage(player, "§7Comandos: §f/warp gui§7, §f/warp list§7, §f/warp info <nombre>");
    }
}
