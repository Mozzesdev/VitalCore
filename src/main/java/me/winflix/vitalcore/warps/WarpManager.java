package me.winflix.vitalcore.warps;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.Database;
import me.winflix.vitalcore.general.database.collections.warp.WarpDAO;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.warps.models.Warp;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Manager central para el sistema de warps
 */
public class WarpManager extends Manager {

    // Cache de warps para mejor rendimiento
    private final Map<String, Warp> warpCache = new ConcurrentHashMap<>();
    
    // Cooldowns de warps por jugador
    private final Map<UUID, Map<String, Long>> playerCooldowns = new ConcurrentHashMap<>();
    
    // Warmups activos
    private final Map<UUID, WarmupData> activeWarmups = new ConcurrentHashMap<>();

    // Configuración
    private static final int DEFAULT_WARMUP_TIME = 3; // segundos
    private static final int GLOBAL_COOLDOWN = 10; // segundos
    private static final double MOVEMENT_TOLERANCE = 2.0; // bloques

    public WarpManager(VitalCore plugin) {
        super(plugin);
    }

    @Override
    public WarpManager initialize() {
        try {
            // Crear tablas de base de datos si no existen
            WarpDAO.initialize(Database.getConnection());
        } catch (Exception e) {
            plugin.getLogger().severe("Error al inicializar la base de datos de warps: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Cargar warps al cache
        loadWarpsToCache();
        return this;
    }

    @Override
    public void setupCommands() {
        // Los comandos se registran en Core.java
    }

    @Override
    public void setupEvents() {
        // Los eventos se registran en Core.java
    }

    @Override
    public void registerCommands() {
        // Los comandos se registran en Core.java
    }

    @Override
    public void onDisable() {
        // Cancelar todos los warmups activos
        activeWarmups.values().forEach(warmup -> warmup.task.cancel());
        activeWarmups.clear();
        warpCache.clear();
        playerCooldowns.clear();
    }

    // ==================== GESTIÓN DE WARPS ====================

    public boolean createWarp(String name, Location location, Player creator, String category, boolean isPublic) {
        if (warpExists(name)) {
            return false;
        }

        Warp warp = new Warp(
            name, 
            location, 
            category != null ? category : "general",
            Material.COMPASS,
            0.0, // cost
            0,   // cooldown
            isPublic,
            null, // permission
            creator.getUniqueId()
        );

        if (WarpDAO.createWarp(warp)) {
            warpCache.put(name.toLowerCase(), warp);
            return true;
        }
        return false;
    }

    public boolean deleteWarp(String name) {
        if (WarpDAO.deleteWarp(name)) {
            warpCache.remove(name.toLowerCase());
            return true;
        }
        return false;
    }

    public boolean updateWarp(Warp warp) {
        if (WarpDAO.updateWarp(warp)) {
            warpCache.put(warp.getName().toLowerCase(), warp);
            return true;
        }
        return false;
    }

    public Warp getWarp(String name) {
        Warp warp = warpCache.get(name.toLowerCase());
        if (warp == null) {
            // Intentar cargar desde BD si no está en cache
            warp = WarpDAO.getWarp(name);
            if (warp != null) {
                warpCache.put(name.toLowerCase(), warp);
            }
        }
        return warp;
    }

    public List<Warp> getAllWarps() {
        return new ArrayList<>(warpCache.values());
    }

    public List<Warp> getPublicWarps() {
        return warpCache.values().stream()
                .filter(Warp::isPublic)
                .sorted(Comparator.comparing(Warp::getName))
                .toList();
    }

    public List<Warp> getWarpsByCategory(String category) {
        return warpCache.values().stream()
                .filter(warp -> warp.getCategory().equalsIgnoreCase(category))
                .sorted(Comparator.comparing(Warp::getName))
                .toList();
    }

    public List<Warp> getWarpsByOwner(UUID owner) {
        return warpCache.values().stream()
                .filter(warp -> warp.getOwner().equals(owner))
                .sorted(Comparator.comparing(Warp::getName))
                .toList();
    }

    public List<String> getCategories() {
        return warpCache.values().stream()
                .map(Warp::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    public boolean warpExists(String name) {
        return warpCache.containsKey(name.toLowerCase()) || WarpDAO.warpExists(name);
    }

    // ==================== FAVORITOS ====================

    public List<String> getFavoriteWarps(Player player) {
        return WarpDAO.getFavoriteWarps(player.getUniqueId());
    }

    public boolean addFavorite(Player player, String warpName) {
        if (!warpExists(warpName)) {
            return false;
        }
        return WarpDAO.addFavorite(player.getUniqueId(), warpName);
    }

    public boolean removeFavorite(Player player, String warpName) {
        return WarpDAO.removeFavorite(player.getUniqueId(), warpName);
    }

    public boolean isFavorite(Player player, String warpName) {
        return getFavoriteWarps(player).contains(warpName);
    }

    // ==================== TELETRANSPORTE ====================

    public void teleportToWarp(Player player, String warpName) {
        Warp warp = getWarp(warpName);
        if (warp == null) {
            Utils.errorMessage(player, "El warp '" + warpName + "' no existe.");
            return;
        }

        teleportToWarp(player, warp);
    }

    public void teleportToWarp(Player player, Warp warp) {
        // Verificar disponibilidad del warp
        if (!warp.isAvailable()) {
            Utils.errorMessage(player, "El warp '" + warp.getName() + "' no está disponible (mundo no cargado).");
            return;
        }

        // Verificar permisos
        if (!hasWarpPermission(player, warp)) {
            Utils.errorMessage(player, "No tienes permiso para usar este warp.");
            return;
        }

        // Verificar cooldown
        if (!canUseWarp(player, warp)) {
            long remainingTime = getRemainingCooldown(player, warp.getName());
            Utils.errorMessage(player, "Debes esperar " + remainingTime + " segundos antes de usar este warp.");
            return;
        }

        // Verificar combate (si hay sistema de combate)
        // if (isInCombat(player)) { ... }

        // Iniciar warmup
        startWarmup(player, warp);
    }

    private void startWarmup(Player player, Warp warp) {
        // Cancelar warmup anterior si existe
        cancelWarmup(player);

        Location startLocation = player.getLocation().clone();
        final int warmupTime; // Hacer final para usar en runnable

        // Bypass para admin
        if (player.hasPermission("vitalcore.warp.bypass.warmup")) {
            warmupTime = 0;
        } else {
            warmupTime = DEFAULT_WARMUP_TIME;
        }

        if (warmupTime == 0) {
            // Teletransporte inmediato
            executeTeleport(player, warp);
            return;
        }

        Utils.infoMessage(player, "Teletransportándote a '" + warp.getName() + "' en " + warmupTime + " segundos...");

        WarmupData warmupData = new WarmupData(warp, startLocation, warmupTime);
        
        warmupData.task = new BukkitRunnable() {
            int timeLeft = warmupTime;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                // Verificar movimiento
                if (player.getLocation().distance(startLocation) > MOVEMENT_TOLERANCE) {
                    Utils.errorMessage(player, "Teletransporte cancelado: te has movido demasiado.");
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    executeTeleport(player, warp);
                    cancel();
                    return;
                }

                // Mostrar countdown en actionbar
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                    new TextComponent(Utils.useColors("§eTeletransporte en: §f" + timeLeft + "s")));
                timeLeft--;
            }

            @Override
            public void cancel() {
                super.cancel();
                activeWarmups.remove(player.getUniqueId());
            }
        }.runTaskTimer(plugin, 0L, 20L);

        activeWarmups.put(player.getUniqueId(), warmupData);
    }

    private void executeTeleport(Player player, Warp warp) {
        Location destination = warp.getLocation();

        if (destination == null) {
            Utils.errorMessage(player, "Error: no se pudo cargar la ubicación del warp.");
            return;
        }

        // Teletransporte seguro - implementación básica
        if (destination.getWorld() != null) {
            // Verificar que el bloque destino sea seguro
            if (destination.getBlock().getType().isSolid() || 
                destination.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                // Buscar un lugar seguro cerca
                for (int y = destination.getBlockY(); y < destination.getWorld().getMaxHeight(); y++) {
                    Location testLoc = destination.clone();
                    testLoc.setY(y);
                    if (!testLoc.getBlock().getType().isSolid() && 
                        !testLoc.clone().add(0, 1, 0).getBlock().getType().isSolid() &&
                        testLoc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                        destination = testLoc;
                        break;
                    }
                }
            }
            player.teleport(destination);
        } else {
            Utils.errorMessage(player, "Error: mundo no disponible.");
            return;
        }

        // Actualizar estadísticas
        WarpDAO.updateWarpUsage(warp.getName());
        warp.incrementUsage();

        // Establecer cooldown
        setCooldown(player, warp);

        // Mensaje de éxito
        Utils.successMessage(player, "¡Te has teletransportado a '" + warp.getName() + "'!");
    }

    public void cancelWarmup(Player player) {
        WarmupData warmup = activeWarmups.remove(player.getUniqueId());
        if (warmup != null) {
            warmup.task.cancel();
            Utils.errorMessage(player, "Teletransporte cancelado.");
        }
    }

    public boolean hasActiveWarmup(Player player) {
        return activeWarmups.containsKey(player.getUniqueId());
    }

    // ==================== PERMISOS ====================

    private boolean hasWarpPermission(Player player, Warp warp) {
        // Admin bypass
        if (player.hasPermission("vitalcore.warp.admin")) {
            return true;
        }

        // Warp privado - solo el dueño
        if (!warp.isPublic() && !warp.getOwner().equals(player.getUniqueId())) {
            return false;
        }

        // Permiso básico
        if (!player.hasPermission("vitalcore.warp.use")) {
            return false;
        }

        // Permiso específico del warp
        if (warp.getPermission() != null && !warp.getPermission().isEmpty()) {
            return player.hasPermission(warp.getPermission());
        }

        // Permiso por categoría
        String categoryPerm = "vitalcore.warp.category." + warp.getCategory().toLowerCase();
        if (!player.hasPermission(categoryPerm)) {
            return false;
        }

        // Permiso específico del warp por nombre
        String warpPerm = "vitalcore.warp.warp." + warp.getName().toLowerCase();
        return player.hasPermission(warpPerm);
    }

    // ==================== COOLDOWNS ====================

    private boolean canUseWarp(Player player, Warp warp) {
        if (player.hasPermission("vitalcore.warp.bypass.cooldown")) {
            return true;
        }

        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null) {
            return true;
        }

        Long lastUse = cooldowns.get(warp.getName());
        if (lastUse == null) {
            return true;
        }

        long cooldownTime = Math.max(warp.getCooldown(), GLOBAL_COOLDOWN) * 1000L;
        return System.currentTimeMillis() - lastUse >= cooldownTime;
    }

    private void setCooldown(Player player, Warp warp) {
        if (player.hasPermission("vitalcore.warp.bypass.cooldown")) {
            return;
        }

        playerCooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                      .put(warp.getName(), System.currentTimeMillis());
    }

    private long getRemainingCooldown(Player player, String warpName) {
        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null) {
            return 0;
        }

        Long lastUse = cooldowns.get(warpName);
        if (lastUse == null) {
            return 0;
        }

        Warp warp = getWarp(warpName);
        long cooldownTime = Math.max(warp != null ? warp.getCooldown() : 0, GLOBAL_COOLDOWN) * 1000L;
        long elapsed = System.currentTimeMillis() - lastUse;
        
        return Math.max(0, (cooldownTime - elapsed) / 1000);
    }

    // ==================== UTILIDADES ====================

    private void loadWarpsToCache() {
        List<Warp> warps = WarpDAO.getAllWarps();
        warpCache.clear();
        for (Warp warp : warps) {
            warpCache.put(warp.getName().toLowerCase(), warp);
        }
        plugin.getLogger().info("Cargados " + warps.size() + " warps al cache.");
    }

    public void reloadWarps() {
        loadWarpsToCache();
    }

    // ==================== CLASES INTERNAS ====================

    private static class WarmupData {
        final Warp warp;
        final Location startLocation;
        final int warmupTime;
        BukkitTask task;

        WarmupData(Warp warp, Location startLocation, int warmupTime) {
            this.warp = warp;
            this.startLocation = startLocation;
            this.warmupTime = warmupTime;
        }
    }
}
