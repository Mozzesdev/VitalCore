package me.winflix.vitalcore.tribes.commands.members;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.tribe.TribesDAO;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMenu;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMessages;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribes.models.Tribe;
import me.winflix.vitalcore.tribes.models.TribeMember;
import me.winflix.vitalcore.tribes.utils.RankManager;
import me.winflix.vitalcore.tribes.utils.TribeUtils;

public class Leave extends SubCommand {

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getVariants() {
        return "l";
    }

    @Override
    public String getDescription() {
        return "This command invite a player or players to your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/tribe leave";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        // Obtener el usuario actual y su tribu
        Tribe tribe = TribesDAO.getTribeByMember(p.getUniqueId());
        if (tribe == null) {
            Utils.errorMessage(p, "No perteneces a ninguna tribu.");
            return;
        }

        // Verificar si el jugador es el único miembro de la tribu
        if (tribe.getMembers().size() <= 1) {
            Utils.errorMessage(p, "No puedes salirte de tu tribu si solo estás tú.");
            return;
        }

        // Mensaje de confirmación y cancelación
        String menuName = "¿Deseas salirte de la tribu?";
        String confirmMessage = "&aSalir de la tribu";
        List<String> confirmLore = Collections.singletonList("&7Click para salir de la tribu");
        String cancelMessage = "&cCancelar";
        List<String> cancelLore = Collections.singletonList("&7Click para cancelar!");

        // Crear mensajes para el menú de confirmación
        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage, cancelLore);

        // Crear el menú de confirmación
        ConfirmMenu confirmMenu = new ConfirmMenu(
                p,
                VitalCore.fileManager.getMessagesFile().getConfig(),
                confirmMessages,
                menuName);

        // Acción después de cerrar el menú de confirmación
        confirmMenu.afterClose((confirmed) -> {
            if (confirmed) {
                TribeMember member = tribe.getMember(p.getUniqueId());

                // Verificar si el jugador es el dueño de la tribu
                if (member.getRange().getName().equals(RankManager.OWNER_RANK.getName())) {
                    TribeMember newOwner = tribe.getDifferentMember(p.getUniqueId());
                    newOwner.setRange(RankManager.OWNER_RANK);
                    tribe.replaceMember(newOwner.getPlayerId(), newOwner);
                    tribe.setTribeName("Tribe_of_" + newOwner.getPlayerName());
                }

                // Quitar al jugador de la tribu
                tribe.removeMember(member);
                TribesDAO.saveTribe(tribe);

                // Notificaciones
                Utils.successMessage(p, "Has salido correctamente de tu tribu y se creó tu nueva tribu.");
                TribeUtils.castMessageToAllMembersTribe(tribe,
                        p.getName() + " ha abandonado la tribu.");
            }
        });

        // Abrir el menú de confirmación
        confirmMenu.open();
    }

}