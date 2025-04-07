package me.winflix.vitalcore.core.files;

import java.io.File;
import java.io.IOException;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.YmlFile;

public class MotdConfigFile extends YmlFile {

    public MotdConfigFile(VitalCore plugin) {
        super(plugin, "config.yml", "motd");
        create();
    }

    @Override
    public void create() {
        getConfig();

        File file = new File(getPath());

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            saveDefaultConfig();
        }

        reloadConfig();
    }
}