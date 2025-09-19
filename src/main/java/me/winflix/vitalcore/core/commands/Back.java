package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.core.managers.BackManager;
import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class Back extends BaseCommand {

    @Override
    public String getName() {
        return "back";
    }

    @Override
    public String getVariants() {
        return "return";
    }

    @Override
    public String getDescription() {
        return "Regresa a tu ubicación anterior";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/back";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return List.of(); // Sin autocompletado para este comando
    }

    @Override
    public void perform(Player player, String[] args) {
        // Verificar si el jugador tiene una ubicación previa
        if (!BackManager.hasPreviousLocation(player)) {
            Utils.errorMessage(player, "No tienes una ubicación anterior registrada. Necesitas teletransportarte al menos una vez para usar este comando.");
            return;
        }
        
        // Verificar cooldown
        if (!BackManager.canUseBack(player)) {
            int remainingSeconds = BackManager.getRemainingCooldown(player);
            Utils.errorMessage(player, "Debes esperar " + remainingSeconds + " segundos antes de usar /back nuevamente.");
            return;
        }
        
        // Intentar teletransportar
        boolean success = BackManager.teleportToPreviousLocation(player);
        
        if (success) {
            // Establecer cooldown
            BackManager.setBackCooldown(player);
            
            // Mensaje de éxito
            Utils.successMessage(player, "¡Has regresado a tu ubicación anterior!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            
            // Efecto de partículas
            player.getWorld().spawnParticle(
                org.bukkit.Particle.PORTAL, 
                player.getLocation().add(0, 1, 0), 
                20, 
                0.5, 
                1.0, 
                0.5, 
                0.1
            );
        } else {
            Utils.errorMessage(player, "No se pudo regresar a tu ubicación anterior. Es posible que el mundo ya no exista.");
            // Limpiar ubicación inválida
            BackManager.removePreviousLocation(player);
        }
    }
}
