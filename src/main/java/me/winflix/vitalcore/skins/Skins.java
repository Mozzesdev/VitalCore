package me.winflix.vitalcore.skins;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.skins.events.JoinListener;

import java.util.ArrayList;

public class Skins extends Manager {

    @SuppressWarnings("unused")
    private final ArrayList<SubCommand> skinsCommands = new ArrayList<>();

    public Skins(VitalCore plugin) {
        super(plugin);
    }

    public Skins initialize() {
        setupEvents();
        setupCommands();
        return this;
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
