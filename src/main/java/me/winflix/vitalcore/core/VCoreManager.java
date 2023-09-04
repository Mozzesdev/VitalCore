package me.winflix.vitalcore.core;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.core.commands.Reload;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;

public class VCoreManager {
    VitalCore plugin = VitalCore.getPlugin();
    private ArrayList<SubCommand> vCoreCommands = new ArrayList<SubCommand>();

    public void initialize() {
        setupEvents();
        setupCommands();
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
