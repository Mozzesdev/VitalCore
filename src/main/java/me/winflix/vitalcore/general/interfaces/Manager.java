package me.winflix.vitalcore.general.interfaces;

import me.winflix.vitalcore.VitalCore;

public abstract class Manager {

    public static VitalCore plugin;

    public Manager(VitalCore plugin) {
        Manager.plugin = plugin;
    }

    public abstract Manager initialize();

    public abstract void setupCommands();

    public abstract void setupEvents();

    public abstract void registerCommands();

    public static VitalCore getPlugin() {
        return plugin;
    };

    public abstract void onDisable();

}
