package me.winflix.vitalcore.citizen;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.citizen.entities.PlayerController;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.citizen.utils.LocationLookup;
import me.winflix.vitalcore.citizen.trait.TraitFactory;
import me.winflix.vitalcore.citizen.utils.controller.EntityControllers;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.citizen.commands.Create;
import me.winflix.vitalcore.citizen.listeners.DeathListener;
import me.winflix.vitalcore.citizen.utils.managers.BodyManager;
import me.winflix.vitalcore.citizen.tasks.BodyRemoverTask;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.EntityType;

import java.util.*;

public class Citizen {
    private final ArrayList<SubCommand> npcCommands = new ArrayList<>();
    public static BodyManager bodyManger;
    private static TraitFactory traitFactory;
    VitalCore plugin = VitalCore.getPlugin();
    public static final Map<UUID, NPC> npcs = new HashMap<>();
    private static LocationLookup locationLookup;

    public void initialize() {
        bodyManger = new BodyManager();
        traitFactory = new TraitFactory();
        locationLookup = new LocationLookup();
        locationLookup.runTaskTimer(VitalCore.getPlugin(), 0, 5);
        setupEvents();
        setupCommands();
        tasksInitialize();
        loadEntityTypes();
    }

    public void setupCommands() {
        registerCommands();
        PluginCommand npcCommand = plugin.getCommand("npc");
        CommandManager tribeCommandManager = new CommandManager(plugin, npcCommands, null);
        assert npcCommand != null;
        npcCommand.setExecutor(tribeCommandManager);
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new DeathListener(), plugin);
    }

    public void registerCommands() {
        npcCommands.add(new Create());
    }

    public static BodyManager getBodyManger() {
        return bodyManger;
    }

    public void tasksInitialize() {
        new BodyRemoverTask().runTaskTimerAsynchronously(plugin, 0L, 20L);
    }

    public static TraitFactory getTraitFactory() {
        return traitFactory;
    }

    public static LocationLookup getLocationLookup() {
        return locationLookup;
    }

    public static Map<UUID, NPC> getNpcs() {
        return npcs;
    }

    private void loadEntityTypes() {
        EntityControllers.setEntityControllerForType(EntityType.PLAYER, PlayerController.class);
    }
}
