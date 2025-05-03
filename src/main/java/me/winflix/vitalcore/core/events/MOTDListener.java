package me.winflix.vitalcore.core.events;

import java.io.File;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import me.winflix.vitalcore.core.managers.MOTDManager;

public class MOTDListener implements Listener {

    private final MOTDManager motdManager;

    public MOTDListener(MOTDManager motdManager) {
        this.motdManager = motdManager;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String motd = motdManager.getProcessedMotd();
        event.setMotd(motd);

        event.setMaxPlayers(motdManager.getDisplayedMaxPlayers(event.getMaxPlayers()));

        File iconFile = motdManager.getIconForMotd(motd);
        if (iconFile != null) {
            try {
                BufferedImage image = ImageIO.read(iconFile);
                if (image != null) {
                    event.setServerIcon(Bukkit.loadServerIcon(image));
                }
            } catch (Exception e) {
                Bukkit.getLogger().info(e.getLocalizedMessage());
                Bukkit.getLogger().warning("No se pudo cargar el ícono del servidor: " + iconFile.getName());
            }
        }
    }
}
