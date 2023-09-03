package me.winflix.vitalcore.tribe.commands.home;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.User;

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
        User playerDB = UsersCollection.getUserWithTribe(p.getUniqueId());

        FileConfiguration messageFile = VitalCore.fileManager.getMessagesFile().getConfig();
        String successMessage = messageFile.getString("tribes.homes.add.success");
        String restrictedMessage = messageFile.getString("tribes.homes.add.restricted");

        if (world.equals("world")) {
            String tribeHome = world + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";"
                    + loc.getPitch();
            Tribe playerTribe = playerDB.getTribe();
            playerTribe.setTribeHome(tribeHome);
            TribesCollection.saveTribe(playerTribe);
            Utils.successMessage(p, successMessage);
        } else {
            Utils.errorMessage(p, restrictedMessage);
        }
    }
}
