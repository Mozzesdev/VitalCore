package me.winflix.vitalcore.tribes.files;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.YmlFile;
import me.winflix.vitalcore.tribes.models.Tribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;


public class TribeFile extends YmlFile {
    private final Tribe tribe;

    /**
     * @param plugin   Instancia del plugin
     * @param fileName Nombre de archivo (añade .yml automáticamente si falta)
     * @param tribe    Objeto Tribe a serializar la primera vez
     */
    public TribeFile(VitalCore plugin, String fileName, Tribe tribe) {
        // Usa la carpeta "tribes" dentro de resources/tribes y dataFolder/tribes
        super(plugin, fileName, "tribes");
        this.tribe = tribe;
        create();  // maneja directorios, defaults, reloadConfig() y onCreate()
    }

    /**
     * Hook que se ejecuta justo después de recargar la configuración.
     * Si nunca existió el archivo, volcamos aquí el objeto Tribe.
     */
    @Override
    protected void onCreate() {
        File file = getFile();
        if (!file.exists() && tribe != null) {
            // Asegura que la carpeta existe
            file.getParentFile().mkdirs();

            // Jackson + YAML para escribir todos los campos de Tribe:
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8)) {
                mapper.writeValue(writer, tribe);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE,
                    "Error al escribir Tribe en " + file.getPath(), e);
            }

            // Recarga el FileConfiguration para reflejar lo escrito
            reloadConfig();
        }
    }

    /**
     * Deserializa el YAML de vuelta a un objeto Tribe.
     * Jackson aprovecha el constructor vacío y los getters/setters
     * de tu clase para mapear
     *
     * @return Una nueva instancia de Tribe con los datos del archivo;
     *         o la instancia original si hay algún error.
     */
    public Tribe loadTribe() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(getFile(), Tribe.class);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE,
                "Error al leer Tribe desde " + getFile().getPath(), e);
            // En caso de fallo devolvemos la instancia que ya teníamos
            return tribe;
        }
    }

    /**
     * Si en algún punto necesitas el objeto original (p. ej. antes de escribirlo),
     * éste es el getter que lo retorna.
     */
    public Tribe getTribe() {
        return tribe;
    }
}
