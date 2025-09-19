package me.winflix.vitalcore.core.commands;

import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.general.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

public class Feed extends BaseCommand {
    @Override
    public String getName() {
        return "feed";
    }

    @Override
    public String getVariants() {
        return "comer";
    }

    @Override
    public String getDescription() {
        return "Rellena la barra de hambre y saturación";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/feed";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return List.of();
    }

    @Override
    public void perform(Player player, String[] args) {
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);
        Utils.successMessage(player, "¡Has sido alimentado!");
    }
}
