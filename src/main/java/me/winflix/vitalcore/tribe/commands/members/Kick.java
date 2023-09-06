package me.winflix.vitalcore.tribe.commands.members;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMenu;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMessages;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.Rank;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.TribeMember;
import me.winflix.vitalcore.tribe.models.User;

public class Kick extends SubCommand {

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getVariants() {
        return "k";
    }

    @Override
    public String getDescription() {
        return "This command kick a player or players to your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/tribe kick <player>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void perform(Player sender, String[] args) {
        // Verificar la cantidad de argumentos
        if (args.length <= 1) {
            Utils.errorMessage(sender, "Syntax error: use " + getSyntax());
            return;
        }

        // Obtener el jugador objetivo
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);

        if (targetPlayer == null) {
            Utils.errorMessage(sender, "Ese jugador no existe.");
            return;
        }

        // Verificar si el jugador intenta expulsarse a sí mismo
        if (targetPlayer.getName().equalsIgnoreCase(sender.getName())) {
            Utils.errorMessage(sender, "No puedes expulsarte a ti mismo, intenta con /tribe leave.");
            return;
        }

        // Obtener información del jugador que ejecuta la expulsión
        User senderDB = UsersCollection.getUserWithTribe(sender.getUniqueId());
        Tribe tribeDB = senderDB.getTribe();
        Rank senderRange = tribeDB.getMember(sender.getUniqueId()).getRange();

        // Obtener información del miembro objetivo
        TribeMember member = tribeDB.getMember(targetPlayer.getUniqueId());

        if (member == null) {
            Utils.errorMessage(sender, "Ese jugador no se encuentra en tu tribu.");
            return;
        }

        // Verificar si el jugador tiene permisos para expulsar al miembro
        if (!senderRange.canKick(member)) {
            Utils.errorMessage(sender, "Solo puedes expulsar a jugadores teniendo un rango superior al expulsado.");
            return;
        }

        // Mensaje de confirmación y cancelación
        String menuName = "Deseas expulsar a " + targetPlayer.getName();
        String confirmMessage = "&aExpulsar a " + targetPlayer.getName();
        List<String> confirmLore = new ArrayList<String>();
        confirmLore.add("&7Click para expulsar a " + targetPlayer.getName() + "!");
        String cancelMessage = "&cCancelar expulsión";
        List<String> cancelLore = new ArrayList<String>();
        cancelLore.add("&7Click para cancelar la expulsión!");

        // Crear mensajes para el menú de confirmación
        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage, cancelLore);

        // Crear el menú de confirmación
        ConfirmMenu confirmMenu = new ConfirmMenu(
                VitalCore.getPlayerMenuUtility(sender),
                VitalCore.fileManager.getMessagesFile().getConfig(),
                confirmMessages,
                menuName);

        // Acción después de cerrar el menú de confirmación
        confirmMenu.afterClose((confirmed) -> {
            if (confirmed) {
                // Eliminar al miembro de la tribu y guardarla
                tribeDB.removeMember(member);
                TribesCollection.saveTribe(tribeDB);

                // Crear una nueva tribu para el miembro expulsado
                Tribe newTribe = TribesCollection.createTribe(targetPlayer);
                User playerDB = UsersCollection.getUserWithTribe(targetPlayer.getUniqueId());
                playerDB.setTribeId(newTribe.getId());
                UsersCollection.saveUser(playerDB);

                // Mensajes de éxito
                String kickSenderMessage = "Se ha expulsado correctamente a " + targetPlayer.getName();
                String kickTargetMessage = "Te han expulsado de la tribu de &6" + tribeDB.getTribeName();
                Utils.successMessage(sender, kickSenderMessage);
                if (targetPlayer.isOnline()) {
                    Utils.errorMessage(targetPlayer.getPlayer(), kickTargetMessage);
                }
            }
        });

        // Abrir el menú de confirmación
        confirmMenu.open();
    }

}
