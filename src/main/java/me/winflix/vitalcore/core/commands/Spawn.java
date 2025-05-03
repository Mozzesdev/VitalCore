package me.winflix.vitalcore.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.core.Core;
import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;

import java.util.Collections;
import java.util.List;

public class Spawn extends BaseCommand {
    
    @Override
    public String getName() {
        return "spawn";
    }
    
    @Override
    public String getVariants() {
        return "worldspawn";
    }
    
    @Override
    public String getDescription() {
        return "Teletransporta al spawn del mundo actual";
    }
    
    @Override
    public String getPermission() {
        return null;
    }
    
    @Override
    public String getSyntax() {
        return "/spawn [jugador]";
    }
    
    @Override
    public List<String> getArguments(Player player, String[] args) {
        return args.length == 1 ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList() : Collections.emptyList();
    }
    
    @Override
    public void perform(Player player, String[] args) {
        // Modo administrador: teletransportar a otros jugadores
        if(args.length > 0) {
            if(!player.hasPermission("vitalcore.spawn.others") || !player.isOp()) {
                Utils.errorMessage(player, "No puedes teletransportar a otros jugadores.");
                return;
            }
            
            Player target = Bukkit.getPlayer(args[0]);
            if(target == null) {
                Utils.errorMessage(player, "Jugador no encontrado.");
                return;
            }
            
            Core.worldManager.teleportToSpawn(target, true);
            Utils.infoMessage(player, "Has teletransportado a " + target.getName() + " al spawn!");
            return;
        }
        
        // Auto-teletransporte
        if(!Core.worldManager.checkCooldown(player)) return;
        
        Core.worldManager.teleportToSpawn(player, false);
        Core.worldManager.applyCooldown(player);
    }
}