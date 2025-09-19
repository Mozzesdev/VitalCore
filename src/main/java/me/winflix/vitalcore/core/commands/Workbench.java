package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;

/**
 * Comando para abrir una mesa de crafteo virtual
 */
public class Workbench extends BaseCommand {
    
    @Override
    public String getName() {
        return "workbench";
    }

    @Override
    public String getVariants() {
        return "wb,craft,crafting";
    }

    @Override
    public String getDescription() {
        return "Abre una mesa de crafteo virtual";
    }

    @Override
    public String getPermission() {
        return "vitalcore.workbench";
    }

    @Override
    public String getSyntax() {
        return "/workbench";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        // Abrir mesa de crafteo virtual usando la nueva API
        player.openInventory(Bukkit.createInventory(null, InventoryType.WORKBENCH, "Mesa de Crafteo"));
        Utils.successMessage(player, "¡Mesa de crafteo abierta!");
    }
}
