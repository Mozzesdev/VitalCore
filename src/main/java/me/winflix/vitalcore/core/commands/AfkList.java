package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.core.managers.AfkManager;
import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando para ver el estado AFK e inactividad de los jugadores
 */
public class AfkList extends BaseCommand {
    
    @Override
    public void perform(Player player, String[] args) {
        player.sendMessage(Utils.useColors("§6=== Estado AFK de Jugadores ==="));
        
        boolean foundAfkPlayers = false;
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (AfkManager.isAfk(onlinePlayer)) {
                foundAfkPlayers = true;
                String reason = AfkManager.getAfkReason(onlinePlayer);
                String afkTime = AfkManager.getFormattedAfkTime(onlinePlayer);
                
                if (reason != null && !reason.isEmpty()) {
                    player.sendMessage(Utils.useColors("§e" + onlinePlayer.getName() + 
                                     "§7 - AFK: §f" + reason + 
                                     "§7 (" + afkTime + ")"));
                } else {
                    player.sendMessage(Utils.useColors("§e" + onlinePlayer.getName() + 
                                     "§7 - AFK (" + afkTime + ")"));
                }
            }
        }
        
        if (!foundAfkPlayers) {
            Utils.successMessage(player, "No hay jugadores AFK actualmente.");
        }
        
        // Mostrar jugadores con alta inactividad (más de 3 minutos pero menos de 5)
        player.sendMessage(Utils.useColors("§6\n=== Jugadores Inactivos ==="));
        boolean foundInactivePlayers = false;
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!AfkManager.isAfk(onlinePlayer)) {
                long inactivityTime = AfkManager.getInactivityTime(onlinePlayer);
                // Mostrar si llevan más de 3 minutos inactivos
                if (inactivityTime > AfkManager.INACTIVITY_WARNING_TIME) {
                    foundInactivePlayers = true;
                    String inactiveTime = AfkManager.getFormattedInactivityTime(onlinePlayer);
                    player.sendMessage(Utils.useColors("§6" + onlinePlayer.getName() + 
                                     "§7 - Inactivo por " + inactiveTime));
                }
            }
        }
        
        if (!foundInactivePlayers) {
            Utils.successMessage(player, "No hay jugadores con alta inactividad.");
        }
    }
    
    @Override
    public String getName() {
        return "afklist";
    }
    
    @Override
    public String getVariants() {
        return "afklist,listafk";
    }
    
    @Override
    public String getDescription() {
        return "Muestra el estado AFK e inactividad de todos los jugadores";
    }
    
    @Override
    public String getPermission() {
        return "vitalcore.afklist";
    }
    
    @Override
    public String getSyntax() {
        return "/afklist";
    }
    
    @Override
    public List<String> getArguments(Player player, String[] args) {
        return new ArrayList<>();
    }
}
