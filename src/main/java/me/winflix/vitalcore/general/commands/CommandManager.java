package me.winflix.vitalcore.general.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.menu.Menu;
import me.winflix.vitalcore.general.menu.PlayerMenuUtility;
import me.winflix.vitalcore.general.utils.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager implements TabExecutor {

    private ArrayList<SubCommand> subcommands = new ArrayList<>();
    private Class<? extends Menu> menu;

    public CommandManager(VitalCore plugin, ArrayList<SubCommand> commands, Class<? extends Menu> menu) {
        for (SubCommand subCommand : commands) {
            subcommands.add(subCommand);
        }
        this.menu = menu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true; // No es un jugador, salimos
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            openMenu(player);
        } else {
            executeSubcommand(player, args);
        }

        return true;
    }

    private void openMenu(Player player) {
        if (menu == null) {
            return;
        }

        try {
            Constructor<? extends Menu> constructor = menu.getDeclaredConstructor(PlayerMenuUtility.class);
            Menu instance = constructor.newInstance(VitalCore.getPlayerMenuUtility(player));
            instance.open();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void executeSubcommand(Player player, String[] args) {
        FileConfiguration messageFile = VitalCore.fileManager.messagesFile.getConfig();
        String message = messageFile.getString("commands.unknown");

        for (SubCommand subCommand : getSubCommands()) {
            if (!player.isOp()) {
                if (subCommand.getPermission() != null && !player.hasPermission(subCommand.getPermission())) {
                    Utils.errorMessage(player, message);
                    return;
                }
            }

            if (args[0].equalsIgnoreCase(subCommand.getName()) || args[0].equalsIgnoreCase(subCommand.getVariants())) {
                subCommand.perform(player, args);
                return; // Terminamos aquí después de ejecutar un subcomando
            }
        }

        Utils.errorMessage(player, message);
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subcommands;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;

        if (args.length == 1) { // prank <subcommand> <args>
            return getSubCommands().stream()
                    .filter(subCommand -> (player.isOp() ? true
                            : subCommand.getPermission() == null
                                    || player.hasPermission(subCommand.getPermission())))
                    .map(SubCommand::getName)
                    .collect(Collectors.toList());
        } else if (args.length >= 2) {
            String requestedSubCommand = args[0].toLowerCase(); // Convert to lowercase for case-insensitive comparison

            for (SubCommand subCommand : getSubCommands()) {
                if (requestedSubCommand.equals(subCommand.getName())) {
                    List<String> subCommandArguments = subCommand.getSubCommandArguments(player, args);

                    if (subCommandArguments == null) {
                        List<String> playerNames = Bukkit.getServer().getOnlinePlayers()
                                .stream()
                                .map(Player::getName)
                                .collect(Collectors.toList());
                        return playerNames;
                    } else {
                        return subCommandArguments;
                    }
                }
            }
        }

        return Collections.emptyList();
    }

}
