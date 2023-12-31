package me.winflix.vitalcore.tribe.commands.home;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.User;

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
        // Obtener la información del jugador desde la base de datos
        User pdb = UsersCollection.getUserWithTribe(p.getUniqueId());

        // Obtener la ubicación de la tribu del jugador en formato de cadena
        String tribeHome = pdb.getTribe().getTribeHome();

        // Obtener la configuración de mensajes
        FileConfiguration messagesFile = VitalCore.fileManager.getMessagesFile().getConfig();
        String successMessage = messagesFile.getString("tribes.homes.to.success");
        String errorMessage = messagesFile.getString("tribes.homes.to.error");

        // Verificar si la ubicación de la tribu está vacía
        if (tribeHome.isEmpty()) {
            Utils.errorMessage(p, errorMessage);
            return;
        }

        // Dividir la cadena de ubicación en sus componentes
        String[] split = tribeHome.split(";");
        World world = Bukkit.getServer().getWorld(split[0]);
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        float yaw = Float.parseFloat(split[4]);
        float pitch = Float.parseFloat(split[5]);

        // Teletransportar al jugador a la ubicación de la tribu
        p.teleport(new Location(world, x, y, z, yaw, pitch));

        // Mensaje de éxito
        Utils.successMessage(p, successMessage);
    }

}
