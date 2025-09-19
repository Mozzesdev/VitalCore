package me.winflix.vitalcore.warps.commands;

import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.warps.WarpManager;
import me.winflix.vitalcore.warps.menu.WarpMenu;

/**
 * SubComando para abrir la GUI de warps
 */
public class WarpGuiCommand extends SubCommand {

    private final WarpManager warpManager;

    public WarpGuiCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public String getVariants() {
        return "menu";
    }

    @Override
    public String getPermission() {
        return "vitalcore.warp.use";
    }

    @Override
    public String getDescription(Player p) {
        return "Abre la interfaz gráfica de warps";
    }

    @Override
    public String getSyntax(Player p) {
        return "/warp gui [categoria]";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        if (args.length == 1) {
            // Autocompletar con categorías disponibles
            return warpManager.getCategories().stream()
                    .filter(category -> category.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length > 0) {
            // Abrir GUI filtrado por categoría
            String category = args[0];
            WarpMenu menu = new WarpMenu(player, warpManager, category);
            menu.open();
        } else {
            // Abrir GUI principal
            new WarpMenu(player, warpManager).open();
        }
    }
}
