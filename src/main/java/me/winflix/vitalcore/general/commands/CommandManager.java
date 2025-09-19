package me.winflix.vitalcore.general.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager implements TabExecutor {

    private ArrayList<SubCommand> subcommands = new ArrayList<>();
    private BaseCommand baseCommand;

    // Constructor que incluye comando base
    public CommandManager(VitalCore plugin, ArrayList<SubCommand> commands, BaseCommand baseCommand) {
        subcommands.addAll(commands);
        this.baseCommand = baseCommand;
    }

    // Constructor original para compatibilidad si no se requiere comando base
    public CommandManager(VitalCore plugin, ArrayList<SubCommand> commands) {
        this(plugin, commands, null);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // Caso 1: Solo comando base, sin subcomandos
        if (subcommands.size() == 0 && baseCommand != null) {
            if (!player.isOp()) {
                if (baseCommand.getPermission() != null && !player.hasPermission(baseCommand.getPermission())) {
                    Utils.errorMessage(player, "You don't have permission to use this command.");
                    return true;
                }
            }
            baseCommand.perform(player, args);
            return true;
        }

        // Caso 2: Solo subcomandos, sin comando base
        if (subcommands.size() > 0 && baseCommand == null && args.length >= 1) {
            boolean subCommandExecuted = executeSubcommand(player, args);
            if (!subCommandExecuted) {
                FileConfiguration messageFile = VitalCore.fileManager.getMessagesFile(player).getConfig();
                String message = messageFile.getString("general.commands.unknown");
                Utils.errorMessage(player, message);
            }
            return true;
        }

        // Caso 3: Comando base + subcomandos (como tribes y warps)
        if (subcommands.size() > 0 && baseCommand != null) {
            if (args.length == 0) {
                // Sin argumentos: ejecutar comando base
                if (!player.isOp()) {
                    if (baseCommand.getPermission() != null && !player.hasPermission(baseCommand.getPermission())) {
                        Utils.errorMessage(player, "You don't have permission to use this command.");
                        return true;
                    }
                }
                baseCommand.perform(player, args);
            } else {
                // Con argumentos: intentar subcomando primero, si no existe, ejecutar comando base
                boolean subCommandExecuted = executeSubcommand(player, args);
                if (!subCommandExecuted) {
                    // Si no se encontró subcomando, ejecutar comando base con argumentos
                    if (!player.isOp()) {
                        if (baseCommand.getPermission() != null && !player.hasPermission(baseCommand.getPermission())) {
                            Utils.errorMessage(player, "You don't have permission to use this command.");
                            return true;
                        }
                    }
                    baseCommand.perform(player, args);
                }
            }
            return true;
        }

        return true;
    }

    private boolean executeSubcommand(Player player, String[] args) {
        FileConfiguration messageFile = VitalCore.fileManager.getMessagesFile(player).getConfig();
        String message = messageFile.getString("general.commands.unknown");

        for (SubCommand subCommand : getSubCommands()) {
            if (args[0].equalsIgnoreCase(subCommand.getName()) || args[0].equalsIgnoreCase(subCommand.getVariants())) {
                if (!player.isOp()) {
                    if (subCommand.getPermission() != null && !player.hasPermission(subCommand.getPermission())) {
                        Utils.errorMessage(player, message);
                        return true; // Se encontró el subcomando pero no tiene permisos
                    }
                }
                subCommand.perform(player, args);
                return true; // Subcomando encontrado y ejecutado
            }
        }

        return false; // No se encontró ningún subcomando
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

        // Caso 1: Solo BaseCommand sin subcomandos
        if (subcommands.size() == 0 && baseCommand != null) {
            // Delegar tab completion al BaseCommand
            return baseCommand.getArguments(player, args);
        }

        // Caso 2: Solo subcomandos sin BaseCommand
        if (subcommands.size() > 0 && baseCommand == null) {
            if (args.length == 1) {
                return getSubCommands().stream()
                        .filter(subCommand -> (player.isOp() ? true
                                : subCommand.getPermission() == null
                                        || player.hasPermission(subCommand.getPermission())))
                        .map(SubCommand::getName)
                        .collect(Collectors.toList());
            } else if (args.length >= 2) {
                String requestedSubCommand = args[0].toLowerCase();

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
        }

        // Caso 3: BaseCommand + subcomandos (como tribes y warps)
        if (subcommands.size() > 0 && baseCommand != null) {
            if (args.length == 1) {
                // Combinar subcomandos con argumentos del BaseCommand
                List<String> completions = new ArrayList<>();
                
                // Agregar subcomandos filtrados por permisos
                List<String> subCommandNames = getSubCommands().stream()
                        .filter(subCommand -> (player.isOp() ? true
                                : subCommand.getPermission() == null
                                        || player.hasPermission(subCommand.getPermission())))
                        .map(SubCommand::getName)
                        .collect(Collectors.toList());
                completions.addAll(subCommandNames);
                
                // Agregar argumentos del BaseCommand si los tiene
                List<String> baseCommandArgs = baseCommand.getArguments(player, args);
                if (baseCommandArgs != null && !baseCommandArgs.isEmpty()) {
                    completions.addAll(baseCommandArgs);
                }
                
                return completions;
            } else if (args.length >= 2) {
                String requestedSubCommand = args[0].toLowerCase();
                
                // Primero verificar si es un subcomando
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
                
                // Si no es un subcomando, delegar al BaseCommand
                return baseCommand.getArguments(player, args);
            }
        }

        return Collections.emptyList();
    }

}
