package me.winflix.vitalcore.core;

import java.util.ArrayList;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.core.commands.Msg;
import me.winflix.vitalcore.core.commands.Reload;
import me.winflix.vitalcore.core.commands.SetSpawn;
import me.winflix.vitalcore.core.commands.Spawn;
import me.winflix.vitalcore.core.commands.Tpa;
import me.winflix.vitalcore.core.commands.TpaAccept;
import me.winflix.vitalcore.core.commands.TpaDeny;
import me.winflix.vitalcore.core.commands.TpaHere;
import me.winflix.vitalcore.core.commands.TpToggle;
import me.winflix.vitalcore.core.commands.Back;
import me.winflix.vitalcore.core.commands.God;
import me.winflix.vitalcore.core.commands.Fly;
import me.winflix.vitalcore.core.commands.Afk;
import me.winflix.vitalcore.core.commands.AfkList;
import me.winflix.vitalcore.core.commands.Repair;
import me.winflix.vitalcore.core.commands.Enderchest;
import me.winflix.vitalcore.core.commands.Workbench;
import me.winflix.vitalcore.core.commands.Anvil;
import me.winflix.vitalcore.core.events.AntibotListener;
import me.winflix.vitalcore.core.events.ChatListener;
import me.winflix.vitalcore.core.events.MOTDListener;
import me.winflix.vitalcore.core.events.PlayerListener;
import me.winflix.vitalcore.core.events.AfkListener;
import me.winflix.vitalcore.core.events.CommandTabCompleteListener;
import me.winflix.vitalcore.core.managers.ChatManager;
import me.winflix.vitalcore.core.managers.MOTDManager;
import me.winflix.vitalcore.core.managers.WorldManager;
import me.winflix.vitalcore.core.managers.AfkManager;
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
        AfkManager.startInactivityChecker(plugin);
        
        return this;
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new MOTDListener(motdManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new AntibotListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ChatListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new AfkListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new CommandTabCompleteListener(), plugin);
    }

    public void setupCommands() {
        registerCommands();
        setupVcoreCommands();
        setupTpaCommands();
        setupTpaHereCommand();
        setupTpToggleCommand();
        setupBackCommand();
        setupHealCommand();
        setupFeedCommand();
        setupGodCommand();
        setupFlyCommand();
        setupAfkCommand();
        setupAfkListCommand();
        setupRepairCommand();
        setupMsgCommand();
        setupSpawnCommands();
        setupEnderchestCommand();
        setupWorkbenchCommand();
        setupAnvilCommand();
    }

    public void registerCommands() {
        vCoreCommands.add(new Reload());
    }

    private void setupVcoreCommands() {
        CommandManager vCommandManager = new CommandManager(plugin, vCoreCommands);
        plugin.getCommand("vcore").setExecutor(vCommandManager);
        plugin.getCommand("vcore").setTabCompleter(vCommandManager);
    }

    private void setupTpaCommands() {
        CommandManager tpaCM = new CommandManager(plugin, new ArrayList<>(), new Tpa());
        plugin.getCommand("tpa").setExecutor(tpaCM);
        plugin.getCommand("tpa").setTabCompleter(tpaCM);

        CommandManager tpAcceptCM = new CommandManager(plugin, new ArrayList<>(), new TpaAccept());
        plugin.getCommand("tpaccept").setExecutor(tpAcceptCM);
        plugin.getCommand("tpaccept").setTabCompleter(tpAcceptCM);

        CommandManager tpaDenyCM = new CommandManager(plugin, new ArrayList<>(), new TpaDeny());
        plugin.getCommand("tpadeny").setExecutor(tpaDenyCM);
        plugin.getCommand("tpadeny").setTabCompleter(tpaDenyCM);
    }

    private void setupMsgCommand() {
        CommandManager msgC = new CommandManager(plugin, new ArrayList<>(), new Msg());
        plugin.getCommand("msg").setExecutor(msgC);
        plugin.getCommand("msg").setTabCompleter(msgC);
    }

    private void setupSpawnCommands() {
        CommandManager spawnC = new CommandManager(plugin, new ArrayList<>(), new Spawn());
        plugin.getCommand("spawn").setExecutor(spawnC);
        plugin.getCommand("spawn").setTabCompleter(spawnC);

        CommandManager setSpawnC = new CommandManager(plugin, new ArrayList<>(), new SetSpawn());
        plugin.getCommand("setspawn").setExecutor(setSpawnC);
        plugin.getCommand("setspawn").setTabCompleter(setSpawnC);
    }

    private void setupTpaHereCommand() {
        CommandManager tpaHereC = new CommandManager(plugin, new ArrayList<>(), new TpaHere());
        plugin.getCommand("tpahere").setExecutor(tpaHereC);
        plugin.getCommand("tpahere").setTabCompleter(tpaHereC);
    }

    private void setupTpToggleCommand() {
        CommandManager tpToggleC = new CommandManager(plugin, new ArrayList<>(), new TpToggle());
        plugin.getCommand("tptoggle").setExecutor(tpToggleC);
        plugin.getCommand("tptoggle").setTabCompleter(tpToggleC);
    }

    private void setupBackCommand() {
        CommandManager backC = new CommandManager(plugin, new ArrayList<>(), new Back());
        plugin.getCommand("back").setExecutor(backC);
        plugin.getCommand("back").setTabCompleter(backC);
    }

    private void setupHealCommand() {
        CommandManager healC = new CommandManager(plugin, new ArrayList<>(), new me.winflix.vitalcore.core.commands.Heal());
        plugin.getCommand("heal").setExecutor(healC);
        plugin.getCommand("heal").setTabCompleter(healC);
    }

    private void setupFeedCommand() {
        CommandManager feedC = new CommandManager(plugin, new ArrayList<>(), new me.winflix.vitalcore.core.commands.Feed());
        plugin.getCommand("feed").setExecutor(feedC);
        plugin.getCommand("feed").setTabCompleter(feedC);
    }

    private void setupGodCommand() {
        CommandManager godC = new CommandManager(plugin, new ArrayList<>(), new God());
        plugin.getCommand("god").setExecutor(godC);
        plugin.getCommand("god").setTabCompleter(godC);
    }

    private void setupFlyCommand() {
        CommandManager flyC = new CommandManager(plugin, new ArrayList<>(), new Fly());
        plugin.getCommand("fly").setExecutor(flyC);
        plugin.getCommand("fly").setTabCompleter(flyC);
    }

    private void setupAfkCommand() {
        CommandManager afkC = new CommandManager(plugin, new ArrayList<>(), new Afk());
        plugin.getCommand("afk").setExecutor(afkC);
        plugin.getCommand("afk").setTabCompleter(afkC);
    }

    private void setupAfkListCommand() {
        CommandManager afkListC = new CommandManager(plugin, new ArrayList<>(), new AfkList());
        plugin.getCommand("afklist").setExecutor(afkListC);
        plugin.getCommand("afklist").setTabCompleter(afkListC);
    }

    private void setupRepairCommand() {
        CommandManager repairC = new CommandManager(plugin, new ArrayList<>(), new Repair());
        plugin.getCommand("repair").setExecutor(repairC);
        plugin.getCommand("repair").setTabCompleter(repairC);
    }

    private void setupEnderchestCommand() {
        CommandManager enderchestC = new CommandManager(plugin, new ArrayList<>(), new Enderchest());
        plugin.getCommand("enderchest").setExecutor(enderchestC);
        plugin.getCommand("enderchest").setTabCompleter(enderchestC);
    }

    private void setupWorkbenchCommand() {
        CommandManager workbenchC = new CommandManager(plugin, new ArrayList<>(), new Workbench());
        plugin.getCommand("workbench").setExecutor(workbenchC);
        plugin.getCommand("workbench").setTabCompleter(workbenchC);
    }

    private void setupAnvilCommand() {
        CommandManager anvilC = new CommandManager(plugin, new ArrayList<>(), new Anvil());
        plugin.getCommand("anvil").setExecutor(anvilC);
        plugin.getCommand("anvil").setTabCompleter(anvilC);
    }

    @Override
    public void onDisable() {
        // Detener el sistema de verificación de inactividad
        AfkManager.stopInactivityChecker();
    }

}
