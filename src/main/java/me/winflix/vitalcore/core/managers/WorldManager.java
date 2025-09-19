package me.winflix.vitalcore.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.core.managers.BackManager;
import me.winflix.vitalcore.general.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class WorldManager {
    private final FileConfiguration config;
    private final Map<Player, Long> teleportCooldowns = new HashMap<>();
    
    public WorldManager() {
        config = VitalCore.fileManager.getWorldsConfig();
        loadWorldSpawns();
    }
    
    private void loadWorldSpawns() {
        // Cargar todos los spawns desde la configuración al iniciar
        for(String worldName : config.getKeys(false)) {
            if(config.contains(worldName + ".spawn")) {
                Bukkit.getLogger().info("Spawn cargado para mundo: " + worldName);
            }
        }
    }
    
    public void setSpawn(Player player) {
        World world = player.getWorld();
        Location loc = player.getLocation();
        
        String path = world.getName() + ".spawn";
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
        
        VitalCore.fileManager.saveWorldsConfig();
        Utils.infoMessage(player, "Spawn del mundo " + world.getName() + " establecido!");
    }
    
    public Location getSpawn(World world) {
        String path = world.getName() + ".spawn";
        if(!config.contains(path)) return null;
        
        return new Location(
            world,
            config.getDouble(path + ".x"),
            config.getDouble(path + ".y"),
            config.getDouble(path + ".z"),
            (float) config.getDouble(path + ".yaw"),
            (float) config.getDouble(path + ".pitch")
        );
    }
    
    public void teleportToSpawn(Player player, boolean instant) {
        Location spawn = getSpawn(player.getWorld());
        
        if(spawn == null) {
            Utils.errorMessage(player, "Este mundo no tiene spawn establecido.");
            return;
        }
        
        if(instant) {
            // Registrar ubicación previa antes del teletransporte
            BackManager.setPreviousLocation(player);
            player.teleport(spawn);
        } else {
            // Teleportación con delay y efectos
            Utils.infoMessage(player, "Teletransportando al spawn en 3 segundos...");
            Bukkit.getScheduler().runTaskLater(VitalCore.getPlugin(), () -> {
                if(player.isOnline()) {
                    // Registrar ubicación previa antes del teletransporte
                    BackManager.setPreviousLocation(player);
                    player.teleport(spawn);
                    player.sendTitle("§b§lSPAWN", "", 10, 40, 10);
                }
            }, 60L); // 3 segundos (20 ticks = 1 segundo)
        }
    }
    
    public boolean checkCooldown(Player player) {
        if(!teleportCooldowns.containsKey(player)) return true;
        
        long secondsElapsed = (System.currentTimeMillis() - teleportCooldowns.get(player)) / 1000;
        if(secondsElapsed < 30) {
            Utils.errorMessage(player, "Espera " + (30 - secondsElapsed) + " segundos antes de usar esto de nuevo.");
            return false;
        }
        return true;
    }
    
    public void applyCooldown(Player player) {
        teleportCooldowns.put(player, System.currentTimeMillis());
    }
}