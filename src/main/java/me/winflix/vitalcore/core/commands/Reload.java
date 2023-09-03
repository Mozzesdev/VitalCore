package me.winflix.vitalcore.core.commands;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.files.FileManager;
import me.winflix.vitalcore.general.utils.Utils;

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
        FileManager fileManager = VitalCore.fileManager;
        fileManager.reloadAllFiles();
        FileConfiguration messageFile = fileManager.getMessagesFile().getConfig();
        String message = messageFile.getString("reload");
        Utils.successMessage(p, message);
    }
}
