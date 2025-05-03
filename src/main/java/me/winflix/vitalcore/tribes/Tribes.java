package me.winflix.vitalcore.tribes;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.tribes.commands.home.SetTribeHome;
import me.winflix.vitalcore.tribes.commands.BaseTribeCommand;
import me.winflix.vitalcore.tribes.commands.crud.Create;
import me.winflix.vitalcore.tribes.commands.crud.Delete;
import me.winflix.vitalcore.tribes.commands.home.ToTribeHome;
import me.winflix.vitalcore.tribes.commands.members.Accept;
import me.winflix.vitalcore.tribes.commands.members.Cancel;
import me.winflix.vitalcore.tribes.commands.members.Invite;
import me.winflix.vitalcore.tribes.commands.members.Kick;
import me.winflix.vitalcore.tribes.commands.members.Leave;
import me.winflix.vitalcore.tribes.commands.members.Promote;
import me.winflix.vitalcore.tribes.commands.menus.Members;
import me.winflix.vitalcore.tribes.commands.menus.Menu;
import me.winflix.vitalcore.tribes.events.JoinEvents;

public class Tribes extends Manager {

    private final ArrayList<SubCommand> tribeCommands = new ArrayList<>();

    public Tribes(VitalCore plugin) {
        super(plugin);
    }

    public Tribes initialize() {
        setupEvents();
        setupCommands();
        return this;
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new JoinEvents(), plugin);
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand tribeCommand = plugin.getCommand("tribe");
        CommandManager tribeCommandManager = new CommandManager(plugin, tribeCommands, new BaseTribeCommand());
        assert tribeCommand != null;
        tribeCommand.setExecutor(tribeCommandManager);
    }

    public void registerCommands() {
        tribeCommands.add(new Menu());
        tribeCommands.add(new SetTribeHome());
        tribeCommands.add(new ToTribeHome());
        tribeCommands.add(new Invite());
        tribeCommands.add(new Kick());
        tribeCommands.add(new Members());
        tribeCommands.add(new Leave());
        tribeCommands.add(new Promote());
        tribeCommands.add(new Accept());
        tribeCommands.add(new Cancel());
        tribeCommands.add(new Create());
        tribeCommands.add(new Delete());
    }

    @Override
    public void onDisable() {
    }

}
