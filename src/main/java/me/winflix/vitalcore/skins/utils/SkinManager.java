package me.winflix.vitalcore.skins.utils;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import com.mojang.authlib.properties.Property;

import me.winflix.vitalcore.skins.files.SkinFile;
import me.winflix.vitalcore.skins.models.Skin;

public class SkinManager {

    private final VitalCore plugin;
    private final Map<UUID, Skin> skinCache = new ConcurrentHashMap<>();

    public SkinManager(VitalCore plugin) {
        this.plugin = plugin;
    }

    public Skin getSkin(Player player) {
        UUID uuid = player.getUniqueId();
        if (skinCache.containsKey(uuid)) {
            return skinCache.get(uuid);
        } else {
            Skin skin = loadSkinFromFile(uuid);
            if (skin != null) {
                skinCache.put(uuid, skin);
                return skin;
            }
        }
        return null;
    }

    public void updateSkin(Player player, Skin skin) {
        UUID uuid = player.getUniqueId();
        skinCache.put(uuid, skin);
        saveSkinToFile(uuid, skin);
    }

    public void removeSkin(Player player) {
        UUID uuid = player.getUniqueId();
        skinCache.remove(uuid);
    }

    private Skin loadSkinFromFile(UUID uuid) {
        File file = new File(plugin.getDataFolder(), "skins/" + uuid + ".yml");
        
        if (file.exists()) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String skinName = config.getString("skin.ownerName");
                String skinId = config.getString("skin.ownerId");
                String value = config.getString("skin.property.value");
                String signature = config.getString("skin.property.signature");
                String proName = config.getString("skin.property.name");
                if (value != null && signature != null) {
                    Property property = new Property(proName, value, signature);
                    return new Skin(UUID.fromString(skinId), skinName, property);
                }
            } catch (Exception e) {
                VitalCore.Log.log(Level.SEVERE, "Failed to load skin for UUID: " + uuid);
            }
        }
        return null;
    }

    private void saveSkinToFile(UUID uuid, Skin skin) {
        if (uuid == null || skin == null) {
            throw new IllegalArgumentException("UUID o Skin no pueden ser null");
        }
        String fileName = uuid.toString() + ".yml";
        SkinFile skinFile = new SkinFile(plugin, fileName, skin);
        skinFile.updateSkinData();
    }
}
