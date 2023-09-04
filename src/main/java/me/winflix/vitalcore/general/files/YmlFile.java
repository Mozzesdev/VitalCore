package me.winflix.vitalcore.general.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.winflix.vitalcore.VitalCore;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public abstract class YmlFile {

    private final VitalCore plugin;
    private final String fileName;
    private final String folder;
    private File file;
    private FileConfiguration customFile;
    private final String path;

    public YmlFile(VitalCore plugin, String fileName, String folder) {
        this.plugin = plugin;
        this.fileName = fileName.toLowerCase().endsWith(".yml") ? fileName : fileName + ".yml";
        this.folder = folder;
        this.path = plugin.getDataFolder() + File.separator + this.folder + File.separator + this.fileName;
        this.file = null;
        this.customFile = null;
    }

    public abstract void create();

    public File getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFolder() {
        return folder;
    }

    public FileConfiguration getConfig() {
        if (customFile == null) {
            reloadConfig();
        }
        return customFile;
    }

    public void reloadConfig() {
        if (customFile == null) {
            file = new File(path);
        }
        customFile = YamlConfiguration.loadConfiguration(file);
        Reader defaultConfigStream;
        try {
            defaultConfigStream = new InputStreamReader(plugin.getResource(fileName), "UTF8");
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
                customFile.setDefaults(defaultConfig);
            }
        } catch (UnsupportedEncodingException ex) {
            ex.getMessage();
        } catch (NullPointerException e) {
            e.getMessage();
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
}
