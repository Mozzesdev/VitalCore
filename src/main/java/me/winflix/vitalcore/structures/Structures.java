package me.winflix.vitalcore.structures;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.structures.commands.Get;
import me.winflix.vitalcore.structures.events.BreakStructure;
import me.winflix.vitalcore.structures.events.PlaceStructure;
import me.winflix.vitalcore.structures.events.StructureProtection;
import me.winflix.vitalcore.structures.region.RegionManager;
import me.winflix.vitalcore.structures.states.PlayerBuildState;
import me.winflix.vitalcore.structures.utils.StructureManager;

public class Structures extends Manager {

    private final ArrayList<SubCommand> strCommands = new ArrayList<>();
    public static RegionManager regionManager;
    public static StructureManager structureManager;
    private PlayerBuildState playerBuildState;

    public Structures(VitalCore plugin) {
        super(plugin);
        playerBuildState = new PlayerBuildState();
        regionManager = new RegionManager();
        structureManager = new StructureManager();
    }

    public Structures initialize() {
        setupEvents();
        setupCommands();
        return this;
    }

    public PlayerBuildState getPlayerBuildState() {
        return playerBuildState;
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new PlaceStructure(regionManager, structureManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BreakStructure(regionManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new StructureProtection(regionManager), plugin);
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand strCommand = plugin.getCommand("structures");
        CommandManager strCommandManager = new CommandManager(plugin, strCommands);
        assert strCommand != null;
        strCommand.setExecutor(strCommandManager);
    }

    public void registerCommands() {
        strCommands.add(new Get(structureManager));
    }

    public void onDisable(Plugin plugin) {
    }
}
