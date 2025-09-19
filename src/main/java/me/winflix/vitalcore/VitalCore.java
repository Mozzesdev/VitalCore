package me.winflix.vitalcore;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.winflix.vitalcore.addons.Addons;
import me.winflix.vitalcore.core.Core;
import me.winflix.vitalcore.general.database.Database;
import me.winflix.vitalcore.general.events.MenuEvents;
import me.winflix.vitalcore.general.files.FileManager;
import me.winflix.vitalcore.general.models.PlayerMenuUtility;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.skins.Skins;
import me.winflix.vitalcore.tribes.Tribes;
import me.winflix.vitalcore.warps.Warps;

public class VitalCore extends JavaPlugin {
    public static final Logger Log = Logger.getLogger("VitalCore");
    private static VitalCore plugin;
    public static FileManager fileManager;
    public static Addons addons;
    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        fileManager = new FileManager(this);

        new Core(this).initialize();
        new Skins(this).initialize();
        new Tribes(this).initialize();
        new Warps(this).initialize();
        addons = new Addons(this).initialize();

        registerGeneralEvents();
        Database.connect();

        Log.info(Utils.useColors("&ahas been enabled"));
    }

    @Override
    public void onDisable() {
        Database.disconnect();
        Log.info(Utils.useColors("&chas been disabled"));
    }

    public static VitalCore getPlugin() {
        return plugin;
    }

    public static PlayerMenuUtility getPlayerMenuUtility(Player p) {
        return playerMenuUtilityMap.computeIfAbsent(p, PlayerMenuUtility::new);
    }

    private void registerGeneralEvents() {
        getServer().getPluginManager().registerEvents(new MenuEvents(), this);
    }

    public static Addons getAddons() {
        return addons;
    }

    public static FileManager getFileManager() {
        return fileManager;
    }

}
