package me.winflix.vitalcore.structures.files;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.files.JsonFile;

public class StructureFile extends JsonFile {

    public StructureFile(VitalCore plugin, String fileName, String folder) {
        super(plugin, fileName, folder);
        create();
    }

    @Override
    public void create() {
        reloadConfig();
    }

}
