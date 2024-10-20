package me.winflix.vitalcore.general.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.structures.files.StructureFile;
import me.winflix.vitalcore.tribe.files.UserFile;
import me.winflix.vitalcore.tribe.files.TribeFile;

public class FileManager {

    private static final String TRIBES_FOLDER = "tribes";
    private static final String USERS_FOLDER = "users";
    private static final String STRUCTURES_FOLDER = "structures";
    private static final String MESSAGES_FILE_NAME = "en";
    private static final String I18N_FOLDER = "i18n";
    public List<TribeFile> tribeFiles = new ArrayList<>();
    public List<UserFile> usersFiles = new ArrayList<>();
    public FileConfiguration configFile;
    public MessagesFile messagesFile;
    public StructureFile structuresFile;
    public VitalCore plugin;

    public FileManager(VitalCore plugin) {
        this.plugin = plugin;
        configFile = plugin.getConfig();
        setupFolders();
        setupConfigFile();
        setupMessagesFiles();
        setupStructuresFiles();
        // setupTribesFiles();
        // setupUsersFiles();
        setPrefixes();
    }

    public void createFolder(String name) {
        File folder = new File(plugin.getDataFolder(), name);
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                plugin.getLogger().severe("No se pudo crear la carpeta: " + folder.getPath());
            }
        }
    }

    public void setupFolders() {
        createFolder(TRIBES_FOLDER);
        createFolder(USERS_FOLDER);
        createFolder(I18N_FOLDER);
        createFolder(STRUCTURES_FOLDER);
    }

    public void setupStructuresFiles() {
        structuresFile = new StructureFile(plugin, "structures", STRUCTURES_FOLDER);
    }

    public void setupTribesFiles() {
        tribeFiles = TribesCollection.getAllTribes().stream()
                .map(tribe -> {
                    return new TribeFile(plugin, tribe.getId(), TRIBES_FOLDER, tribe);
                })
                .collect(Collectors.toList());
    }

    public void setupUsersFiles() {
        usersFiles = UsersCollection.getAllUsers().stream()
                .map(player -> {
                    return new UserFile(plugin, player.getId(), USERS_FOLDER, player);
                })
                .collect(Collectors.toList());
    }

    public void setupMessagesFiles() {
        messagesFile = new MessagesFile(plugin, MESSAGES_FILE_NAME, I18N_FOLDER);
    }

    public void setupConfigFile() {
        plugin.saveDefaultConfig();
        configFile.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public MessagesFile getMessagesFile() {
        return messagesFile;
    }

    public FileConfiguration getConfigFile() {
        return configFile;
    }

    public List<TribeFile> getTribesFiles() {
        return tribeFiles;
    }

    public StructureFile getStructuresFile() {
        return structuresFile;
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
        messagesFile.reloadConfig();
        plugin.reloadConfig();
        structuresFile.reloadConfig();
        // setupUsersFiles();
        // setupTribesFiles();
        setPrefixes();
    }

    public void setPrefixes() {
        FileConfiguration messagesConfig = getMessagesFile().getConfig();
        Utils.ERROR_PREFIX = messagesConfig.getString("prefixes.error");
        Utils.INFO_PREFIX = messagesConfig.getString("prefixes.info");
        Utils.SUCCESS_PREFIX = messagesConfig.getString("prefixes.success");
    }
}
