package me.winflix.vitalcore.addons.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.model.data.ModelContext;
import me.winflix.vitalcore.addons.model.runtime.ModelEngineManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;

public class Spawn extends SubCommand {

    @Override
    public String getName() {
        return "spawn"; // Mantener nombre simple para spawnear entidades
    }

    @Override
    public String getVariants() {
        return "s";
    }

    @Override
    public String getDescription(Player p) {
        return "Spawns a model attached to a new invisible entity."; // Descripción específica
    }

    @Override
    public String getPermission() {
        return "vitalcore.addons.spawn"; // Permiso específico
    }

    @Override
    public String getSyntax(Player p) {
        // Sintaxis específica para este comando
        return "/addons spawn <modelName> [EntityType]";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        ModelEngineManager engine = VitalCore.addons.getModelEngineManager();
        if (engine == null)
            return Collections.emptyList();

        if (args.length == 2) {
            // Sugerir nombres de modelos
            String input = args[1].toLowerCase();
            return engine.getModelNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Sugerir tipos de entidad
            String input = args[2].toUpperCase();
            List<String> suggestions = new ArrayList<>();
            for (EntityType type : EntityType.values()) {
                if (type.isAlive() && type.isSpawnable() && type.name().startsWith(input)) {
                    suggestions.add(type.name());
                }
            }
            return suggestions;
        }
        return Collections.emptyList();
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            Utils.errorMessage(player, "Usage: " + getSyntax(player));
            return;
        }

        String modelName = args[1];
        ModelEngineManager engine = VitalCore.addons.getModelEngineManager();

        if (engine == null) {
            Utils.errorMessage(player, "ModelEngine is not available.");
            return;
        }
        if (engine.getModel(modelName) == null) {
            Utils.errorMessage(player, "Model not found: " + modelName);
            return;
        }

        EntityType baseEntityType = EntityType.PIG;
        if (args.length >= 3) {
            try {
                EntityType specifiedType = EntityType.valueOf(args[2].toUpperCase());
                if (specifiedType.isAlive() && specifiedType.isSpawnable()) {
                    baseEntityType = specifiedType;
                } else {
                    Utils.errorMessage(player,
                            "Specified EntityType '" + args[2] + "' is not spawnable/alive. Defaulting to PIG.");
                }
            } catch (IllegalArgumentException e) {
                Utils.errorMessage(player, "Invalid EntityType '" + args[2] + "'. Defaulting to PIG.");
            }
        }
        final EntityType finalBaseEntityType = baseEntityType; // Necesario para lambda

        // --- Crear Contexto y Llamar a createInstance ---
        Location spawnLoc = player.getLocation();
        ModelContext context = new ModelContext.Builder(spawnLoc)
                .type(ModelContext.InstanceType.ENTITY) // Especificar tipo ENTITY
                .owner(player)
                .customData("base_entity_type", finalBaseEntityType)
                .build();

        try {
            if (engine.createInstance(modelName, context) != null) {
                Utils.successMessage(player,
                        "Model '" + modelName + "' spawned with base entity " + finalBaseEntityType.name()
                                + " at your location.");
            } else {
                Utils.errorMessage(player, "Failed to create model instance.");
            }
        } catch (Exception e) {
            Utils.errorMessage(player, "Error creating model instance: " + e.getMessage());
            VitalCore.Log.log(Level.SEVERE,
                    "Failed to create model instance '" + modelName + "' for player " + player.getName(), e);
        }
    }
}