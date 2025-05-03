package me.winflix.vitalcore.addons.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.managers.ModelEngineManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;

public class Spawn extends SubCommand {

    @Override
    public String getName() {
        return "spawn";
    }

    @Override
    public String getVariants() {
        return "s"; // Mantener si se usa
    }

    @Override
    public String getDescription(Player p) {
        // Descripción más precisa
        return "Spawns a specified custom model.";
    }

    @Override
    public String getPermission() {
        return "vitalcore.addons.spawn";
    }

    @Override
    public String getSyntax(Player p) {
        // Sintaxis actualizada y más clara
        return "/addons spawn <modelName> [x,y,z | world,x,y,z | look]";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        ModelEngineManager engine = VitalCore.addons.getModelEngineManager();

        if (engine == null) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            // Sugerir nombres de modelos que coincidan parcialmente
            String input = args[1].toLowerCase();
            return engine.getModelNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Sugerir 'look' o coordenadas relativas de ejemplo
            List<String> suggestions = new ArrayList<>();
            suggestions.add("look");
            suggestions.add(String.format("%.1f,%.1f,%.1f", player.getLocation().getX(), player.getLocation().getY(),
                    player.getLocation().getZ()));
            suggestions.add("~,~+2,~");
            return suggestions;
        }
        return Collections.emptyList();
    }

    public boolean isDisabled() {
        return false;
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

        // --- Verificación de Modelo (Mejorada si es posible) ---
        if (engine.getModel(modelName) == null) {
            Utils.errorMessage(player, "Model not found: " + modelName);
            return;
        }

        // --- Parseo de Ubicación ---
        Location targetLocation;
        try {
            targetLocation = parseLocation(player, args);
        } catch (IllegalArgumentException e) {
            Utils.errorMessage(player, e.getMessage());
            return;
        }

        // --- Spawn del Modelo ---
        try {
            engine.spawn(modelName, targetLocation, player);
            Utils.successMessage(player,
                    "Model '" + modelName + "' spawned at " +
                            String.format("%s: %.1f, %.1f, %.1f",
                                    targetLocation.getWorld().getName(),
                                    targetLocation.getX(),
                                    targetLocation.getY(),
                                    targetLocation.getZ()));
        } catch (Exception e) { // Captura excepciones específicas del 'spawn' si es posible
            Utils.errorMessage(player, "Error spawning model: " + e.getMessage());
            // Loguear el error en lugar de imprimir stack trace directamente
            VitalCore.Log.log(Level.SEVERE,
                    "Failed to spawn model '" + modelName + "' for player " + player.getName(), e);
        }
    }

    /**
     * Parsea la ubicación desde los argumentos del comando.
     *
     * @param player El jugador que ejecuta el comando.
     * @param args   Los argumentos del comando.
     * @return La Location parseada.
     * @throws IllegalArgumentException Si el formato de la ubicación es inválido.
     */
    private Location parseLocation(Player player, String[] args) throws IllegalArgumentException {
        if (args.length < 3) {
            return new Location(player.getLocation().getWorld(), player.getLocation().getX(),
                    player.getLocation().getY(), player.getLocation().getZ());
        }

        String locArg = args[2];

        if (locArg.equalsIgnoreCase("look")) {
            // Ubicación donde mira el jugador (encima del bloque)
            Block targetBlock = player.getTargetBlockExact(10); // Rango máximo de 10 bloques
            if (targetBlock == null) {
                throw new IllegalArgumentException("You are not looking at a block within range (10 blocks).");
            }
            // Spawn 1 bloque encima del bloque apuntado
            return targetBlock.getLocation().add(0.5, 1.0, 0.5); // Centrado y 1 arriba
        }

        // Intentar parsear coordenadas [world,]x,y,z
        String[] parts = locArg.split(",");
        World world = player.getWorld();
        double x, y, z;
        int coordStartIndex = 0;

        if (parts.length == 4) {
            // Posibilidad de incluir mundo: world,x,y,z
            World specifiedWorld = Bukkit.getWorld(parts[0]);
            if (specifiedWorld == null) {
                throw new IllegalArgumentException("Invalid world specified: " + parts[0]);
            }
            world = specifiedWorld;
            coordStartIndex = 1; // Las coordenadas empiezan desde el índice 1
        } else if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid location format. Use [world,]x,y,z or 'look'.");
        }

        try {
            x = Double.parseDouble(parts[coordStartIndex]);
            y = Double.parseDouble(parts[coordStartIndex + 1]);
            z = Double.parseDouble(parts[coordStartIndex + 2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinates. x, y, and z must be numbers.");
        }

        return new Location(world, x, y, z);
    }
}