package me.winflix.vitalcore.warps.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.warps.WarpManager;
import me.winflix.vitalcore.warps.models.Warp;

/**
 * SubComando para crear warps
 */
public class WarpSetCommand extends SubCommand {

    private final WarpManager warpManager;

    public WarpSetCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getVariants() {
        return "create,add";
    }

    @Override
    public String getPermission() {
        return "vitalcore.warp.create";
    }

    @Override
    public String getDescription(Player p) {
        return "Crea un nuevo warp en tu ubicación actual";
    }

    @Override
    public String getSyntax(Player p) {
        return "/warp set <nombre> [--category=categoria] [--private] [--icon=MATERIAL]";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        if (args.length == 1) {
            // Sugerencias para nombres de warp
            return List.of("<nombre>");
        } else if (args.length > 1) {
            // Sugerencias para flags
            return List.of("--category=general", "--private", "--public", "--icon=COMPASS");
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
        
        // Validar nombre
        if (!isValidWarpName(warpName)) {
            Utils.errorMessage(player, "Nombre de warp inválido. Usa solo letras, números y guiones.");
            return;
        }

        // Verificar si ya existe
        if (warpManager.warpExists(warpName)) {
            Utils.errorMessage(player, "Ya existe un warp con el nombre '" + warpName + "'.");
            return;
        }

        // Parsear flags
        WarpSettings settings = parseFlags(args);
        
        // Crear warp
        Location location = player.getLocation();
        boolean success = warpManager.createWarp(
            warpName, 
            location, 
            player, 
            settings.category, 
            settings.isPublic
        );

        if (success) {
            // Aplicar configuraciones adicionales si es necesario
            Warp warp = warpManager.getWarp(warpName);
            if (warp != null && settings.icon != null) {
                warp.setIcon(settings.icon);
                warpManager.updateWarp(warp);
            }

            Utils.successMessage(player, "¡Warp '" + warpName + "' creado exitosamente!");
            Utils.infoMessage(player, "§7Ubicación: §f" + location.getWorld().getName() + 
                             " (" + String.format("%.1f", location.getX()) + 
                             ", " + String.format("%.1f", location.getY()) + 
                             ", " + String.format("%.1f", location.getZ()) + ")");
            Utils.infoMessage(player, "§7Categoría: §f" + settings.category);
            Utils.infoMessage(player, "§7Visibilidad: §f" + (settings.isPublic ? "Público" : "Privado"));
        } else {
            Utils.errorMessage(player, "Error al crear el warp. Inténtalo de nuevo.");
        }
    }

    private boolean isValidWarpName(String name) {
        return name.matches("^[a-zA-Z0-9_-]+$") && name.length() >= 2 && name.length() <= 32;
    }

    private WarpSettings parseFlags(String[] args) {
        WarpSettings settings = new WarpSettings();
        
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.equals("--private")) {
                settings.isPublic = false;
            } else if (arg.equals("--public")) {
                settings.isPublic = true;
            } else if (arg.startsWith("--category=")) {
                settings.category = arg.substring("--category=".length());
            } else if (arg.startsWith("--icon=")) {
                String iconName = arg.substring("--icon=".length());
                try {
                    settings.icon = Material.valueOf(iconName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Icono inválido, usar por defecto
                    settings.icon = Material.COMPASS;
                }
            }
        }
        
        return settings;
    }

    private static class WarpSettings {
        String category = "general";
        boolean isPublic = true;
        Material icon = Material.COMPASS;
    }
}
