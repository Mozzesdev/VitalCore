package me.winflix.vitalcore.structures.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.structures.interfaces.StructuresType;
import me.winflix.vitalcore.structures.region.Region;
import me.winflix.vitalcore.structures.region.RegionManager;
import me.winflix.vitalcore.structures.region.StructureRegion;
import me.winflix.vitalcore.structures.utils.StructureBlockPosition;
import me.winflix.vitalcore.structures.utils.StructureManager;
import net.minecraft.core.Direction;

public class Foundation extends Structure {

    public Foundation(String name, float health, StructureItem item, Material[][][] blocks, String id,
            Recipe rawRecipe) {
        super(name, StructuresType.FOUNDATION, health, blocks, id, item);
        this.rawRecipe = rawRecipe;
        this.shapedRecipe = new ShapedRecipe(new NamespacedKey(VitalCore.getPlugin(), id), getItemStack());
    }

    public Foundation(String name, float health, StructureItem item, Material[][][] blocks, String id) {
        super(name, StructuresType.FOUNDATION, health, blocks, id, item);
    }

    public StructureItem getItem() {
        return item;
    }

    public List<Location> getBlockLocations(Location loc) {
        Location locationFoundation = loc.subtract(0, 1, 0); // Ajusta la ubicación para fundaciones
        List<Location> baseLocations = super.getBlockLocations(locationFoundation);

        // Ajustar las ubicaciones para considerar una altura de -4 a 4 alrededor del
        // centro
        List<Location> adjustedLocations = new ArrayList<>();
        for (Location locBase : baseLocations) {
            for (int yOffset = -4; yOffset <= 4; yOffset++) {
                adjustedLocations.add(locBase.clone().add(0, yOffset, 0));
            }
        }

        return adjustedLocations;
    }

    @Override
    public boolean build(Location location, Player player, RegionManager regionManager) {
        List<Location> blockLocations = getBlockLocations(location);

        // Verifica si ya hay una fundación en la región antes de construir
        if (regionManager.isInsideAnyRegion(location)) {
            player.sendMessage(Utils.useColors("&cNo puedes colocar una fundación encima de otra."));
            return false;
        }

        double distance = StructureManager.getStructureBorderDistance(matriz);

        Region adyacentRegion = regionManager.findAdjacentRegion(location, distance + 1);

        if (adyacentRegion instanceof StructureRegion && adyacentRegion != null) {

            Structure adyacentStructure = ((StructureRegion) adyacentRegion).getStructure();
            Direction direction = getBuildDirection(location, adyacentStructure);

            player.sendMessage(adyacentStructure.getName());
            player.sendMessage(direction.getName());

            return true;
        }

        if (super.build(location, player, regionManager)) {
            String regionId = UUID.randomUUID().toString();
            StructureRegion newRegion = new StructureRegion(regionId, player.getUniqueId().toString(),
                    blockLocations,
                    this);
            regionManager.addRegion(regionId, newRegion);

            return true;
        }

        return false;
    }

    private Direction getBuildDirection(Location buildLocation, Structure adjacentStructure) {
        List<Location> adjacentBlockPositions = adjacentStructure.getBlockPositions().stream()
                .map(StructureBlockPosition::getLocation)
                .collect(Collectors.toList());

        // Encuentra el borde más cercano de la estructura adyacente
        Location closestEdge = findClosestEdge(buildLocation, adjacentBlockPositions);

        if (closestEdge != null) {
            VitalCore.Log.info(closestEdge.toString());

            // Compara las diferencias de coordenadas
            double diffX = buildLocation.getX() - closestEdge.getX();
            double diffZ = buildLocation.getZ() - closestEdge.getZ();

            // Si no es diagonal, sigue con la lógica normal
            if (Math.abs(diffX) > Math.abs(diffZ)) {
                // La diferencia en X es mayor, entonces es ESTE u OESTE
                return (diffX > 0) ? Direction.EAST : Direction.WEST;
            } else {
                // La diferencia en Z es mayor, entonces es NORTE o SUR
                return (diffZ > 0) ? Direction.SOUTH : Direction.NORTH;
            }
        }
        return null;
    }

    // Encuentra el borde más cercano de la estructura adyacente
    private Location findClosestEdge(Location buildLocation, List<Location> blockPositions) {
        Location closest = null;
        double closestAlignment = Double.MAX_VALUE;

        for (Location pos : blockPositions) {
            double diffX = Math.abs(buildLocation.getX() - pos.getX());
            double diffZ = Math.abs(buildLocation.getZ() - pos.getZ());

            // Si las X o Z están alineadas, damos prioridad a esa alineación
            if (buildLocation.getX() == pos.getX()) {
                // Si las X coinciden, solo comparamos las Z
                if (diffZ < closestAlignment) {
                    closestAlignment = diffZ;
                    closest = pos;
                }
            } else if (buildLocation.getZ() == pos.getZ()) {
                // Si las Z coinciden, solo comparamos las X
                if (diffX < closestAlignment) {
                    closestAlignment = diffX;
                    closest = pos;
                }
            } else {
                // Si no están alineados ni en X ni en Z, usamos la suma de diferencias
                double totalDiff = diffX + diffZ;
                if (totalDiff < closestAlignment) {
                    closestAlignment = totalDiff;
                    closest = pos;
                }
            }
        }

        return closest;
    }

    public void destroy(Player player, RegionManager regionManager, String regionId) {
        super.destroy();

        if (player.getGameMode() == GameMode.SURVIVAL) {
        }
        dropItem();

        // Elimina la región asociada
        regionManager.removeRegion(regionId);
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack itemStack = item.toItemStack();

        ItemMeta meta = itemStack.getItemMeta();

        // Usando PersistentDataContainer en lugar de NMS
        NamespacedKey keyData = new NamespacedKey("yourplugin", "structure_data");
        NamespacedKey keyItem = new NamespacedKey("yourplugin", "item_type");

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String structureJson = StructureManager.toJson(this);

        pdc.set(keyData, PersistentDataType.STRING, structureJson);
        pdc.set(keyItem, PersistentDataType.STRING, "structure_item");

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Foundation {")
                .append("\n  Name: ").append(getName())
                .append("\n  Type: ").append(getType() != null ? getType().toString() : "null")
                .append("\n  ID: ").append(getId())
                .append("\n  Health: ").append(getHealth())
                .append("\n  Item: ").append(item != null ? item.toString() : "null")
                .append("\n  Recipe: ").append(rawRecipe != null ? rawRecipe.toString() : "null")
                .append("\n  Block Positions: ").append(getBlockPositions())
                .append("\n  Matrix Dimensions: ").append(getMatriz().length).append(" x ")
                .append(getMatriz().length > 0 ? getMatriz()[0].length : 0).append(" x ")
                .append(getMatriz().length > 0 && getMatriz()[0].length > 0 ? getMatriz()[0][0].length : 0)
                .append("\n}");
        return sb.toString();
    }

}
