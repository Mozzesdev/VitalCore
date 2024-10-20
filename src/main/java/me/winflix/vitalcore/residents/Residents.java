package me.winflix.vitalcore.residents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.residents.commands.Create;
import me.winflix.vitalcore.residents.entities.controllers.PlayerController;
import me.winflix.vitalcore.residents.interfaces.NPC;
import me.winflix.vitalcore.residents.models.ResidentNPC;
import me.winflix.vitalcore.residents.trait.TraitManager;
import me.winflix.vitalcore.residents.utils.controllers.EntityControllers;

public class Residents extends Manager {

    public static TraitManager traitManager;
    public static Map<UUID, NPC> npcs = new HashMap<>();
    private final ArrayList<SubCommand> npcCommands = new ArrayList<>();

    public Residents(VitalCore plugin) {
        super(plugin);
    }

    @Override
    public Residents initialize() {
        traitManager = new TraitManager();
        loadEntityTypes();
        setupCommands();
        setupEvents();
        new NPCUpdateTasks().runTaskTimer(plugin, 0, 1);
        return this;
    }

    @Override
    public void setupCommands() {
        registerCommands();
        PluginCommand npcCommand = plugin.getCommand("npc");
        CommandManager npcCommandManager = new CommandManager(plugin, npcCommands);
        npcCommand.setExecutor(npcCommandManager);
    }

    @Override
    public void setupEvents() {
    }

    @Override
    public void registerCommands() {
        npcCommands.add(new Create());
    }

    public static TraitManager getTraitManager() {
        return traitManager;
    }

    private void loadEntityTypes() {
        EntityControllers.setEntityControllerForType(EntityType.PLAYER, PlayerController.class);
    }

    public static Map<UUID, NPC> getNpcs() {
        return npcs;
    }

    class NPCUpdateTasks extends BukkitRunnable {

        @Override
        public void run() {
            npcs.values().forEach(npc -> {
                new PlayerTick(() -> ((ResidentNPC) npc).update()).run();
            });
        }

        private static class PlayerTick implements Runnable {
            private final Runnable tick;

            public PlayerTick(Runnable tick) {
                this.tick = tick;
            }

            @Override
            public void run() {
                tick.run();
            }
        }
    }

}
