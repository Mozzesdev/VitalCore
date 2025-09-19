package me.winflix.vitalcore.skins;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.skins.events.JoinListener;
import me.winflix.vitalcore.skins.utils.SkinManager;
import me.winflix.vitalcore.skins.commands.Set;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;

public class Skins extends Manager {

    private final ArrayList<SubCommand> skinsCommands = new ArrayList<>();
    public SkinManager  skinManager = new SkinManager(plugin);

    public Skins(VitalCore plugin) {
        super(plugin);
    }

    public Skins initialize() {
        setupEvents();
        setupCommands();
        return this;
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new JoinListener(skinManager), plugin);
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand tribeCommand = plugin.getCommand("skins");
        CommandManager tribeCommandManager = new CommandManager(plugin, skinsCommands);
        assert tribeCommand != null;
        tribeCommand.setExecutor(tribeCommandManager);
    }

    public void registerCommands() {
        skinsCommands.add(new Set(skinManager));
    }

    @Override
    public void onDisable() {
    }
}
