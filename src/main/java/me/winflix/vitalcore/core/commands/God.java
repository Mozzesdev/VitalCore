package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.core.managers.GodManager;
import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

public class God extends BaseCommand {
    
    @Override
    public String getName() {
        return "god";
    }

    @Override
    public String getVariants() {
        return "invulnerable";
    }

    @Override
    public String getDescription() {
        return "Activa o desactiva el modo invulnerable";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/god";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        boolean newGodMode = GodManager.toggleGodMode(player);
        
        if (newGodMode) {
            Utils.successMessage(player, "Modo god activado. Eres invulnerable.");
        } else {
            Utils.infoMessage(player, "Modo god desactivado. Ya no eres invulnerable.");
        }
    }
}
