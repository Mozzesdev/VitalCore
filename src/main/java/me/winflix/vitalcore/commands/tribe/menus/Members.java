package me.winflix.vitalcore.commands.tribe.menus;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.menu.tribe.MembersMenu;

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
    public void perform(Player p, String[] args) {
        new MembersMenu(VitalCore.getPlayerMenuUtility(p)).open();
    }
}
