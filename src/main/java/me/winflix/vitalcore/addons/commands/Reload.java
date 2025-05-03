package me.winflix.vitalcore.addons.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;

public class Reload extends SubCommand {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getVariants() {
        return "reload|rl";
    }

    @Override
    public String getDescription(Player p) {
        return "Recarga los modelos, texturas y resource pack del sistema de addons.";
    }

    @Override
    public String getPermission() {
        return "vitalcore.addons.reload";
    }

    @Override
    public String getSyntax(Player p) {
        return "/addons reload";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void perform(Player player, String[] args) {
        player.sendMessage("&7[&bAddons&7] &fRecargando modelos y resource pack...");
        try {
            // Recarga los modelos
            VitalCore.addons.getModelEngineManager().reload();
            for (Player other : Bukkit.getServer().getOnlinePlayers()) {
                VitalCore.addons.getResourcePackManager().sendResourcePackToPlayer(other);
            }

            player.sendMessage("§7[§a✔§7] §aAddons recargado correctamente.");
        } catch (Exception e) {
            player.sendMessage("§7[§c✖§7] §cError al recargar addons: " + e.getMessage());
            e.printStackTrace();
        }
    }
}