package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando para reparar items consumiendo experiencia
 */
public class Repair extends BaseCommand {
    
    // Costo base de experiencia por punto de durabilidad
    private static final int XP_PER_DURABILITY = 2;
    
    // Costo mínimo en XP para cualquier reparación
    private static final int MIN_XP_COST = 5;
    
    @Override
    public void perform(Player player, String[] args) {
        String mode = "hand"; // Por defecto reparar item en mano
        
        if (args.length > 0) {
            mode = args[0].toLowerCase();
            if (!mode.equals("hand") && !mode.equals("all")) {
                Utils.errorMessage(player, "Uso: /repair [hand | all]");
                return;
            }
        }
        
        // Verificar permisos específicos según el modo
        if (mode.equals("hand")) {
            if (!player.hasPermission("vitalcore.repair.hand") && !player.isOp()) {
                Utils.errorMessage(player, "No tienes permiso para reparar items en tu mano.");
                return;
            }
            repairHand(player);
        } else {
            if (!player.hasPermission("vitalcore.repair.all") && !player.isOp()) {
                Utils.errorMessage(player, "No tienes permiso para reparar todo tu inventario.");
                return;
            }
            repairAll(player);
        }
    }
    
    /**
     * Repara el item en la mano del jugador
     * @param player El jugador
     */
    private void repairHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            Utils.errorMessage(player, "No tienes ningún item en tu mano.");
            return;
        }
        
        if (!isDamageable(item)) {
            Utils.errorMessage(player, "Este item no puede ser reparado.");
            return;
        }
        
        if (!isDamaged(item)) {
            Utils.successMessage(player, "Este item ya está completamente reparado.");
            return;
        }
        
        int xpCost = calculateRepairCost(item);
        
        if (!hasEnoughXP(player, xpCost)) {
            int currentXP = getTotalExperience(player);
            Utils.errorMessage(player, "No tienes suficiente experiencia. Necesitas " + 
                             xpCost + " XP pero solo tienes " + currentXP + " XP.");
            return;
        }
        
        // Consumir experiencia
        removeExperience(player, xpCost);
        
        // Reparar item
        repairItem(item);
        
        Utils.successMessage(player, "¡Item reparado! Costo: §e" + 
                         xpCost + " XP§a.");
    }
    
    /**
     * Repara todos los items dañados del inventario del jugador
     * @param player El jugador
     */
    private void repairAll(Player player) {
        List<ItemStack> itemsToRepair = new ArrayList<>();
        int totalCost = 0;
        
        // Calcular items a reparar y costo total
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isDamageable(item) && isDamaged(item)) {
                itemsToRepair.add(item);
                totalCost += calculateRepairCost(item);
            }
        }
        
        // Incluir armadura
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && isDamageable(item) && isDamaged(item)) {
                itemsToRepair.add(item);
                totalCost += calculateRepairCost(item);
            }
        }
        
        // Incluir item en mano secundaria
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && isDamageable(offHand) && isDamaged(offHand)) {
            itemsToRepair.add(offHand);
            totalCost += calculateRepairCost(offHand);
        }
        
        if (itemsToRepair.isEmpty()) {
            Utils.successMessage(player, "No tienes items que necesiten reparación.");
            return;
        }
        
        if (!hasEnoughXP(player, totalCost)) {
            int currentXP = getTotalExperience(player);
            Utils.errorMessage(player, "No tienes suficiente experiencia. Necesitas " + 
                             totalCost + " XP pero solo tienes " + currentXP + " XP.");
            return;
        }
        
        // Consumir experiencia
        removeExperience(player, totalCost);
        
        // Reparar todos los items
        for (ItemStack item : itemsToRepair) {
            repairItem(item);
        }
        
        Utils.successMessage(player, "¡" + itemsToRepair.size() + " items reparados! Costo total: §e" + 
                         totalCost + " XP§a.");
    }
    
    /**
     * Verifica si un item puede ser reparado
     * @param item El item
     * @return true si es reparable
     */
    private boolean isDamageable(ItemStack item) {
        return item.getItemMeta() instanceof Damageable && item.getType().getMaxDurability() > 0;
    }
    
    /**
     * Verifica si un item está dañado
     * @param item El item
     * @return true si está dañado
     */
    private boolean isDamaged(ItemStack item) {
        if (!(item.getItemMeta() instanceof Damageable)) {
            return false;
        }
        Damageable damageable = (Damageable) item.getItemMeta();
        return damageable.getDamage() > 0;
    }
    
    /**
     * Calcula el costo de experiencia para reparar un item
     * @param item El item
     * @return Costo en XP
     */
    private int calculateRepairCost(ItemStack item) {
        if (!(item.getItemMeta() instanceof Damageable)) {
            return 0;
        }
        
        Damageable damageable = (Damageable) item.getItemMeta();
        int damage = damageable.getDamage();
        int cost = damage * XP_PER_DURABILITY;
        
        return Math.max(cost, MIN_XP_COST);
    }
    
    /**
     * Repara completamente un item
     * @param item El item a reparar
     */
    private void repairItem(ItemStack item) {
        if (!(item.getItemMeta() instanceof Damageable)) {
            return;
        }
        
        Damageable damageable = (Damageable) item.getItemMeta();
        damageable.setDamage(0);
        item.setItemMeta(damageable);
    }
    
    /**
     * Verifica si el jugador tiene suficiente experiencia
     * @param player El jugador
     * @param cost Costo requerido
     * @return true si tiene suficiente XP
     */
    private boolean hasEnoughXP(Player player, int cost) {
        return getTotalExperience(player) >= cost;
    }
    
    /**
     * Obtiene la experiencia total del jugador
     * @param player El jugador
     * @return Experiencia total
     */
    private int getTotalExperience(Player player) {
        int level = player.getLevel();
        float exp = player.getExp();
        
        // Calcular XP total basado en el sistema de Minecraft
        int totalXP = 0;
        
        // XP de niveles completos
        if (level <= 16) {
            totalXP = level * level + 6 * level;
        } else if (level <= 31) {
            totalXP = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            totalXP = (int) (4.5 * level * level - 162.5 * level + 2220);
        }
        
        // XP del progreso actual
        int xpForNextLevel = getXPNeededForLevel(level + 1) - getXPNeededForLevel(level);
        totalXP += (int) (exp * xpForNextLevel);
        
        return totalXP;
    }
    
    /**
     * Obtiene la XP necesaria para alcanzar un nivel específico
     * @param level El nivel
     * @return XP necesaria
     */
    private int getXPNeededForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }
    
    /**
     * Remueve experiencia del jugador
     * @param player El jugador
     * @param amount Cantidad a remover
     */
    private void removeExperience(Player player, int amount) {
        int currentXP = getTotalExperience(player);
        int newXP = Math.max(0, currentXP - amount);
        
        // Resetear XP del jugador
        player.setLevel(0);
        player.setExp(0);
        
        // Restaurar la nueva cantidad
        giveExperience(player, newXP);
    }
    
    /**
     * Da experiencia al jugador
     * @param player El jugador
     * @param amount Cantidad a dar
     */
    private void giveExperience(Player player, int amount) {
        int remainingXP = amount;
        int level = 0;
        
        while (remainingXP > 0) {
            int xpForNextLevel = getXPNeededForLevel(level + 1) - getXPNeededForLevel(level);
            
            if (remainingXP >= xpForNextLevel) {
                remainingXP -= xpForNextLevel;
                level++;
            } else {
                // XP parcial para el nivel actual
                float progress = (float) remainingXP / xpForNextLevel;
                player.setLevel(level);
                player.setExp(progress);
                break;
            }
        }
        
        if (remainingXP == 0) {
            player.setLevel(level);
            player.setExp(0);
        }
    }
    
    @Override
    public String getName() {
        return "repair";
    }
    
    @Override
    public String getVariants() {
        return "repair,fix,reparar";
    }
    
    @Override
    public String getDescription() {
        return "Repara items consumiendo experiencia (requiere permisos específicos)";
    }
    
    @Override
    public String getPermission() {
        return "vitalcore.repair";
    }
    
    @Override
    public String getSyntax() {
        return "/repair [hand | all]";
    }
    
    @Override
    public List<String> getArguments(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (args.length == 1) {
            arguments.add("hand");
            arguments.add("all");
        }
        return arguments;
    }
}
