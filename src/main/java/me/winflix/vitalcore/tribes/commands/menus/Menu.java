package me.winflix.vitalcore.tribes.commands.menus;

import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.tribe.TribesDAO;
import me.winflix.vitalcore.tribes.menu.TribeMenu;
import me.winflix.vitalcore.tribes.models.Tribe;

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
    public String getDescription(Player p) {
        return "This command open a complete menu of your tribe";
    }

    @Override
    public String getSyntax(Player p) {
        return "/tribe menu";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        TribeMenu tribeMenu = new TribeMenu(p);
        Tribe tribe = TribesDAO.getTribeByMember(p.getUniqueId());

        if (tribe == null) {
            return;
        }

        VitalCore.Log.info("Opening tribe menu for " + tribe.getTribeName());

        tribeMenu.open();

    }
}
