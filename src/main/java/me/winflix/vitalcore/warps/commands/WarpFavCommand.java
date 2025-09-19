package me.winflix.vitalcore.warps.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.warps.WarpManager;

/**
 * SubComando para gestionar favoritos
 */
public class WarpFavCommand extends SubCommand {

    private final WarpManager warpManager;

    public WarpFavCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public String getName() {
        return "fav";
    }

    @Override
    public String getVariants() {
        return "favorite";
    }

    @Override
    public String getPermission() {
        return "vitalcore.warp.use";
    }

    @Override
    public String getDescription(Player p) {
        return "Añade o quita un warp de tus favoritos";
    }

    @Override
    public String getSyntax(Player p) {
        return "/warp fav <nombre>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        if (args.length == 1) {
            // Autocompletar con warps públicos disponibles
            return warpManager.getPublicWarps().stream()
                    .map(warp -> warp.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length == 0) {
            Utils.errorMessage(player, "Uso: " + getSyntax(player));
            return;
        }

        String warpName = args[0];

        if (!warpManager.warpExists(warpName)) {
            Utils.errorMessage(player, "El warp '" + warpName + "' no existe.");
            return;
        }

        boolean isFavorite = warpManager.isFavorite(player, warpName);

        if (isFavorite) {
            // Quitar de favoritos
            boolean success = warpManager.removeFavorite(player, warpName);
            if (success) {
                Utils.successMessage(player, "Warp '" + warpName + "' eliminado de favoritos.");
            } else {
                Utils.errorMessage(player, "Error al eliminar de favoritos.");
            }
        } else {
            // Añadir a favoritos
            boolean success = warpManager.addFavorite(player, warpName);
            if (success) {
                Utils.successMessage(player, "Warp '" + warpName + "' añadido a favoritos.");
            } else {
                Utils.errorMessage(player, "Error al añadir a favoritos.");
            }
        }
    }
}
