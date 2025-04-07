package me.winflix.vitalcore.skins.files;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.YmlFile;
import me.winflix.vitalcore.skins.models.Skin;

public class SkinFile extends YmlFile {
    private Skin skin;

    public SkinFile(VitalCore plugin, String fileName, String folder, Skin skin) {
        super(plugin, fileName, folder);
        this.skin = skin;
        create();
    }

    @Override
    public void create() {
        File file = new File(getPath());

        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
                reloadConfig();
                updateSkinData();
            } catch (IOException e) {
                VitalCore.Log.severe("No se pudo crear el archivo: " + file.getPath());
                e.printStackTrace();
            }
        }

    }

    public void updateSkinData() {
        // Serializar el Skin al YamlConfiguration
        getConfig().set("skin.ownerId", skin.getOwnerId().toString());
        getConfig().set("skin.ownerName", skin.getOwnerName());
        getConfig().set("skin.property", skin.getPropertyAsMap());

        saveConfig();
    }

    public Skin loadSkin() {
        Map<String, String> propertyMap = getConfig().getConfigurationSection("skin.property")
                .getValues(false)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toString()));

        return new Skin(
                UUID.fromString(getConfig().getString("skin.ownerId")),
                getConfig().getString("skin.ownerName"),
                Skin.propertyFromMap(propertyMap));
    }
}