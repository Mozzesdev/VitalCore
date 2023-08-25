package me.winflix.vitalcore.commands.tribe.menus;

import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.menu.tribe.TribeMenu;;

public class Menu extends SubCommand {

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public String getVariants() {
        return "m";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "This command open a complete menu of your tribe";
    }

    @Override
    public String getSyntax() {
        return "/tribe menu";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        new TribeMenu(VitalCore.getPlayerMenuUtility(p)).open();
    }
}
