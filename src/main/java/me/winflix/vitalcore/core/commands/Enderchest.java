package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Comando para abrir el ender chest desde cualquier lugar
 */
public class Enderchest extends BaseCommand {
    
    @Override
    public String getName() {
        return "enderchest";
    }

    @Override
    public String getVariants() {
        return "ec,echest";
    }

    @Override
    public String getDescription() {
        return "Abre tu ender chest desde cualquier lugar";
    }

    @Override
    public String getPermission() {
        return "vitalcore.enderchest";
    }

    @Override
    public String getSyntax() {
        return "/enderchest";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        // Abrir el ender chest del jugador
        player.openInventory(player.getEnderChest());
        Utils.successMessage(player, "¡Ender chest abierto!");
    }
}
