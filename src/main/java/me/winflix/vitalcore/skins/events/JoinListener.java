package me.winflix.vitalcore.skins.events;

import me.winflix.vitalcore.skins.utils.SkinGrabber;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        SkinGrabber.changeSkinByPlayer(player);

    }
}
