package me.winflix.vitalcore.tribes.commands;

import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.BaseCommand;
import me.winflix.vitalcore.tribes.menu.TribeMenu;

public class BaseTribeCommand extends BaseCommand {
    @Override
    public String getName() {
        return "tribe";
    }

    @Override
    public String getVariants() {
        return "tr";
    }

    @Override
    public String getDescription() {
        return "Tribe command";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/tribe <subcommand>";
    }

    @Override
    public List<String> getArguments(Player player, String args[]) {
        return null;
    }

    @Override
    public void perform(Player player, String[] args) {
        new TribeMenu(player).open();
    }
}