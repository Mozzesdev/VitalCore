package me.winflix.vitalcore.structures.utils;

import org.bukkit.Location;
import org.bukkit.Material;

public class StructureBlockPosition {
    private Location location;
    private Material material;
    private double distanceToCenter;

    public StructureBlockPosition(Location location, Material material, double distanceToCenter) {
        this.location = location;
        this.material = material;
        this.distanceToCenter = distanceToCenter;
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }

    public double getDistanceToCenter() {
        return distanceToCenter;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setDistanceToCenter(double distanceToCenter) {
        this.distanceToCenter = distanceToCenter;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}
