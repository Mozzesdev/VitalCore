package me.winflix.vitalcore.addons.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.YmlFile;

public class ChestConfig extends YmlFile {
    private final Map<String, ChestSettings> settingsMap = new HashMap<>();

    // Valores por defecto
    private double defaultItemVelocityFactor;
    private double defaultVisualSpawnOffsetY;
    private int defaultMaxFlyTicks;
    private long defaultFlyTickInterval;
    private long defaultOpenToFlyDelay;
    private long defaultIdleDelay;

    public ChestConfig(VitalCore plugin) {
        super(plugin, "chests.yml", "addons");
        create();
    }

    @Override
    protected void onCreate() {
        FileConfiguration cfg = getConfig();
        // Leer defaults
        this.defaultItemVelocityFactor = cfg.getDouble("itemVelocityFactor", 0.3);
        this.defaultVisualSpawnOffsetY = cfg.getDouble("visualSpawnOffsetY", 1.2);
        this.defaultMaxFlyTicks = cfg.getInt("maxFlyTicks", 40);
        this.defaultFlyTickInterval = cfg.getLong("flyTickInterval", 2L);
        this.defaultOpenToFlyDelay = cfg.getLong("openToFlyDelay", 40L);
        this.defaultIdleDelay = cfg.getLong("idleDelay", 20L);

        ConfigurationSection root = cfg.getConfigurationSection("chests");
        if (root == null) {
            plugin.getLogger().warning("Sección 'chests' no encontrada en chests.yml");
            return;
        }
        settingsMap.clear();
        for (String chestId : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(chestId);
            if (sec == null)
                continue;

            // Parámetros, con fallback a defaults
            double ivf = sec.getDouble("itemVelocityFactor", defaultItemVelocityFactor);
            double vsY = sec.getDouble("visualSpawnOffsetY", defaultVisualSpawnOffsetY);
            int mft = sec.getInt("maxFlyTicks", defaultMaxFlyTicks);
            long fti = sec.getLong("flyTickInterval", defaultFlyTickInterval);
            long otd = sec.getLong("openToFlyDelay", defaultOpenToFlyDelay);
            long idl = sec.getLong("idleDelay", defaultIdleDelay);
            String modelId = sec.getString("modelId", "");

            // Animaciones
            Map<String, String> animations = new HashMap<>();
            ConfigurationSection animSec = sec.getConfigurationSection("animations");
            if (animSec != null) {
                for (String animKey : animSec.getKeys(false)) {
                    animations.put(animKey, animSec.getString(animKey, ""));
                }
            }

            ChestSettings s = new ChestSettings(ivf, vsY, mft, fti, otd, idl, modelId, animations);
            settingsMap.put(chestId, s);
        }
    }

    /**
     * Obtiene la configuración para un cofre específico.
     * 
     * @param chestId Identificador del cofre en el YAML.
     */
    public ChestSettings getSettings(String chestId) {
        return settingsMap.get(chestId);
    }

    /**
     * Recarga la configuración de cofres en caliente.
     */
    public void reload() {
        reloadConfig();
        onCreate();
    }

    public boolean hasChest(String chestId) {
        try {
            return getSettings(chestId) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getChestIds() {
    return new ArrayList<>(this.settingsMap.keySet());
}

    /**
     * Datos de configuración para un tipo de cofre.
     */
    public static class ChestSettings {
        private final double itemVelocityFactor;
        private final double visualSpawnOffsetY;
        private final int maxFlyTicks;
        private final long flyTickInterval;
        private final long openToFlyDelay;
        private final long idleDelay;
        private final String modelId;
        private final Map<String, String> animations;

        public ChestSettings(double itemVelocityFactor,
                double visualSpawnOffsetY,
                int maxFlyTicks,
                long flyTickInterval,
                long openToFlyDelay,
                long idleDelay,
                String modelId,
                Map<String, String> animations) {
            this.itemVelocityFactor = itemVelocityFactor;
            this.visualSpawnOffsetY = visualSpawnOffsetY;
            this.maxFlyTicks = maxFlyTicks;
            this.flyTickInterval = flyTickInterval;
            this.openToFlyDelay = openToFlyDelay;
            this.idleDelay = idleDelay;
            this.modelId = modelId;
            this.animations = animations;
        }

        public double getItemVelocityFactor() {
            return itemVelocityFactor;
        }

        public double getVisualSpawnOffsetY() {
            return visualSpawnOffsetY;
        }

        public int getMaxFlyTicks() {
            return maxFlyTicks;
        }

        public long getFlyTickInterval() {
            return flyTickInterval;
        }

        public long getOpenToFlyDelay() {
            return openToFlyDelay;
        }

        public long getIdleDelay() {
            return idleDelay;
        }

        public String getModelId() {
            return modelId;
        }

        public Map<String, String> getAnimations() {
            return animations;
        }
    }
}