package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.core.managers.AfkManager;
import me.winflix.vitalcore.general.commands.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando para activar/desactivar el modo AFK
 */
public class Afk extends BaseCommand {
    
    @Override
    public void perform(Player player, String[] args) {
        
        // Obtener el motivo si se proporciona
        String reason = null;
        if (args.length > 0) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (String arg : args) {
                reasonBuilder.append(arg).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }
        
        // Alternar estado AFK
        boolean isNowAfk = AfkManager.toggleAfk(player, reason);
        
        if (isNowAfk) {
            // Jugador ahora está AFK
            if (reason != null && !reason.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Ahora estás AFK: " + ChatColor.GRAY + reason);
                
                // Anunciar a otros jugadores
                String announcement = ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " está AFK: " + ChatColor.WHITE + reason;
                AfkManager.announceToOthers(player, announcement);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Ahora estás AFK.");
                
                // Anunciar a otros jugadores
                String announcement = ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " está AFK.";
                AfkManager.announceToOthers(player, announcement);
            }
            
            // Cambiar el display name para mostrar [AFK]
            AfkManager.setAfkDisplayName(player);
            
        } else {
            // Jugador ya no está AFK
            String afkTime = AfkManager.getFormattedAfkTime(player);
            player.sendMessage(ChatColor.GREEN + "Ya no estás AFK. " + ChatColor.GRAY + "(Estuviste AFK por " + afkTime + ")");
            
            // Anunciar a otros jugadores
            String announcement = ChatColor.GREEN + player.getName() + ChatColor.GRAY + " ya no está AFK.";
            AfkManager.announceToOthers(player, announcement);
            
            // Restaurar el display name normal
            AfkManager.removeAfkDisplayName(player);
        }
    }
    
    @Override
    public String getName() {
        return "afk";
    }
    
    @Override
    public String getVariants() {
        return "afk,away";
    }
    
    @Override
    public String getDescription() {
        return "Activa o desactiva el modo AFK con motivo opcional";
    }
    
    @Override
    public String getPermission() {
        return "";
    }
    
    @Override
    public String getSyntax() {
        return "/afk [motivo]";
    }
    
    @Override
    public List<String> getArguments(Player player, String[] args) {
        return new ArrayList<>();
    }
}
