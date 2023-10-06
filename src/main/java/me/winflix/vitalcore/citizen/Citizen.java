package me.winflix.vitalcore.npc;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.npc.commands.Create;
import me.winflix.vitalcore.npc.events.DeathListener;
import me.winflix.vitalcore.npc.events.MoveListener;
import me.winflix.vitalcore.npc.managers.BodyManager;
import me.winflix.vitalcore.npc.tasks.BodyRemoverTask;
import org.bukkit.command.PluginCommand;

import java.util.ArrayList;

public class Citizen {
    private final ArrayList<SubCommand> npcCommands = new ArrayList<>();
    public static BodyManager bodyManger;
    VitalCore plugin = VitalCore.getPlugin();

    public void initialize() {
        bodyManger = new BodyManager();
        setupEvents();
        setupCommands();
        tasksInitialize();
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand npcCommand = plugin.getCommand("npc");
        CommandManager tribeCommandManager = new CommandManager(plugin, npcCommands, null);
        assert npcCommand != null;
        npcCommand.setExecutor(tribeCommandManager);
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new MoveListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new DeathListener(), plugin);
    }

    public void registerCommands() {
        npcCommands.add(new Create());
    }

    public static BodyManager getBodyManger() {
        return bodyManger;
    }

    public void tasksInitialize(){
        new BodyRemoverTask().runTaskTimerAsynchronously(plugin, 0L, 20L);
    }
}
