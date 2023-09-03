package me.winflix.vitalcore.general.utils;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import me.winflix.vitalcore.VitalCore;

public class PlayerUtils {

    @SuppressWarnings("deprecation")
    public static ItemStack getPlayerSkull(String playerName, UUID uuid, String... lore) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

        if (skullMeta != null) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            skullMeta.setDisplayName(Utils.useColors("&r" + playerName));

            if (lore != null && lore.length > 0) {
                skullMeta.setLore(Arrays.asList(lore));
            }

            NamespacedKey uuidKey = new NamespacedKey(VitalCore.getPlugin(), "uuid");
            skullMeta.getPersistentDataContainer().set(uuidKey, PersistentDataType.STRING, uuid.toString());

            itemStack.setItemMeta(skullMeta);
        }

        return itemStack;
    }

    public static File getPlayerFile(String uuid) {
        File dataFolder = new File(
                VitalCore.getPlugin(VitalCore.class).getDataFolder() + File.separator + "players-states");
        for (File file : dataFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                if (file.getName().equalsIgnoreCase(uuid + ".yml")) {
                    return file;
                }
            }
        }
        return null;
    }
}
