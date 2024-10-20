package me.winflix.vitalcore.structures.region;

import org.bukkit.Location;

import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class RegionManager {

    // Mapa que almacena todas las regiones con su ID único
    private Map<String, Region> regions = new HashMap<>();

    // Añadir una región al mapa
    public void addRegion(String id, Region region) {
        regions.put(id, region);
    }

    // Eliminar una región por su ID
    public void removeRegion(String id) {
        regions.remove(id);
    }

    // Verificar si una ubicación está dentro de alguna región
    public boolean isInsideAnyRegion(Location loc) {
        for (Region region : regions.values()) {
            if (region.contains(loc)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInsideRegion(Location loc, String regionId) {
        Region region = getRegion(regionId); // Obtener la región por ID
        if (region != null) {
            return region.contains(loc); // Comprobar si la ubicación está dentro de la región
        }
        return false; // Si la región no existe, devolvemos false
    }

    // Obtener una región por su ID
    public Region getRegion(String id) {
        return regions.get(id);
    }

    // Obtener todas las regiones
    public Map<String, Region> getAllRegions() {
        return regions;
    }

    // Obtener las regiones de un jugador específico
    public List<Region> getRegionsByOwner(String ownerId) {
        return regions.values().stream()
                .filter(region -> region.getOwnerId().equals(ownerId))
                .toList();
    }

    public Region getRegionByLocation(Location location) {
        return regions.values().stream()
                .filter(region -> region.getLocations().contains(location))
                .findFirst() // Obtiene la primera región que coincida
                .orElse(null); // Devuelve null si no se encuentra ninguna región
    }

    public Region findAdjacentRegion(Location loc, double distance) {
        // Recorre todas las regiones en el mapa
        for (Region region : regions.values()) {
            // Revisa todas las ubicaciones de la región
            for (Location regionLoc : region.getLocations()) {
                // Comprueba si la distancia total es menor o igual a la distancia especificada
                if (regionLoc.distance(loc) <= distance) {
                    // Si se encuentra una región adyacente, devuélvela
                    return region;
                }
            }
        }
        // Si no se encontró ninguna región adyacente, devuelve null
        return null;
    }

    // Sobrescribir el toString para facilitar la visualización
    @Override
    public String toString() {
        return "RegionManager{" +
                "regions=" + regions +
                '}';
    }
}
