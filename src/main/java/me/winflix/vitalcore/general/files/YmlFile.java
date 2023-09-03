package me.winflix.vitalcore.general.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.winflix.vitalcore.VitalCore;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public abstract class YmlFile {

    private final VitalCore plugin = VitalCore.getPlugin();
    private final String path;
    private final String folderpath;
    private File file;
    private FileConfiguration customFile;
    private final String allPath;

    public YmlFile(String fileName, String folder) {
        this.path = fileName.toLowerCase().endsWith(".yml") ? fileName : fileName + ".yml";
        this.folderpath = folder;
        this.allPath = plugin.getDataFolder() + File.separator + folderpath;
        create();
    }

    public abstract void create();

    public FileConfiguration getConfig() {
        if (customFile == null) {
            reloadConfig();
        }
        return customFile;
    }

    public File getFile() {
        return file;
    }

    public String getAllPath() {
        return allPath + File.separator + path;
    }

    public void reloadConfig() {
        if (customFile == null) {
            file = new File(allPath, path);
        }
        customFile = YamlConfiguration.loadConfiguration(file);

        try (Reader defaultConfigStream = new InputStreamReader(plugin.getResource(path), StandardCharsets.UTF_8)) {
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
                customFile.setDefaults(defaultConfig);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            customFile.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }
    }

    public String getPath() {
        return path;
    }

    public String getFolderpath() {
        return folderpath;
    }
}
