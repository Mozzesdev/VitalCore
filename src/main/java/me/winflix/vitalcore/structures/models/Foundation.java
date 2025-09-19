package me.winflix.vitalcore.structures.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import me.winflix.vitalcore.structures.region.RegionManager;
import me.winflix.vitalcore.structures.region.StructureRegion;
import me.winflix.vitalcore.structures.utils.StructureManager;
import net.minecraft.core.Direction;

public class Foundation extends Structure {

    Material[][][] stackedMatriz = null;
    boolean isStacked = false;

    public Foundation(String name, float health, StructureItem item, Material[][][] blocks, String id,
            Recipe rawRecipe) {
        super(name, StructuresType.FOUNDATION, health, blocks, id, item);
        this.rawRecipe = rawRecipe;
        this.shapedRecipe = new ShapedRecipe(new NamespacedKey(VitalCore.getPlugin(), id), getItemStack());
    }

    public boolean isStacked() {
        return isStacked;
    }

    public void setStacked(boolean isStacked) {
        this.isStacked = isStacked;
    }

    public Foundation(String name, float health, StructureItem item, Material[][][] blocks, String id) {
        super(name, StructuresType.FOUNDATION, health, blocks, id, item);
    }

    public StructureItem getItem() {
        return item;
    }

    public List<Location> getBlockLocations(Location loc) {
        List<Location> baseLocations = super.getBlockLocations(loc);

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
    public boolean build(Location location, Player player, RegionManager regionManager,
            StructureManager structureManager) {

        // Ajusta la ubicación para fundaciones
        location.subtract(0, 1, 0);

        // Verifica si ya hay una fundación en la región antes de construir
        if (regionManager.isInsideAnyRegion(location)) {
            player.sendMessage(Utils.useColors("&cNo puedes colocar una fundación encima de otra."));
            return false;
        }

        // Construcción en la ubicación final determinada
        if (super.build(location, player)) {
            // Lógica adicional tras la construcción exitosa
            List<Location> blockLocations = getBlockLocations(buildLocation);
            String regionId = UUID.randomUUID().toString();

            StructureRegion newRegion = new StructureRegion(regionId, player.getUniqueId().toString(), blockLocations,
                    this);
            regionManager.addRegion(regionId, newRegion);

            return true;
        }

        return false;
    }

    public void updateMatriz(Direction direction, StructureManager manager) {
        if (!isStacked || direction == null) {
            this.matriz = manager.getStructureById(id).getMatriz();
            return;
        }

        Material[][] secondLayer = this.matriz[1];

        switch (direction) {
            case WEST:
                for (int i = 1; i < secondLayer.length - 1; i++) {
                    secondLayer[i][secondLayer[0].length - 1] = Material.AIR;
                }
                break;

            case EAST:
                for (int i = 1; i < secondLayer.length - 1; i++) {
                    secondLayer[i][0] = Material.AIR;
                }
                break;

            case SOUTH:
                for (int i = 1; i < secondLayer[0].length - 1; i++) {
                    secondLayer[0][i] = Material.AIR;
                }
                break;

            case NORTH:
                for (int i = 1; i < secondLayer[0].length - 1; i++) {
                    secondLayer[secondLayer.length - 1][i] = Material.AIR;
                }
                break;
            default:
                break;
        }

        this.matriz[1] = secondLayer;
    }

    public void destroy(Player player, RegionManager regionManager, String regionId) {
        super.destroy();

        if (player.getGameMode() == GameMode.SURVIVAL) {
            dropItem();
        }

        // Elimina la región asociada
        regionManager.removeRegion(regionId);
    }

    @Override
    public ItemStack getItemStack() {
        if (item != null) {
            ItemStack itemStack = item.toItemStack();

            ItemMeta meta = itemStack.getItemMeta();

            // Usando PersistentDataContainer en lugar de NMS
            NamespacedKey keyData = new NamespacedKey("yourplugin", "structure_data");
            NamespacedKey keyItem = new NamespacedKey("yourplugin", "item_type");

            PersistentDataContainer pdc = meta.getPersistentDataContainer();

            Foundation foundation = this;
            foundation.setBlockPositions(new ArrayList<>());
            foundation.setBuildLocation(null);
            foundation.setFace(null);

            String structureJson = StructureManager.toJson(foundation);

            pdc.set(keyData, PersistentDataType.STRING, structureJson);
            pdc.set(keyItem, PersistentDataType.STRING, "structure_item");

            itemStack.setItemMeta(meta);

            return itemStack;
        }

        return null;
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
