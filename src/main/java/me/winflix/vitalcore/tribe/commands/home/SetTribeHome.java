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
        // Obtener la ubicación actual del jugador
        Location loc = p.getLocation();
        String world = loc.getWorld().getName();

        // Obtener la información del jugador desde la base de datos
        User playerDB = UsersCollection.getUserWithTribe(p.getUniqueId());

        // Obtener la configuración de mensajes
        FileConfiguration messageFile = VitalCore.fileManager.getMessagesFile().getConfig();
        String successMessage = messageFile.getString("tribes.homes.add.success");
        String restrictedMessage = messageFile.getString("tribes.homes.add.restricted");

        // Verificar si el jugador se encuentra en el mundo "world"
        if (world.equals("world")) {
            // Construir la ubicación de la tribu en formato de cadena
            String tribeHome = world + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";"
                    + loc.getPitch();

            // Obtener la tribu del jugador
            Tribe playerTribe = playerDB.getTribe();

            // Establecer la ubicación de la tribu como la ubicación actual del jugador
            playerTribe.setTribeHome(tribeHome);

            // Guardar la tribu actualizada
            TribesCollection.saveTribe(playerTribe);

            // Mensaje de éxito
            Utils.successMessage(p, successMessage);
        } else {
            // Mensaje de error si el jugador no está en el mundo "world"
            Utils.errorMessage(p, restrictedMessage);
        }
    }

}
