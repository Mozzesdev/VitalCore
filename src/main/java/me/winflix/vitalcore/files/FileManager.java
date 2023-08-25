package me.winflix.vitalcore.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.database.collections.TribeCollection;
import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.utils.Utils;

public class FileManager {

    private VitalCore plugin;
    public YmlFileManager messagesFile;
    public FileConfiguration configFile;
    public List<TribeFile> tribeFiles = new ArrayList<>();
    public List<PlayerFile> playerFiles = new ArrayList<>();

    public FileManager(VitalCore plugin) {
        this.plugin = plugin;
        configFile = plugin.getConfig();
        setupFolders();
        setupConfigFile();
        messagesFile = setupMessagesFiles();
        tribeFiles = setupTribesFiles();
        playerFiles = setupPlayerFiles();
        setPrefixes();
    }

    public void createFolder(String name) {
        File folder = new File(plugin.getDataFolder(), name);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public void setupFolders() {
        createFolder("tribes");
        createFolder("users");
    }

    public List<TribeFile> setupTribesFiles() {
        return TribeCollection.getAllTribes().stream()
                .map(tribe -> {
                    return new TribeFile(plugin, tribe.getId() + ".yml", "tribes", tribe);
                })
                .collect(Collectors.toList());
    }

    public List<PlayerFile> setupPlayerFiles() {
        return UserCollection.getAllPlayers().stream()
                .map(player -> {
                    return new PlayerFile(plugin, player.getId() + ".yml", "users", player);
                })
                .collect(Collectors.toList());
    }

    public YmlFileManager setupMessagesFiles() {
        return new YmlFileManager(plugin, "messages.yml");
    }

    public void setupConfigFile() {
        plugin.saveDefaultConfig();
        configFile.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public YmlFileManager getMessagesFile() {
        return messagesFile;
    }

    public FileConfiguration getConfigFile() {
        return configFile;
    }

    public List<TribeFile> getTribesFiles() {
        return tribeFiles;
    }

    public TribeFile getTribeFile(String id) {
        return getTribesFiles().stream()
                .filter(tribe -> tribe.getConfig().getString("id").equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<PlayerFile> getPlayersFiles() {
        return playerFiles;
    }

    public PlayerFile getPlayerFile(String id) {
        return getPlayersFiles().stream()
                .filter(player -> player.getConfig().getString("id").equals(id))
                .findFirst()
                .orElse(null);
    }

    public void reloadAllFiles() {
        messagesFile.reloadConfig();
        plugin.reloadConfig();
        setupPlayerFiles();
        setupTribesFiles();
        setPrefixes();
    }

    public void setPrefixes() {
        Utils.ERROR_PREFIX = messagesFile.getConfig().getString("prefixes.error");
        Utils.INFO_PREFIX = messagesFile.getConfig().getString("prefixes.info");
        Utils.SUCCESS_PREFIX = messagesFile.getConfig().getString("prefixes.success");
    }
}
