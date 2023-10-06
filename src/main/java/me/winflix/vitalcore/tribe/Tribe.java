package me.winflix.vitalcore.tribe;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.tribe.commands.home.SetTribeHome;
import me.winflix.vitalcore.tribe.commands.home.ToTribeHome;
import me.winflix.vitalcore.tribe.commands.members.Accept;
import me.winflix.vitalcore.tribe.commands.members.Cancel;
import me.winflix.vitalcore.tribe.commands.members.Invite;
import me.winflix.vitalcore.tribe.commands.members.Kick;
import me.winflix.vitalcore.tribe.commands.members.Leave;
import me.winflix.vitalcore.tribe.commands.members.Promote;
import me.winflix.vitalcore.tribe.commands.menus.Members;
import me.winflix.vitalcore.tribe.commands.menus.Menu;
import me.winflix.vitalcore.tribe.events.JoinEvents;
import me.winflix.vitalcore.tribe.menu.TribeMenu;

public class TribeManager {

    private final ArrayList<SubCommand> tribeCommands = new ArrayList<>();
    VitalCore plugin = VitalCore.getPlugin();

    public void initialize() {
        setupEvents();
        setupCommands();
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new JoinEvents(plugin), plugin);
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand tribeCommand = plugin.getCommand("tribe");
        CommandManager tribeCommandManager = new CommandManager(plugin, tribeCommands, TribeMenu.class);
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
    }

}
