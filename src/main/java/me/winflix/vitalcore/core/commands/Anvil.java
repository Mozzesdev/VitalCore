package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;

/**
 * Comando para abrir un yunque virtual
 */
public class Anvil extends BaseCommand {
    
    @Override
    public String getName() {
        return "anvil";
    }

    @Override
    public String getVariants() {
        return "yunque";
    }

    @Override
    public String getDescription() {
        return "Abre un yunque virtual";
    }

    @Override
    public String getPermission() {
        return "vitalcore.anvil";
    }

    @Override
    public String getSyntax() {
        return "/anvil";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        // Abrir yunque virtual
        player.openInventory(Bukkit.createInventory(null, InventoryType.ANVIL, "Yunque"));
        Utils.successMessage(player, "¡Yunque abierto!");
    }
}
