package me.winflix.vitalcore.tribes.commands.menus;
import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.tribes.menu.MembersMenu;

public class Members extends SubCommand {

    @Override
    public String getName() {
        return "members";
    }

    @Override
    public String getVariants() {
        return "mb";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "This command save the home of your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/tribe members";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        new MembersMenu(p).open();
    }
}
