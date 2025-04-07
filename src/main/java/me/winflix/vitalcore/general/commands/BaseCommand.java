package me.winflix.vitalcore.general.commands;

import java.util.List;

import org.bukkit.entity.Player;

public abstract class BaseCommand {

    public abstract String getName();

    public abstract String getVariants();

    public abstract String getDescription();

    public abstract String getPermission();

    public abstract String getSyntax();

    public abstract List<String> getArguments(Player player, String args[]);

    public abstract void perform(Player player, String[] args);
}