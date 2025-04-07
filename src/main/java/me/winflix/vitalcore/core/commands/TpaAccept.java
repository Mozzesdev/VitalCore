package me.winflix.vitalcore.core.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.core.managers.TeleportManager;
import me.winflix.vitalcore.general.commands.BaseCommand;

public class TpaAccept extends BaseCommand {

    @Override
    public String getName() {
        return "tpaccept";
    }

    @Override
    public String getVariants() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Accept a teleport request from another player";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/tpaccept <player>";
    }

    @Override
    public List<String> getArguments(Player player, String[] args) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getName().equalsIgnoreCase(player.getName()))
                .map(Player::getName)
                .toList();
    }

    @Override
    public void perform(Player player, String[] args) {
        TeleportManager.acceptRequest(player, args[0]);
    }
}