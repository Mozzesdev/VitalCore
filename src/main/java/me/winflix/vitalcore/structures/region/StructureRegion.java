package me.winflix.vitalcore.structures.region;

import java.util.List;

import org.bukkit.Location;

import me.winflix.vitalcore.structures.models.Structure;

public class StructureRegion extends Region {

    Structure structure;

    public StructureRegion(String id, String ownerId, List<Location> locations, Structure structure) {
        super(id, ownerId, locations);
        this.structure = structure;
    }

    public Structure getStructure() {
        return structure;
    }
    
}
