package me.winflix.vitalcore.warps.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Modelo que representa un warp en el sistema
 */
public class Warp {
    private String name;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private String category;
    private Material icon;
    private double cost;
    private int cooldown;
    private boolean isPublic;
    private String permission;
    private UUID owner;
    private long createdAt;
    private long lastUsed;
    private int usageCount;

    // Constructor completo
    public Warp(String name, Location location, String category, Material icon,
            double cost, int cooldown, boolean isPublic, String permission, UUID owner) {
        this.name = name;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.category = category;
        this.icon = icon;
        this.cost = cost;
        this.cooldown = cooldown;
        this.isPublic = isPublic;
        this.permission = permission;
        this.owner = owner;
        this.createdAt = System.currentTimeMillis();
        this.lastUsed = 0;
        this.usageCount = 0;
    }

    // Constructor para cargar desde BD
    public Warp(String name, String worldName, double x, double y, double z, float yaw, float pitch,
            String category, Material icon, double cost, int cooldown, boolean isPublic,
            String permission, UUID owner, long createdAt, long lastUsed, int usageCount) {
        this.name = name;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.category = category;
        this.icon = icon;
        this.cost = cost;
        this.cooldown = cooldown;
        this.isPublic = isPublic;
        this.permission = permission;
        this.owner = owner;
        this.createdAt = createdAt;
        this.lastUsed = lastUsed;
        this.usageCount = usageCount;
    }

    /**
     * Convierte las coordenadas almacenadas a un objeto Location
     * 
     * @return Location del warp o null si el mundo no existe
     */
    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Actualiza la ubicación del warp
     */
    public void setLocation(Location location) {
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    /**
     * Incrementa el contador de uso y actualiza última vez usado
     */
    public void incrementUsage() {
        this.usageCount++;
        this.lastUsed = System.currentTimeMillis();
    }

    /**
     * Verifica si el warp está disponible (mundo existe)
     */
    public boolean isAvailable() {
        return Bukkit.getWorld(worldName) != null;
    }

    // Getters y Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    @Override
    public String toString() {
        return "Warp{" +
                "name='" + name + '\'' +
                ", worldName='" + worldName + '\'' +
                ", category='" + category + '\'' +
                ", isPublic=" + isPublic +
                ", owner=" + owner +
                '}';
    }
}
