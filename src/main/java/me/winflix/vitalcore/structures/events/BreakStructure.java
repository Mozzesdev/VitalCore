package me.winflix.vitalcore.structures.events;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.winflix.vitalcore.structures.region.RegionManager;
import me.winflix.vitalcore.structures.region.StructureRegion;
import me.winflix.vitalcore.structures.utils.StructureBlockPosition;

public class BreakStructure implements Listener {
    RegionManager regionManager;

    public BreakStructure(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onBreakStructure(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Location blockLocation = block.getLocation();

        StructureRegion structureRegion = (StructureRegion) regionManager.getRegionByLocation(blockLocation);

        if (structureRegion != null) {
            List<StructureBlockPosition> structurePositions = structureRegion.getStructure().getBlockPositions();

            boolean isPart = structurePositions.stream()
                    .anyMatch(pos -> pos.getLocation().equals(blockLocation));

            if (isPart) {
                event.setCancelled(true);
                structureRegion.getStructure().destroy(player, regionManager, structureRegion.getId());
            }
        }
    }
}
