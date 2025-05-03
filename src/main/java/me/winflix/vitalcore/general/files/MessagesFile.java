package me.winflix.vitalcore.general.files;

import me.winflix.vitalcore.VitalCore;

public class MessagesFile extends YmlFile {

    public MessagesFile(VitalCore plugin, String fileName, String folder) {
        super(plugin, fileName, folder);
        create();
    }

    @Override
    protected void onCreate() {
    }


}
