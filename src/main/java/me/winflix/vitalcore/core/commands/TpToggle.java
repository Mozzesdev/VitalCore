package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.core.managers.TeleportManager;
import me.winflix.vitalcore.general.commands.BaseCommand;
import org.bukkit.entity.Player;

import java.util.List;

public class TpToggle extends BaseCommand {

    @Override
    public String getName() {
        return "tptoggle";
    }

    @Override
    public String getVariants() {
        return "tptog";
    }

    @Override
    public String getDescription() {
        return "Activa o desactiva la recepción de solicitudes de teletransporte";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/tptoggle";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return List.of(); // Sin autocompletado para este comando
    }

    @Override
    public void perform(Player player, String[] args) {
       TeleportManager.toggleTpa(player);
    }
}
