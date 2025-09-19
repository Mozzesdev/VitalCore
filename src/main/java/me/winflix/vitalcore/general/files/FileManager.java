package me.winflix.vitalcore.general.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.config.ChestConfig;
import me.winflix.vitalcore.core.files.MotdConfigFile;
import me.winflix.vitalcore.core.files.WorldConfigFile;
import me.winflix.vitalcore.core.managers.LocaleManager;
import me.winflix.vitalcore.general.database.collections.tribe.TribesDAO;
import me.winflix.vitalcore.general.database.collections.tribe.UsersDAO;
import me.winflix.vitalcore.tribes.files.TribeFile;
import me.winflix.vitalcore.tribes.files.UserFile;

public class FileManager {
    public List<TribeFile> tribeFiles = new ArrayList<>();
    public List<UserFile> usersFiles = new ArrayList<>();
    public FileConfiguration configFile;
    private final LocaleManager localeManager;
    public MotdConfigFile motdConfigFile;
    public WorldConfigFile worldConfigFile;
    public ChestConfig chestsConfig;
    public VitalCore plugin;

    public FileManager(VitalCore plugin) {
        this.plugin = plugin;
        configFile = plugin.getConfig();
        this.localeManager = new LocaleManager(plugin);
        setupConfigFile();
        setupMotdFile();
        setupWorldConfig();
        setupAddonsFiles();
    }

    public void createFolder(String name) {
        File folder = new File(plugin.getDataFolder(), name);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                plugin.getLogger().severe("No se pudo crear la carpeta: " + folder.getPath());
            }
        }
    }

    public void setupTribesFiles() {
        tribeFiles = TribesDAO.getAllTribes().stream()
                .map(tribe -> new TribeFile(plugin, tribe.getId().toString(), tribe))
                .collect(Collectors.toList());
    }

    public void setupUsersFiles() {
        usersFiles = UsersDAO.getAllUsers().stream()
                .map(player -> new UserFile(plugin, player.getId().toString(), player))
                .collect(Collectors.toList());
    }

    public void setupMotdFile() {
        motdConfigFile = new MotdConfigFile(plugin);
    }

    public void setupWorldConfig() {
        worldConfigFile = new WorldConfigFile(plugin);
    }

    private void setupAddonsFiles(){
        chestsConfig = new ChestConfig(plugin);
    } 

    public ChestConfig getChestsConfig() {
        return chestsConfig;
    }

    public FileConfiguration getWorldsConfig() {
        return worldConfigFile.getConfig();
    }

    public void saveWorldsConfig() {
        worldConfigFile.saveConfig();
    }

    public void setupConfigFile() {
        plugin.saveDefaultConfig();
        configFile.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public MessagesFile getMessagesFile(Player player) {
        return localeManager.getMessagesFile(player);
    }

    public MotdConfigFile getMotdFile() {
        return motdConfigFile;
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

    public List<UserFile> getUsersFiles() {
        return usersFiles;
    }

    public UserFile getUserFile(String id) {
        return getUsersFiles().stream()
                .filter(player -> player.getConfig().getString("id").equals(id))
                .findFirst()
                .orElse(null);
    }

    public void reloadAllFiles() {
        localeManager.reloadAllLocales();
        motdConfigFile.reloadConfig();
        chestsConfig.reloadConfig();
        plugin.reloadConfig();
    }

}