package me.winflix.vitalcore.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.core.managers.ChatManager;
import me.winflix.vitalcore.general.utils.Utils;

public class AntiAdListener implements Listener {

    private final ChatManager chatManager;

    public AntiAdListener(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        String[] parts = message.split(" ", 2);
        if (parts.length < 2) return;

        String content = parts[1];

        if (chatManager.containsAdvertisement(content, sender)) {
            event.setCancelled(true);
            String msg = VitalCore.fileManager.getMessagesFile(sender).getConfig()
                .getString("chat.advertising.command-blocked");
            Utils.errorMessage(sender, msg);
            chatManager.notifyStaff(sender);
            chatManager.executeAdvertisementCommand(sender);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        for (String line : lines) {
            if (chatManager.containsAdvertisement(line, player)) {
                event.setCancelled(true);
                String msg = VitalCore.fileManager.getMessagesFile(player).getConfig()
                    .getString("chat.advertising.sign-blocked");
                Utils.errorMessage(player, msg);
                chatManager.notifyStaff(player);
                chatManager.executeAdvertisementCommand(player);
                return;
            }
        }
    }
}
