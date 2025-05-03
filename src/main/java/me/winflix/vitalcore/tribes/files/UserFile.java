package me.winflix.vitalcore.tribes.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.YmlFile;
import me.winflix.vitalcore.tribes.models.User;

public class UserFile extends YmlFile {
    private final User user;

    /**
     * @param plugin   La instancia principal del plugin
     * @param fileName Nombre de archivo (sin ".yml")
     * @param user     Objeto User a serializar la primera vez
     */
    public UserFile(VitalCore plugin, String fileName, User user) {
        // Usa la carpeta "users" en resources/users y dataFolder/users
        super(plugin, fileName, "users");
        this.user = user;
        create(); // crea directorios, copia defaults, reloadConfig() y onCreate()
    }

    /**
     * Hook tras recargar la configuración.
     * Si nunca existió el archivo, serializa aquí el User a YAML.
     */
    @Override
    protected void onCreate() {
        File file = getFile();
        if (!file.exists() && user != null) {
            file.getParentFile().mkdirs();

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8)) {
                mapper.writeValue(writer, user);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Error al escribir User en " + file.getPath(), e);
            }

            // Recarga para que getConfig() refleje lo que acabamos de escribir
            reloadConfig();
        }
    }

    /**
     * Deserializa el YAML de vuelta a un objeto User.
     * Requiere que tu clase User tenga constructor vacío y getters/setters.
     *
     * @return Nueva instancia de User con los datos del archivo,
     *         o la instancia original si ocurre un fallo.
     */
    public User loadUser() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(getFile(), User.class);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Error al leer User desde " + getFile().getPath(), e);
            return user;
        }
    }

    /**
     * Devuelve la instancia original proporcionada al crear este UserFile.
     */
    public User getUser() {
        return user;
    }
}