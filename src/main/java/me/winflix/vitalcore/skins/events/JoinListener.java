package me.winflix.vitalcore.skins.events;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.skins.models.Skin;
import me.winflix.vitalcore.skins.utils.SkinGrabber;
import me.winflix.vitalcore.skins.utils.SkinManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    SkinManager skinManager;

    public JoinListener(SkinManager skinManager) {
        this.skinManager = skinManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Skin skin = skinManager.getSkin(player);

        if (skin == null) {
            skin = SkinGrabber.changeSkin(player, player.getDisplayName());
            if (skin == null) {
                return;
            }
            skinManager.updateSkin(player, skin);
        } else {
            SkinGrabber.applySkin(player, skin);
        }

    }

}
