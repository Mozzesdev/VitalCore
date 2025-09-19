package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Heal extends BaseCommand {
    @Override
    public String getName() {
        return "heal";
    }

    @Override
    public String getVariants() {
        return "curar";
    }

    @Override
    public String getDescription() {
        return "Restaura la vida y elimina efectos negativos";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/heal";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(maxHealth);
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);
        player.setFallDistance(0f);
        player.setRemainingAir(player.getMaximumAir());
        // Eliminar efectos negativos comunes
        PotionEffectType[] badEffects = {
                PotionEffectType.POISON,
                PotionEffectType.WITHER,
                PotionEffectType.BLINDNESS,
                PotionEffectType.NAUSEA,
                PotionEffectType.HUNGER,
                PotionEffectType.SLOWNESS,
                PotionEffectType.MINING_FATIGUE,
                PotionEffectType.UNLUCK,
                PotionEffectType.WEAKNESS,
                PotionEffectType.LEVITATION,
                PotionEffectType.BAD_OMEN,
                PotionEffectType.DARKNESS
        };
        for (PotionEffectType type : badEffects) {
            player.removePotionEffect(type);
        }
        Utils.successMessage(player, "¡Has sido curado!");
    }
}
