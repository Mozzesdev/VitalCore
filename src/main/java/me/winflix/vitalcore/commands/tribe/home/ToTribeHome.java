package me.winflix.vitalcore.commands.tribe.home;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.models.PlayerModel;

public class ToTribeHome extends SubCommand {

    @Override
    public String getName() {
        return "home";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getVariants() {
        return "h";
    }

    @Override
    public String getDescription() {
        return "This command teleport you to home of your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/tribe home";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        PlayerModel pdb = UserCollection.getPlayerWithTribe(p.getUniqueId());
        String tribeHome = pdb.getTribe().getTribeHome();

        String[] split = tribeHome.split(";");
        World world = Bukkit.getServer().getWorld(split[0]);
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        float yaw = Float.parseFloat(split[4]);
        float pitch = Float.parseFloat(split[5]);
        p.teleport(new Location(world, x, y, z, yaw, pitch));
    }
}
