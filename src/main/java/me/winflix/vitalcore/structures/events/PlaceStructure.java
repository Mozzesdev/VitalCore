package me.winflix.vitalcore.structures.events;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.structures.models.Structure;
import me.winflix.vitalcore.structures.region.RegionManager;
import me.winflix.vitalcore.structures.utils.StructureManager;

public class PlaceStructure implements Listener {

  RegionManager regionManager;

  public PlaceStructure(RegionManager regionManager) {
    this.regionManager = regionManager;
  }

  @EventHandler
  public void onPlaceStructure(BlockPlaceEvent event) {
    ItemStack item = event.getItemInHand();

    if (item != null && item.hasItemMeta()) {
      ItemMeta meta = item.getItemMeta();
      PersistentDataContainer pdc = meta.getPersistentDataContainer();
      NamespacedKey keyItem = new NamespacedKey("yourplugin", "item_type");

      if (pdc.has(keyItem, PersistentDataType.STRING)) {
        String itemType = pdc.get(keyItem, PersistentDataType.STRING);

        if (itemType.equals("structure_item")) {
          event.setCancelled(true); // Cancela la colocación del bloque

          NamespacedKey keyStructure = new NamespacedKey("yourplugin", "structure_data");
          if (pdc.has(keyStructure, PersistentDataType.STRING)) {
            String structureJson = pdc.get(keyStructure, PersistentDataType.STRING);
            Structure structure = StructureManager.fromJson(structureJson);

            Location locToBuild = event.getBlock().getLocation().clone();
            Player player = event.getPlayer();

            if (structure == null) {
              event.getPlayer().sendMessage(
                  Utils.useColors("&cError: No se pudo construir la estructura debido a datos inválidos."));
              return; // Sale del método si la estructura es nula
            }

            boolean wasBuilded = structure.build(locToBuild, player, regionManager);

            if (player.getGameMode() == GameMode.SURVIVAL && wasBuilded) {

              if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
              } else {
                player.getInventory().setItemInMainHand(null);
              }

              player.updateInventory();
            }

          }
        }

      }
    }
  }

}
