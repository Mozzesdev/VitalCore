package me.winflix.vitalcore.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.models.PlayerModel;

public class PlayerFile {
    private VitalCore plugin;
    private String path;
    private String folderpath;
    private File file;
    private FileConfiguration customFile;
    private PlayerModel player;
    private String allPath;

    public PlayerFile(VitalCore plugin, String path, String folder, PlayerModel player) {
        this.plugin = plugin;
        this.path = path;
        this.folderpath = folder;
        this.file = null;
        this.customFile = null;
        this.player = player;
        allPath = plugin.getDataFolder() + File.separator + folderpath;
        create();
    }

    public void create() {
        file = new File(allPath, path);
        if (player != null) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            try {
                mapper.writeValue(file, player);
            } catch (IOException e) {
                e.printStackTrace();
            }
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getConfig() {
        if (customFile == null) {
            reloadConfig();
        }
        return customFile;
    }

    public void setPlayer(PlayerModel newPlayer) {
        this.player = newPlayer;
    }

    public String getAllPath() {
        return allPath + File.separator + path;
    }

    public void reloadConfig() {
        if (customFile == null) {
            file = new File(plugin.getDataFolder() + File.separator + folderpath, path);
        }
        customFile = YamlConfiguration.loadConfiguration(file);
        Reader defaultConfigStream;
        try {
            defaultConfigStream = new InputStreamReader(plugin.getResource(path), "UTF8");
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
        if (file == null) {
            file = new File(plugin.getDataFolder() + File.separator + folderpath, path);
        }
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
