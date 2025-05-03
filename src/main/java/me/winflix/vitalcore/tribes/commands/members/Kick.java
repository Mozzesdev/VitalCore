package me.winflix.vitalcore.tribes.commands.members;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.tribe.TribesDAO;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMenu;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMessages;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribes.models.Rank;
import me.winflix.vitalcore.tribes.models.Tribe;
import me.winflix.vitalcore.tribes.models.TribeMember;
import me.winflix.vitalcore.tribes.utils.TribeUtils;

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
    public String getDescription(Player p) {
        return "This command kick a player or players to your tribe.";
    }

    @Override
    public String getSyntax(Player p) {
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
            Utils.errorMessage(sender, "Syntax error: use " + getSyntax(sender));
            return;
        }

        // Obtener el jugador objetivo
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);

        if (targetPlayer == null) {
            Utils.errorMessage(sender, "Ese jugador no existe.");
            return;
        }

        // Verificar si el jugador intenta expulsarse a sí mismo
        if (targetPlayer.getUniqueId().equals(sender.getUniqueId())) {
            Utils.errorMessage(sender, "No puedes expulsarte a ti mismo, intenta con /tribe leave.");
            return;
        }

        // Obtener información del jugador que ejecuta la expulsión
        Tribe tribe = TribesDAO.getTribeByMember(sender.getUniqueId());
        if (tribe == null) {
            Utils.errorMessage(sender, "No perteneces a ninguna tribu.");
            return;
        }

        Rank senderRank = tribe.getMember(sender.getUniqueId()).getRange();

        // Obtener información del miembro objetivo
        TribeMember member = tribe.getMember(targetPlayer.getUniqueId());
        if (member == null) {
            Utils.errorMessage(sender, "Ese jugador no se encuentra en tu tribu.");
            return;
        }

        // Verificar si el jugador tiene permisos para expulsar al miembro
        if (!senderRank.canKick(member)) {
            Utils.errorMessage(sender, "Solo puedes expulsar a jugadores teniendo un rango superior al expulsado.");
            return;
        }

        // Mensaje de confirmación y cancelación
        String menuName = "Deseas expulsar a " + targetPlayer.getName();
        String confirmMessage = "&aExpulsar a " + targetPlayer.getName();
        List<String> confirmLore = new ArrayList<>();
        confirmLore.add("&7Click para expulsar a " + targetPlayer.getName() + "!");
        String cancelMessage = "&cCancelar expulsión";
        List<String> cancelLore = new ArrayList<>();
        cancelLore.add("&7Click para cancelar la expulsión!");

        // Crear mensajes para el menú de confirmación
        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage, cancelLore);

        // Crear el menú de confirmación
        ConfirmMenu confirmMenu = new ConfirmMenu(
                sender,
                VitalCore.fileManager.getMessagesFile(sender).getConfig(),
                confirmMessages,
                menuName);

        // Acción después de cerrar el menú de confirmación
        confirmMenu.afterClose((confirmed) -> {
            if (confirmed) {
                // Eliminar al miembro de la tribu
                tribe.removeMember(member);
                TribesDAO.saveTribe(tribe);

                // Mensajes de éxito
                String kickSenderMessage = "Se ha expulsado correctamente a " + targetPlayer.getName();
                String kickTargetMessage = "Te han expulsado de la tribu de &6" + tribe.getTribeName();
                Utils.successMessage(sender, kickSenderMessage);
                if (targetPlayer.isOnline()) {
                    Utils.errorMessage(targetPlayer.getPlayer(), kickTargetMessage);
                }

                // Notificar a la tribu
                TribeUtils.castMessageToAllMembersTribe(tribe,
                        targetPlayer.getName() + " ha sido expulsado de la tribu.");
            }
        });

        // Abrir el menú de confirmación
        confirmMenu.open();
    }

}
