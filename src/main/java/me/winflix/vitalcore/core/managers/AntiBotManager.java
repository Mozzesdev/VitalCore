package me.winflix.vitalcore.core.managers;

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

import me.winflix.vitalcore.general.utils.Utils;

public class AntiBotManager implements Listener {

    private final Set<Player> blockedPlayers = new HashSet<>();
    private final int minPlayersToActivate = 10;
    private final boolean isEnabled = true;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!isEnabled)
            return;

        if (Bukkit.getOnlinePlayers().size() >= minPlayersToActivate) {
            blockedPlayers.add(event.getPlayer());
            Utils.infoMessage(event.getPlayer(), "&ePor seguridad, debes moverte antes de usar el chat o comandos.");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!blockedPlayers.contains(player))
            return;

        if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
            blockedPlayers.remove(player);
            Utils.successMessage(player, "&aMovimiento detectado. Ya puedes chatear y usar comandos.");
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (blockedPlayers.contains(player)) {
            event.setCancelled(true);
            Utils.errorMessage(player, "&cDebes moverte antes de usar comandos.");
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (blockedPlayers.contains(player)) {
            event.setCancelled(true);
            Utils.errorMessage(player, "&cDebes moverte antes de hablar en el chat.");
        }
    }

    public boolean isBlocked(Player player) {
        return blockedPlayers.contains(player);
    }
}
