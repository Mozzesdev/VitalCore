package me.winflix.vitalcore.general.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import me.winflix.vitalcore.VitalCore;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class JsonFile {

    private final VitalCore plugin;
    private final String fileName;
    private final String folder;
    private File file;
    private Gson gson;
    protected Object customFile;
    private final String path;

    public JsonFile(VitalCore plugin, String fileName, String folder) {
        this.plugin = plugin;
        this.fileName = fileName.toLowerCase().endsWith(".json") ? fileName : fileName + ".json";
        this.folder = folder;
        this.path = plugin.getDataFolder() + File.separator + this.folder + File.separator + this.fileName;
        this.file = null;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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

    public Object getConfig() {
        if (customFile == null) {
            reloadConfig();
        }
        return customFile;
    }

    public void reloadConfig() {
        file = new File(path); // Asegúrate de que se inicializa correctamente
        if (!file.exists()) {
            saveDefaultConfig(); // Cargar configuración predeterminada si el archivo no existe
        } else {
            try (FileReader reader = new FileReader(file)) {
                customFile = gson.fromJson(reader, Object.class);
            } catch (IOException | JsonSyntaxException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(customFile, writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (!file.exists() ) {
            InputStream defaultConfigStream = plugin.getResource(fileName);
            if (defaultConfigStream != null) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(defaultConfigStream,
                        StandardCharsets.UTF_8);
                        FileWriter writer = new FileWriter(file)) {
                    char[] buffer = new char[1024];
                    int bytesRead;
                    while ((bytesRead = inputStreamReader.read(buffer)) != -1) {
                        writer.write(buffer, 0, bytesRead);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
