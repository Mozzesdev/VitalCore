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

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

}
