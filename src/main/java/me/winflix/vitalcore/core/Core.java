package me.winflix.vitalcore.core;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.core.commands.Reload;
import me.winflix.vitalcore.core.commands.Tpa;
import me.winflix.vitalcore.core.commands.TpaAccept;
import me.winflix.vitalcore.core.commands.TpaDeny;
import me.winflix.vitalcore.core.events.MOTDListener;
import me.winflix.vitalcore.core.managers.MOTDManager;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;

public class Core extends Manager {
    private ArrayList<SubCommand> vCoreCommands = new ArrayList<SubCommand>();
    private final MOTDManager motdManager;

    public Core(VitalCore plugin) {
        super(plugin);
        this.motdManager = new MOTDManager();
    }

    public Core initialize() {
        setupEvents();
        setupCommands();
        return this;
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new MOTDListener(motdManager), plugin);
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand vCommand = plugin.getCommand("vcore");
        CommandManager vCommandManager = new CommandManager(plugin, vCoreCommands);
        vCommand.setExecutor(vCommandManager);
        setupTpaCommands();
    }

    public void registerCommands() {
        vCoreCommands.add(new Reload());
    }

    private void setupTpaCommands() {
        PluginCommand tpaCommand = plugin.getCommand("tpa");
        CommandManager tpaCM = new CommandManager(plugin, new ArrayList<>(), new Tpa());
        tpaCommand.setExecutor(tpaCM);

        PluginCommand tpAcceptCommand = plugin.getCommand("tpaccept");
        CommandManager tpAcceptCM = new CommandManager(plugin, new ArrayList<>(), new TpaAccept());
        tpAcceptCommand.setExecutor(tpAcceptCM);

        PluginCommand tpaDenyCommand = plugin.getCommand("tpadeny");
        CommandManager tpaDenyCM = new CommandManager(plugin, new ArrayList<>(), new TpaDeny());
        tpaDenyCommand.setExecutor(tpaDenyCM);
    }

}
