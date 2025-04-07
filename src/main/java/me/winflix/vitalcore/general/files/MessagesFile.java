package me.winflix.vitalcore.general.files;

import java.io.File;
import java.io.IOException;

import me.winflix.vitalcore.VitalCore;

public class MessagesFile extends YmlFile {

    public MessagesFile(VitalCore plugin, String fileName, String folder) {
        super(plugin, fileName, folder);
        create();
    }

    @Override
    public void create() {
        File file = new File(getPath());

        // Crear directorios padres si no existen
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // Asegura que el directorio i18n exista
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                VitalCore.Log.severe("No se pudo crear el archivo: " + file.getPath());
                e.printStackTrace();
            }
        }

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

}
