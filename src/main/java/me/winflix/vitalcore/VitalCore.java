package me.winflix.vitalcore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.skins.Skins;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.winflix.vitalcore.core.Core;
import me.winflix.vitalcore.general.database.Database;
import me.winflix.vitalcore.general.events.MenuEvents;
import me.winflix.vitalcore.general.files.FileManager;
import me.winflix.vitalcore.general.models.PlayerMenuUtility;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.Tribe;

public class VitalCore extends JavaPlugin  {
    public static final Logger Log = Logger.getLogger("VitalCore");
    private static VitalCore plugin;
    public static FileManager fileManager;
    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;

        new Tribe().initialize();
        new Core().initialize();
        new Skins().initialize();
        new Citizen().initialize();

        registerGeneralEvents();

        Database.connect();
        fileManager = new FileManager(plugin);

        Log.info(Utils.useColors("&ahas been enabled"));
    }

    @Override
    public void onDisable() {
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
