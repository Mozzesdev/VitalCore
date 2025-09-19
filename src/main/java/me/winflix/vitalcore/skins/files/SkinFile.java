package me.winflix.vitalcore.skins.files;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.YmlFile;
import me.winflix.vitalcore.skins.models.Skin;

public class SkinFile extends YmlFile {
    private Skin skin;

    /**
     * @param plugin   Referencia al plugin
     * @param fileName Nombre del archivo (se le añade .yml si no lo lleva)
     * @param skin     Objeto Skin a serializar
     */
    public SkinFile(VitalCore plugin, String fileName, Skin skin) {
        // Carpeta "skins", produce e.g. dataFolder/skins/<fileName>.yml
        super(plugin, fileName, "skins");
        this.skin = skin;
        create(); // copia recurso (si existe), recarga config y llama onCreate()
    }

    /**
     * Hook que se ejecuta justo tras crear/cargar el archivo.
     * Aquí volcaremos los datos del Skin a la config.
     */
    @Override
    protected void onCreate() {
        updateSkinData();
    }

    /**
     * Serializa el objeto Skin en el YamlConfiguration y guarda.
     */
    public void updateSkinData() {
        getConfig().set("skin.ownerId", skin.getOwnerId().toString());
        getConfig().set("skin.ownerName", skin.getOwnerName());
        getConfig().createSection("skin.property", skin.getPropertyAsMap());
        saveConfig();
    }

    /**
     * Carga desde el YML un nuevo Skin.
     */
    public Skin loadSkin() {
        ConfigurationSection propSection = getConfig().getConfigurationSection("skin.property");
        Map<String, String> propertyMap = propSection == null
                ? Map.of()
                : propSection.getValues(false)
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().toString()));

        UUID ownerId = UUID.fromString(getConfig().getString("skin.ownerId", UUID.randomUUID().toString()));
        String ownerName = getConfig().getString("skin.ownerName", "Unknown");

        return new Skin(ownerId, ownerName, Skin.propertyFromMap(propertyMap));
    }

}