package me.winflix.vitalcore.core.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.core.managers.TeleportManager;
import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;

public class Tpa extends BaseCommand {

    @Override
    public String getName() {
        return "tpa";
    }

    @Override
    public String getVariants() {
        return "teleport";
    }

    @Override
    public String getDescription() {
        return "Solicita teletransportarse hacia otro jugador";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/tpa <jugador>";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        // Lista de jugadores online excepto el propio
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getName().equalsIgnoreCase(player.getName()))
                .map(Player::getName)
                .toList();
    }

    @Override
    public void perform(Player player, String[] args) {
        // Verificar que se especifique un jugador destino
        if (args.length == 0) {
            Utils.errorMessage(player, "Uso correcto: " + getSyntax());
            return;
        }

        // Obtener al jugador destino
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            Utils.errorMessage(player, "Jugador no encontrado o desconectado.");
            return;
        }

        TeleportManager.sendTeleportRequest(player, target);
    }
}