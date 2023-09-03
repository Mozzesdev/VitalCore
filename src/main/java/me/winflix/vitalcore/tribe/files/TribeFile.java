package me.winflix.vitalcore.tribe.files;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import me.winflix.vitalcore.general.files.YmlFile;
import me.winflix.vitalcore.tribe.models.Tribe;

public class TribeFile extends YmlFile {

    private Tribe tribe;

    public TribeFile(String path, String folder, Tribe tribe) {
        super(path, folder);
        this.tribe = tribe;
    }

    @Override
    public void create() {
        File file = new File(getAllPath());

        if (!file.exists()) {
            if (tribe != null) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                try {
                    mapper.writeValue(file, tribe);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public Tribe getTribe() {
        return tribe;
    }
}