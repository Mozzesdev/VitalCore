package me.winflix.vitalcore.warps;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.warps.commands.WarpCommand;
import me.winflix.vitalcore.warps.commands.WarpDelCommand;
import me.winflix.vitalcore.warps.commands.WarpFavCommand;
import me.winflix.vitalcore.warps.commands.WarpGuiCommand;
import me.winflix.vitalcore.warps.commands.WarpInfoCommand;
import me.winflix.vitalcore.warps.commands.WarpListCommand;
import me.winflix.vitalcore.warps.commands.WarpSetCommand;
import me.winflix.vitalcore.warps.events.WarpListener;

public class Warps extends Manager {

    private final ArrayList<SubCommand> warpCommands = new ArrayList<>();
    private WarpManager warpManager;

    public Warps(VitalCore plugin) {
        super(plugin);
        this.warpManager = new WarpManager(plugin);
    }

    public Warps initialize() {
        warpManager.initialize();
        setupEvents();
        setupCommands();
        return this;
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new WarpListener(warpManager), plugin);
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand warpCommand = plugin.getCommand("warp");
        CommandManager warpCommandManager = new CommandManager(plugin, warpCommands, new WarpCommand(warpManager));
        assert warpCommand != null;
        warpCommand.setExecutor(warpCommandManager);
        warpCommand.setTabCompleter(warpCommandManager);
    }

    public void registerCommands() {
        warpCommands.add(new WarpSetCommand(warpManager));
        warpCommands.add(new WarpDelCommand(warpManager));
        warpCommands.add(new WarpFavCommand(warpManager));
        warpCommands.add(new WarpListCommand(warpManager));
        warpCommands.add(new WarpInfoCommand(warpManager));
        warpCommands.add(new WarpGuiCommand(warpManager));
    }

    @Override
    public void onDisable() {
        // Cleanup si es necesario
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }
}
