package me.winflix.vitalcore.warps.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.warps.WarpManager;
import me.winflix.vitalcore.warps.models.Warp;

/**
 * SubComando para mostrar información detallada de un warp
 */
public class WarpInfoCommand extends SubCommand {

    private final WarpManager warpManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public WarpInfoCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getVariants() {
        return "informacion";
    }

    @Override
    public String getPermission() {
        return "vitalcore.warp.use";
    }

    @Override
    public String getDescription(Player p) {
        return "Muestra información detallada de un warp";
    }

    @Override
    public String getSyntax(Player p) {
        return "/warp info <nombre>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        if (args.length == 1) {
            // Autocompletar con warps disponibles
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
            Utils.errorMessage(player, "Debes especificar el nombre del warp.");
            Utils.infoMessage(player, getSyntax(player));
            return;
        }

        String warpName = args[0];
        Warp warp = warpManager.getWarp(warpName);

        if (warp == null) {
            Utils.errorMessage(player, "El warp '" + warpName + "' no existe.");
            return;
        }

        // Verificar si tiene acceso al warp
        if (!hasWarpAccess(player, warp)) {
            Utils.errorMessage(player, "No tienes acceso a este warp.");
            return;
        }

        showWarpInfo(player, warp);
    }

    private void showWarpInfo(Player player, Warp warp) {
        Utils.infoMessage(player, "§6=== Información del Warp ===");
        
        player.sendMessage(Utils.useColors("§7Nombre: §f" + warp.getName()));
        player.sendMessage(Utils.useColors("§7Categoría: §f" + warp.getCategory()));
        
        // Ubicación
        String location = String.format("§7Ubicación: §f%s §7(%d, %d, %d)",
                warp.getWorldName(), 
                (int) warp.getX(), 
                (int) warp.getY(), 
                (int) warp.getZ());
        player.sendMessage(Utils.useColors(location));
        
        // Tipo (público/privado)
        String type = warp.isPublic() ? "§aPublico" : "§cPrivado";
        player.sendMessage(Utils.useColors("§7Tipo: " + type));
        
        // Propietario
        String ownerName = getOwnerName(warp.getOwner());
        player.sendMessage(Utils.useColors("§7Propietario: §f" + ownerName));
        
        // Ícono
        if (warp.getIcon() != null) {
            player.sendMessage(Utils.useColors("§7Ícono: §f" + warp.getIcon().name()));
        }
        
        // Permiso requerido
        if (warp.getPermission() != null && !warp.getPermission().isEmpty()) {
            player.sendMessage(Utils.useColors("§7Permiso requerido: §f" + warp.getPermission()));
        }
        
        // Cooldown
        if (warp.getCooldown() > 0) {
            player.sendMessage(Utils.useColors("§7Cooldown: §f" + warp.getCooldown() + " segundos"));
        }
        
        // Fecha de creación
        player.sendMessage(Utils.useColors("§7Creado: §f" + formatDate(warp.getCreatedAt())));
        
        // Estadísticas de uso
        player.sendMessage(Utils.useColors("§7Veces usado: §f" + warp.getUsageCount()));
        if (warp.getLastUsed() > 0) {
            player.sendMessage(Utils.useColors("§7Último uso: §f" + formatDate(warp.getLastUsed())));
        }
        
        // Si es favorito
        if (warpManager.isFavorite(player, warp.getName())) {
            player.sendMessage(Utils.useColors("§e⭐ Este warp está en tus favoritos"));
        }
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

    private String getOwnerName(java.util.UUID owner) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
        return offlinePlayer.getName() != null ? offlinePlayer.getName() : owner.toString();
    }

    private String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }
}
