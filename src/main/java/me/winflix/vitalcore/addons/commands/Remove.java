package me.winflix.vitalcore.addons.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.managers.ModelEngineManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;

public class Remove extends SubCommand {

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getVariants() {
        return "rm";
    }

    @Override
    public String getDescription(Player p) {
        return "Elimina el modelo que estés mirando";
    }

    @Override
    public String getPermission() {
        return "vitalcore.addons.remove";
    }

    @Override
    public String getSyntax(Player p) {
        return "/addons remove";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void perform(Player player, String[] args) {
        ModelEngineManager engine = VitalCore.addons.getModelEngineManager();
        if (engine == null) {
            Utils.errorMessage(player, "ModelEngine no está disponible.");
            return;
        }

        RayTraceResult trace = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                10, // Rango
                entity -> entity != player && engine.isModelEntity(entity)
        );

        if (trace == null || trace.getHitEntity() == null) {
            // Este mensaje ahora es más preciso: no se encontró una *entidad de modelo*
            Utils.errorMessage(player, "No estás mirando ninguna entidad de modelo dentro del rango.");
            return;
        }

        Entity target = trace.getHitEntity();
        // Ahora que el filtro ya verificó, destroy debería tener éxito (o manejar
        // errores internos)
        if (engine.destroy(target)) {
            Utils.successMessage(player, "Modelo eliminado correctamente.");
        } else {
            // Este caso sería más inesperado si isModelEntity funcionó. Quizás un error
            // interno?
            Utils.errorMessage(player, "Error al intentar eliminar el modelo (¿ya se eliminó?).");
            // Aquí podrías añadir logging si destroy puede fallar por otras razones
        }
    }
}
