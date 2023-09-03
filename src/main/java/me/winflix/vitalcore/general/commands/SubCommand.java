package me.winflix.vitalcore.general.commands;

import java.util.List;

import org.bukkit.entity.Player;

public abstract class SubCommand {

    // name of the subcommand ex. /prank <subcommand> <-- that
    public abstract String getName();

    public abstract String getVariants();

    // ex. "This is a subcommand that let's a shark eat someone"
    public abstract String getDescription();

    // permission needed for execute the command
    public abstract String getPermission();

    // How to use command ex. /prank freeze <player>
    public abstract String getSyntax();

    // name of the subcommandArgument ex. /prank <subcommand> <-- that
    public abstract List<String> getSubCommandArguments(Player player, String args[]);

    // code for the subcommand
    public abstract void perform(Player player, String args[]);

}
