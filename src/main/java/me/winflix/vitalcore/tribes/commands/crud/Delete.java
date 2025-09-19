package me.winflix.vitalcore.tribes.commands.crud;

import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.SubCommand;

public class Delete extends SubCommand {

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getPermission() {
        return "tribe.delete";
    }

    @Override
    public String getVariants() {
        return "dl";
    }

    @Override
    public String getDescription(Player p) {
        return "tribes.commands.delete.description";
    }

    @Override
    public String getSyntax(Player p) {
        return "/tribe delete <tribe_name>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
    }

}
