package me.winflix.vitalcore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.winflix.vitalcore.commands.CommandManager;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.commands.tribe.home.SetTribeHome;
import me.winflix.vitalcore.commands.tribe.home.ToTribeHome;
import me.winflix.vitalcore.commands.tribe.invites.Invite;
import me.winflix.vitalcore.commands.tribe.invites.Leave;
import me.winflix.vitalcore.commands.tribe.menus.Members;
import me.winflix.vitalcore.commands.tribe.menus.Menu;
import me.winflix.vitalcore.database.Database;
import me.winflix.vitalcore.events.JoinEvents;
import me.winflix.vitalcore.events.MenuEvents;
import me.winflix.vitalcore.files.ConfigFile;
import me.winflix.vitalcore.menu.PlayerMenuUtility;
import me.winflix.vitalcore.menu.tribe.TribeMenu;
import me.winflix.vitalcore.utils.Utils;
import me.winflix.vitalcore.utils.YmlFileManager;

public class VitalCore extends JavaPlugin {
  public static final Logger Log = Logger.getLogger("CoreWild");
  private static VitalCore plugin;
  private ArrayList<SubCommand> tribeCommands = new ArrayList<SubCommand>();
  public static final String prefix = Utils.useColors("&7[&cCoreWild&7] ");
  private static YmlFileManager messagesConfigManager;
  private static ConfigFile configManager;

  private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

  @Override
  public void onEnable() {
    plugin = this;
    
    registerEvents();
    registerCommands();
    setupCommands();
    setupFolders();

    Database.connect();
    messagesConfigManager = new YmlFileManager(this, "messages.yml");
    configManager = new ConfigFile(plugin, "/");
    Log.info(Utils.useColors("&a[NoWipe] -> has been enabled"));
  }

  @Override
  public void onDisable() {
    Log.info(Utils.useColors("&c[NoWipe] -> has been disabled"));
  }

  public void registerEvents() {
    getServer().getPluginManager().registerEvents(new JoinEvents(this), this);
    getServer().getPluginManager().registerEvents(new MenuEvents(), this);
  }

  public void createFolder(String name) {
    File folder = new File(getDataFolder(), name);
    if (!folder.exists()) {
      try {
        folder.mkdir();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
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

  public static YmlFileManager getMessagesConfigManager() {
    return messagesConfigManager;
  }

  public static ConfigFile getConfigManager() {
    return configManager;
  }

  private void setupCommands() {
    getCommand("tribe").setExecutor(new CommandManager(this, tribeCommands, TribeMenu.class));
  }

  private void setupFolders() {
    createFolder("players-states");
  }

  private void registerCommands() {
    registerTribeCommands();
  }

  private void registerTribeCommands() {
    tribeCommands.add(new Menu());
    tribeCommands.add(new SetTribeHome());
    tribeCommands.add(new ToTribeHome());
    tribeCommands.add(new Invite(this));
    tribeCommands.add(new Members());
    tribeCommands.add(new Leave());
  }

}
