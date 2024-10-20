package me.winflix.vitalcore.core;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.core.commands.Reload;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;

public class Core extends Manager {
    private ArrayList<SubCommand> vCoreCommands = new ArrayList<SubCommand>();

    public Core(VitalCore plugin) {
        super(plugin);
    }

    public Core initialize() {
        setupEvents();
        setupCommands();
        return this;
    }

    public void setupEvents() {
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand vCommand = plugin.getCommand("vcore");
        CommandManager vCommandManager = new CommandManager(plugin, vCoreCommands, null);
        vCommand.setExecutor(vCommandManager);
    }

    public void registerCommands() {
        vCoreCommands.add(new Reload());
    }

}
