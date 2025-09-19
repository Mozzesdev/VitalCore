package me.winflix.vitalcore.warps.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.warps.WarpManager;
import me.winflix.vitalcore.warps.models.Warp;

/**
 * SubComando para eliminar warps
 */
public class WarpDelCommand extends SubCommand {

    private final WarpManager warpManager;

    public WarpDelCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public String getName() {
        return "del";
    }

    @Override
    public String getVariants() {
        return "delete,remove";
    }

    @Override
    public String getPermission() {
        return "vitalcore.warp.delete";
    }

    @Override
    public String getDescription(Player p) {
        return "Elimina un warp existente";
    }

    @Override
    public String getSyntax(Player p) {
        return "/warp del <nombre>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        if (args.length == 1) {
            // Autocompletar con warps que el jugador puede eliminar
            return warpManager.getAllWarps().stream()
                    .filter(warp -> canDeleteWarp(player, warp))
                    .map(Warp::getName)
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
        Warp warp = warpManager.getWarp(warpName);

        if (warp == null) {
            Utils.errorMessage(player, "El warp '" + warpName + "' no existe.");
            return;
        }

        // Verificar permisos
        if (!canDeleteWarp(player, warp)) {
            Utils.errorMessage(player, "No tienes permiso para eliminar este warp.");
            return;
        }

        // Eliminar warp
        boolean success = warpManager.deleteWarp(warpName);

        if (success) {
            Utils.successMessage(player, "¡Warp '" + warpName + "' eliminado exitosamente!");
        } else {
            Utils.errorMessage(player, "Error al eliminar el warp. Inténtalo de nuevo.");
        }
    }

    private boolean canDeleteWarp(Player player, Warp warp) {
        // Admin puede eliminar cualquier warp
        if (player.hasPermission("vitalcore.warp.admin")) {
            return true;
        }

        // Solo el dueño puede eliminar su propio warp
        return warp.getOwner().equals(player.getUniqueId());
    }
}
