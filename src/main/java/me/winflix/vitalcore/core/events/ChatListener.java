package me.winflix.vitalcore.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.winflix.vitalcore.core.Core;

public class ChatListener implements Listener {

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        Player sender = event.getPlayer();
        String message = event.getMessage();

        event.setCancelled(true);
        Core.chatManager.sendPublicMessage(sender, message);
    }
}
