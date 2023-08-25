package me.winflix.vitalcore.commands.tribe.home;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.database.collections.TribeCollection;
import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.models.PlayerModel;
import me.winflix.vitalcore.models.TribeModel;
import me.winflix.vitalcore.utils.Utils;

public class SetTribeHome extends SubCommand {

    @Override
    public String getName() {
        return "sethome";
    }

    @Override
    public String getVariants() {
        return "sh";
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
        return "/tribe sethome";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        Location loc = p.getLocation();
        String world = loc.getWorld().getName();
        PlayerModel playerDB = UserCollection.getPlayerWithTribe(p.getUniqueId());

        if (world.equals("world")) {
            String tribeHome = world + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";"
                    + loc.getPitch();
            TribeModel playerTribe = playerDB.getTribe();
            playerTribe.setTribeHome(tribeHome);
            TribeCollection.saveTribe(playerTribe);
            Utils.successMessage(p, "Tribe home was saved");
        } else {
            Utils.errorMessage(p, "Only you can make a tribehome in overworld");
        }

    }
}
