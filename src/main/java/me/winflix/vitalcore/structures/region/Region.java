package me.winflix.vitalcore.structures.region;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Region {

    private String ownerId; // El ID del jugador dueño de la región
    private List<Location> locations; // Lista de ubicaciones de los bloques que conforman la región
    private String id;

    // Constructor
    public Region(String id, String ownerId, List<Location> locations) {
        this.ownerId = ownerId;
        this.id = id;
        this.locations = new ArrayList<>(locations);
    }

    public String getId() {
        return id;
    }

    // Obtiene el ID del dueño de la región
    public String getOwnerId() {
        return ownerId;
    }

    // Verifica si una ubicación está dentro de la región
    public boolean contains(Location loc) {
        return locations.contains(loc);
    }

    // Devuelve todas las ubicaciones de la región
    public List<Location> getLocations() {
        return locations;
    }

    // Sobrescribir el toString para facilitar la visualización
    @Override
    public String toString() {
        return "Region{" +
                "ownerId='" + ownerId + '\'' +
                ", locations=" + locations + '\'' +
                ", id=" + id +
                '}';
    }
}
