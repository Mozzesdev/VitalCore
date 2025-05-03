package me.winflix.vitalcore.general.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.winflix.vitalcore.VitalCore;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Base class para gestionar archivos YAML con recursos por defecto en la
 * carpeta resources.
 */
public abstract class YmlFile {

    protected final VitalCore plugin;
    protected final String fileName;
    protected final String folder;
    protected final File file;
    protected FileConfiguration config;
    protected final String resourcePath;

    /**
     * Construye un manejador de configuración.
     *
     * @param plugin   Referencia a la clase principal del plugin.
     * @param fileName Nombre del archivo (con o sin extensión .yml).
     * @param folder   Carpeta dentro de resources y dataFolder (usar forward
     *                 slashes).
     */
    public YmlFile(VitalCore plugin, String fileName, String folder) {
        this.plugin = plugin;
        String name = fileName.toLowerCase().endsWith(".yml") ? fileName : fileName + ".yml";
        this.fileName = name;
        this.folder = folder.replace("\\", "/");
        this.resourcePath = this.folder + "/" + name;
        this.file = new File(plugin.getDataFolder(), this.folder + File.separator + name);
    }

    /**
     * Inicializa el archivo: guarda el recurso por defecto si no existe, recarga y
     * ejecuta onCreate().
     */
    public final void create() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            saveDefaultConfig();
        }
        reloadConfig();
        onCreate();
    }

    /**
     * Sobreescibir para comportamiento personalizado tras la carga inicial.
     */
    protected abstract void onCreate();

    /**
     * Obtiene la configuración cargada, recargándola si es necesario.
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public File getFile() {
        return file;
    }

    /**
     * Recarga el archivo desde disco y aplica valores por defecto desde resources.
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file);

        try (InputStream defStream = plugin.getResource(resourcePath)) {
            if (defStream != null) {
                try (Reader reader = new InputStreamReader(defStream, StandardCharsets.UTF_8)) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
                    config.setDefaults(defConfig);
                    config.options().copyDefaults(true);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar valores por defecto de " + resourcePath, e);
        }
    }

    /**
     * Guarda la configuración actual en disco.
     */
    public void saveConfig() {
        if (config == null) {
            plugin.getLogger().warning("Imposible guardar " + fileName + ": configuración no cargada.");
            return;
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar " + file.getPath(), e);
        }
    }

    /**
     * Copia el recurso por defecto desde el JAR a la carpeta de datos si existe.
     */
    public void saveDefaultConfig() {
        try {
            plugin.saveResource(resourcePath, false);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Recurso por defecto no encontrado: " + resourcePath);
        }
    }
}
