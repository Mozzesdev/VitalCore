package me.winflix.vitalcore.residents;

import java.util.ArrayList;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.EntityType;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.residents.commands.Create;
import me.winflix.vitalcore.residents.entities.controllers.PlayerController;
import me.winflix.vitalcore.residents.trait.TraitManager;
import me.winflix.vitalcore.residents.utils.controllers.EntityControllers;

public class Residents extends Manager {

    public static TraitManager traitManager;
        private final ArrayList<SubCommand> npcCommands = new ArrayList<>();


    public Residents(VitalCore plugin) {
        super(plugin);
    }

    @Override
    public void initialize() {
        traitManager = new TraitManager();
        loadEntityTypes();
        setupCommands();
        setupEvents();
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

}
