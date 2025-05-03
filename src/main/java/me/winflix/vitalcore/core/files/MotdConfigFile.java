package me.winflix.vitalcore.core.files;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.YmlFile;

public class MotdConfigFile extends YmlFile {

    public MotdConfigFile(VitalCore plugin) {
        super(plugin, "config.yml", "motd");
        create();
    }

    @Override
    protected void onCreate() {
        VitalCore.Log.info("MOTD Config cargado correctamente");
    }

}