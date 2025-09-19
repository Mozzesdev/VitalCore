package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

public class Fly extends BaseCommand {
    
    @Override
    public String getName() {
        return "fly";
    }

    @Override
    public String getVariants() {
        return "volar";
    }

    @Override
    public String getDescription() {
        return "Activa o desactiva el vuelo";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/fly";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        boolean currentFlyMode = player.getAllowFlight();
        boolean newFlyMode = !currentFlyMode;
        
        player.setAllowFlight(newFlyMode);
        
        if (newFlyMode) {
            Utils.successMessage(player, "Vuelo activado. Pulsa doble espacio para volar.");
        } else {
            player.setFlying(false); // Desactivar vuelo activo si estaba volando
            Utils.infoMessage(player, "Vuelo desactivado. Ya no puedes volar.");
        }
    }
}
