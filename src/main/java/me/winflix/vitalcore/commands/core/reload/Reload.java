package me.winflix.vitalcore.commands.core.reload;

import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.utils.Utils;

public class Reload extends SubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getVariants() {
        return "r";
    }

    @Override
    public String getPermission() {
        return "vitalcore.bypass";
    }

    @Override
    public String getDescription() {
        return "This command save the home of your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/vcore reload";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        VitalCore.fileManager.reloadAllFiles();
        Utils.successMessage(p, "Se ha recargado el plugin correctamente");
    }
}
