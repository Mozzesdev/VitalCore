package me.winflix.vitalcore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.winflix.vitalcore.commands.CommandManager;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.commands.core.reload.Reload;
import me.winflix.vitalcore.commands.tribe.home.SetTribeHome;
import me.winflix.vitalcore.commands.tribe.home.ToTribeHome;
import me.winflix.vitalcore.commands.tribe.members.Invite;
import me.winflix.vitalcore.commands.tribe.members.Kick;
import me.winflix.vitalcore.commands.tribe.members.Leave;
import me.winflix.vitalcore.commands.tribe.menus.Members;
import me.winflix.vitalcore.commands.tribe.menus.Menu;
import me.winflix.vitalcore.database.Database;
import me.winflix.vitalcore.events.JoinEvents;
import me.winflix.vitalcore.events.MenuEvents;
import me.winflix.vitalcore.files.FileManager;
import me.winflix.vitalcore.menu.PlayerMenuUtility;
import me.winflix.vitalcore.menu.tribe.TribeMenu;
import me.winflix.vitalcore.utils.Utils;

public class VitalCore extends JavaPlugin {
  public static final Logger Log = Logger.getLogger("VitalCore");
  private static VitalCore plugin;
  private ArrayList<SubCommand> tribeCommands = new ArrayList<SubCommand>();
  private ArrayList<SubCommand> vCoreCommands = new ArrayList<SubCommand>();
  public static FileManager fileManager;

  private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

  @Override
  public void onEnable() {
    plugin = this;
    registerEvents();
    registerCommands();

    Database.connect();
    fileManager = new FileManager(plugin);

    Log.info(Utils.useColors("&ahas been enabled"));
  }

  @Override
  public void onDisable() {
    Log.info(Utils.useColors("&chas been disabled"));
  }

  public void registerEvents() {
    getServer().getPluginManager().registerEvents(new JoinEvents(this), this);
    getServer().getPluginManager().registerEvents(new MenuEvents(), this);
  }

  public static PlayerMenuUtility getPlayerMenuUtility(Player p) {
    PlayerMenuUtility playerMenuUtility;
    if (playerMenuUtilityMap.containsKey(p)) {
      return playerMenuUtilityMap.get(p);
    } else {
      playerMenuUtility = new PlayerMenuUtility(p);
      playerMenuUtilityMap.put(p, playerMenuUtility);
      return playerMenuUtility;
    }
  }

  public static VitalCore getPlugin() {
    return plugin;
  }

  private void setupCommands() {
    PluginCommand tribeCommand = getCommand("tribe");
    CommandManager tribeCommandManager = new CommandManager(this, tribeCommands, TribeMenu.class);
    tribeCommand.setExecutor(tribeCommandManager);

    PluginCommand coreCommand = getCommand("vcore");
    CommandManager coreCommandManager = new CommandManager(this, vCoreCommands, null);
    coreCommand.setExecutor(coreCommandManager);
  }

  private void registerCommands() {
    registerTribeCommands();
    registerVCoreCommands();
    setupCommands();
  }

  private void registerTribeCommands() {
    tribeCommands.add(new Menu());
    tribeCommands.add(new SetTribeHome());
    tribeCommands.add(new ToTribeHome());
    tribeCommands.add(new Invite(this));
    tribeCommands.add(new Kick());
    tribeCommands.add(new Members());
    tribeCommands.add(new Leave());
  }

  public void registerVCoreCommands() {
    vCoreCommands.add(new Reload());
  }

}
