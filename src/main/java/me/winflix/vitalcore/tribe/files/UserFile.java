package me.winflix.vitalcore.tribe.files;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import me.winflix.vitalcore.general.files.YmlFile;
import me.winflix.vitalcore.tribe.models.User;

public class UserFile extends YmlFile {
    private User user;

    public UserFile(String path, String folder, User user) {
        super(path, folder);
        this.user = user;
    }

    @Override
    public void create() {
        File file = new File(getAllPath());

        if (!file.exists()) {
            if (user != null) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                try {
                    mapper.writeValue(file, user);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public User getUser() {
        return user;
    }

}
