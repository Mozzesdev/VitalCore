package me.winflix.vitalcore.core.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.MessagesFile;
import me.winflix.vitalcore.general.utils.Utils;

/**
 * Gestiona la carga de archivos de i18n y la asignación de locale por jugador.
 */
public class LocaleManager implements Listener {
    private static final String I18N_FOLDER = "i18n";
    private static final String[] SUPPORTED_LOCALES = { "es", "en" };
    private static final String DEFAULT_LOCALE = SUPPORTED_LOCALES[0];

    private final VitalCore plugin;
    private final Map<String, MessagesFile> messagesFiles = new ConcurrentHashMap<>();

    public LocaleManager(VitalCore plugin) {
        this.plugin = plugin;
        loadMessagesFiles();
    }

    /**
     * Carga/recarga todas las configuraciones de i18n.
     */
    public void loadMessagesFiles() {
        messagesFiles.clear();
        for (String locale : SUPPORTED_LOCALES) {
            MessagesFile mf = new MessagesFile(plugin, locale, I18N_FOLDER);
            messagesFiles.put(locale, mf);
        }
        Utils.ERROR_PREFIX = getDefaultMessagesFile().getConfig().getString("prefixes.error", "[Error]");
        Utils.INFO_PREFIX = getDefaultMessagesFile().getConfig().getString("prefixes.info", "[Info]");
        Utils.SUCCESS_PREFIX = getDefaultMessagesFile().getConfig().getString("prefixes.success", "[Success]");
    }

    /**
     * Recarga todos los archivos de mensajes.
     */
    public void reloadAllLocales() {
        messagesFiles.values().forEach(MessagesFile::reloadConfig);
        Utils.ERROR_PREFIX = getDefaultMessagesFile().getConfig().getString("prefixes.error", "[Error]");
        Utils.INFO_PREFIX = getDefaultMessagesFile().getConfig().getString("prefixes.info", "[Info]");
        Utils.SUCCESS_PREFIX = getDefaultMessagesFile().getConfig().getString("prefixes.success", "[Success]");
    }

    /**
     * Devuelve el locale basado en la configuración del cliente.
     * Si existe un i18n para ese locale, lo usa; si no, vuelve al por defecto.
     *
     * @param player el jugador del cual obtener el locale
     * @return código de locale soportado o el por defecto
     */
    public String getPlayerLocale(Player player) {
        String code = player.getLocale().substring(0, 2); // p.ej. "es_ES" o "en_US"
        return messagesFiles.containsKey(code) ? code : DEFAULT_LOCALE;
    }

    /**
     * Obtiene el archivo de mensajes para un locale dado.
     */
    public MessagesFile getMessagesFile(Player player) {
        String locale = getPlayerLocale(player);
        return messagesFiles.getOrDefault(locale, getDefaultMessagesFile());
    }

    /**
     * Archivo de mensajes por defecto.
     */
    public MessagesFile getDefaultMessagesFile() {
        return messagesFiles.get(DEFAULT_LOCALE);
    }

    /**
     * Prefijo de error basado en el locale del jugador.
     */
    public String getErrorPrefix(Player player) {
        FileConfiguration cfg = getMessagesFile(player).getConfig();
        return cfg.getString("prefixes.error", "[Error]");
    }

    /**
     * Prefijo de info basado en el locale del jugador.
     */
    public String getInfoPrefix(Player player) {
        FileConfiguration cfg = getMessagesFile(player).getConfig();
        return cfg.getString("prefixes.info", "[Info]");
    }

    /**
     * Prefijo de éxito basado en el locale del jugador.
     */
    public String getSuccessPrefix(Player player) {
        FileConfiguration cfg = getMessagesFile(player).getConfig();
        return cfg.getString("prefixes.success", "[Success]");
    }
}
