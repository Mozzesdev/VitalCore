package me.winflix.vitalcore.core;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.core.commands.Msg;
import me.winflix.vitalcore.core.commands.Reload;
import me.winflix.vitalcore.core.commands.SetSpawn;
import me.winflix.vitalcore.core.commands.Spawn;
import me.winflix.vitalcore.core.commands.Tpa;
import me.winflix.vitalcore.core.commands.TpaAccept;
import me.winflix.vitalcore.core.commands.TpaDeny;
import me.winflix.vitalcore.core.events.AntibotListener;
import me.winflix.vitalcore.core.events.ChatListener;
import me.winflix.vitalcore.core.events.MOTDListener;
import me.winflix.vitalcore.core.managers.ChatManager;
import me.winflix.vitalcore.core.managers.MOTDManager;
import me.winflix.vitalcore.core.managers.WorldManager;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;

public class Core extends Manager {
    private ArrayList<SubCommand> vCoreCommands = new ArrayList<SubCommand>();
    public static final ChatManager chatManager = new ChatManager();
    public static WorldManager worldManager = new WorldManager();
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
        plugin.getServer().getPluginManager().registerEvents(new AntibotListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ChatListener(), plugin);
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand vCommand = plugin.getCommand("vcore");
        CommandManager vCommandManager = new CommandManager(plugin, vCoreCommands);
        vCommand.setExecutor(vCommandManager);
        setupTpaCommands();
        setupMsgCommand();
        setupSpawnCommands();
    }

    public void registerCommands() {
        vCoreCommands.add(new Reload());
    }

    private void setupTpaCommands() {
        CommandManager tpaCM = new CommandManager(plugin, new ArrayList<>(), new Tpa());
        plugin.getCommand("tpa").setExecutor(tpaCM);

        CommandManager tpAcceptCM = new CommandManager(plugin, new ArrayList<>(), new TpaAccept());
        plugin.getCommand("tpaccept").setExecutor(tpAcceptCM);

        CommandManager tpaDenyCM = new CommandManager(plugin, new ArrayList<>(), new TpaDeny());
        plugin.getCommand("tpadeny").setExecutor(tpaDenyCM);
    }

    private void setupMsgCommand() {
        CommandManager msgC = new CommandManager(plugin, new ArrayList<>(), new Msg());
        plugin.getCommand("msg").setExecutor(msgC);
    }

    private void setupSpawnCommands() {
        CommandManager spawnC = new CommandManager(plugin, new ArrayList<>(), new Spawn());
        plugin.getCommand("spawn").setExecutor(spawnC);

        CommandManager setSpawnC = new CommandManager(plugin, new ArrayList<>(), new SetSpawn());
        plugin.getCommand("setspawn").setExecutor(setSpawnC);
    }

    @Override
    public void onDisable() {
    }

}
