package me.winflix.vitalcore.core.events;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.utils.Utils;

public class AntibotListener implements Listener {

    private final Set<Player> blockedPlayers = new HashSet<>();
    private final int minPlayersToActivate = 1;
    private final boolean isEnabled = true;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!isEnabled) return;

        if (Bukkit.getOnlinePlayers().size() >= minPlayersToActivate) {
            Player player = event.getPlayer();
            blockedPlayers.add(player);

            String message = VitalCore.fileManager.getMessagesFile(player).getConfig()
                    .getString("antibot.join.blocked");
            Utils.infoMessage(player, message);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isBlocked(player)) return;

        if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
            blockedPlayers.remove(player);
            String message = VitalCore.fileManager.getMessagesFile(player).getConfig()
                    .getString("antibot.move.success");
            Utils.successMessage(player, message);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (isBlocked(player)) {
            event.setCancelled(true);
            String message = VitalCore.fileManager.getMessagesFile(player).getConfig()
                    .getString("antibot.command.fail");
            Utils.errorMessage(player, message);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (isBlocked(player)) {
            event.setCancelled(true);
            String message = VitalCore.fileManager.getMessagesFile(player).getConfig()
                    .getString("antibot.chat.fail");
            Utils.errorMessage(player, message);
        }
    }

    public boolean isBlocked(Player player) {
        return blockedPlayers.contains(player);
    }
}