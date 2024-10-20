package me.winflix.vitalcore;

import java.util.HashMap;
import java.util.logging.Logger;

import me.winflix.vitalcore.structures.Structures;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.winflix.vitalcore.core.Core;
import me.winflix.vitalcore.general.events.MenuEvents;
import me.winflix.vitalcore.general.files.FileManager;
import me.winflix.vitalcore.general.models.PlayerMenuUtility;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.skins.Skins;

public class VitalCore extends JavaPlugin {
    public static final Logger Log = Logger.getLogger("VitalCore");
    private static VitalCore plugin;
    public static FileManager fileManager;
    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    public Structures structures;

    @Override
    public void onEnable() {
        plugin = this;
        fileManager = new FileManager(this);

        new Core(this).initialize();
        new Skins(this).initialize();
        structures = new Structures(this).initialize();

        registerGeneralEvents();
        // Database.connect();

        Log.info(Utils.useColors("&ahas been enabled"));
    }

    @Override
    public void onDisable() {
        structures.onDisable(plugin);
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

}
