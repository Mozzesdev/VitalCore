package me.winflix.vitalcore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.menu.Menu;
import me.winflix.vitalcore.menu.PlayerMenuUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class CommandManager implements CommandExecutor {

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

        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args.length == 0) {
                if (menu != null) {
                    try {
                        Constructor<? extends Menu> constructor = menu.getDeclaredConstructor(PlayerMenuUtility.class);
                        Menu instance = constructor.newInstance(VitalCore.getPlayerMenuUtility(p));
                        instance.open();
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                            | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (int i = 0; i < getSubcommands().size(); i++) {
                    if (args[0].equalsIgnoreCase(getSubcommands().get(i).getName())
                            || args[0].equalsIgnoreCase(getSubcommands().get(i).getVariants())) {
                        getSubcommands().get(i).perform(p, args);
                    }
                }
            }

        }

        return true;
    }

    public ArrayList<SubCommand> getSubcommands() {
        return subcommands;
    }

}
