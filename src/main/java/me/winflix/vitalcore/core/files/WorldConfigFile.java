package me.winflix.vitalcore.core.files;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.YmlFile;

public class WorldConfigFile extends YmlFile {

    public WorldConfigFile(VitalCore plugin) {
        super(plugin, "config", "world");
        create();
    }

    @Override
    protected void onCreate() {
    }
    
}
