package me.winflix.vitalcore.skins;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.skins.events.JoinListener;

import java.util.ArrayList;

public class Skins {

    private final ArrayList<SubCommand> skinsCommands = new ArrayList<>();
    VitalCore plugin = VitalCore.getPlugin();

    public void initialize() {
        setupEvents();
        setupCommands();
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new JoinListener(), plugin);
    }

    public void setupCommands() {
        registerCommands();
    }

    public void registerCommands() {

    }
}
