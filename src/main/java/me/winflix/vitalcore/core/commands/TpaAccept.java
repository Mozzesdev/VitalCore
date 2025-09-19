package me.winflix.vitalcore.core.commands;

import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.core.managers.TeleportManager;
import me.winflix.vitalcore.general.commands.BaseCommand;

public class TpaAccept extends BaseCommand {

    @Override
    public String getName() {
        return "tpaccept";
    }

    @Override
    public String getVariants() {
        return "tpayes|tpaccept|tpy";
    }

    @Override
    public String getDescription() {
        return "Acepta una solicitud de teletransporte de otro jugador";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/tpaccept [jugador]";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        // Solo mostrar jugadores que tienen solicitudes pendientes hacia este jugador
        return TeleportManager.getPendingRequestSenders(player);
    }

    @Override
    public void perform(Player player, String[] args) {
        // Si no hay argumentos, mostrar solicitudes pendientes
        if (args.length == 0) {
            TeleportManager.showPendingRequests(player);
            return;
        }
        
        TeleportManager.acceptRequest(player, args[0]);
    }
}