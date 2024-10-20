package me.winflix.vitalcore.structures.events;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.winflix.vitalcore.structures.region.RegionManager;
import me.winflix.vitalcore.structures.region.StructureRegion;
import me.winflix.vitalcore.structures.utils.StructureBlockPosition;

public class StructureProtection implements Listener {
    RegionManager regionManager;

    public StructureProtection(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onBurnStructure(BlockBurnEvent event) {
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();

        StructureRegion structureRegion = (StructureRegion) regionManager.getRegionByLocation(blockLocation);

        if (structureRegion != null) {
            List<StructureBlockPosition> structurePositions = structureRegion.getStructure().getBlockPositions();

            boolean isBlockPartOfStructure = structurePositions.stream()
                    .anyMatch(pos -> pos.getLocation().equals(blockLocation));

            if (isBlockPartOfStructure) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEndermanPickBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Enderman) {
            Block block = event.getBlock();
            Location blockLocation = block.getLocation();
            StructureRegion structureRegion = (StructureRegion) regionManager.getRegionByLocation(blockLocation);

            if (structureRegion != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            Location blockLocation = block.getLocation();
            StructureRegion structureRegion = (StructureRegion) regionManager.getRegionByLocation(blockLocation);

            if (structureRegion != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            Location blockLocation = block.getLocation();
            StructureRegion structureRegion = (StructureRegion) regionManager.getRegionByLocation(blockLocation);

            if (structureRegion != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();

        while (iterator.hasNext()) {
            Block block = iterator.next();
            Location blockLocation = block.getLocation();
            StructureRegion structureRegion = (StructureRegion) regionManager.getRegionByLocation(blockLocation);

            if (structureRegion != null) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();

        while (iterator.hasNext()) {
            Block block = iterator.next();
            Location blockLocation = block.getLocation();
            StructureRegion structureRegion = (StructureRegion) regionManager.getRegionByLocation(blockLocation);

            if (structureRegion != null) {
                iterator.remove();
            }
        }
    }
}