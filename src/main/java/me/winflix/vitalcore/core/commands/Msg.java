package me.winflix.vitalcore.core.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.core.Core;
import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;

public class Msg extends BaseCommand {

    @Override
    public String getName() {
        return "msg";
    }

    @Override
    public String getVariants() {
        return "tell|whisper|pm";
    }

    @Override
    public String getDescription() {
        return "Envía un mensaje privado a otro jugador";
    }

    @Override
    public String getPermission() {
        return "vitalcore.chat.private";
    }

    @Override
    public String getSyntax() {
        return "/msg <jugador> <mensaje>";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        // Sugerir jugadores online excepto uno mismo
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getName().equalsIgnoreCase(player.getName()))
                .map(Player::getName)
                .toList();
    }

    @Override
    public void perform(Player player, String[] args) {
        // Verificar argumentos mínimos
        if (args.length < 2) {
            Utils.errorMessage(player, "Uso correcto: " + getSyntax());
            return;
        }

        // Obtener jugador objetivo
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            Utils.errorMessage(player,
                    VitalCore.fileManager.getMessagesFile(player).getConfig()
                            .getString("chat.private.player-offline")
                            .replace("{player}", args[0]));
            return;
        }

        // Construir el mensaje
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Enviar mensaje privado
        Core.chatManager.sendPrivateMessage(player, target, message);
    }
}
