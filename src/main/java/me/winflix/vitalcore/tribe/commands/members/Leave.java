package me.winflix.vitalcore.tribe.commands.members;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMenu;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMessages;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.TribeMember;
import me.winflix.vitalcore.tribe.models.User;
import me.winflix.vitalcore.tribe.utils.RankManager;

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
        User playerDB = UsersCollection.getUserWithTribe(p.getUniqueId());
        Tribe tribeDB = playerDB.getTribe();

        // Verificar si el jugador es el único miembro de la tribu
        if (tribeDB.getMembers().size() <= 1) {
            Utils.errorMessage(p, "No puedes salirte de tu tribu si solo estás tú.");
            return;
        }

        // Mensaje de confirmación y cancelación
        String menuName = "¿Deseas salirte de la tribu?";
        String confirmMessage = "&aInvitar a";
        List<String> confirmLore = Collections.singletonList("&7Click para invitar a");
        String cancelMessage = "&cCancelar invitación";
        List<String> cancelLore = Collections.singletonList("&7Click para cancelar la invitación!");

        // Crear mensajes para el menú de confirmación
        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage, cancelLore);

        // Crear el menú de confirmación
        ConfirmMenu confirmMenu = new ConfirmMenu(
                VitalCore.getPlayerMenuUtility(p),
                VitalCore.fileManager.getMessagesFile().getConfig(),
                confirmMessages,
                menuName);

        // Acción después de cerrar el menú de confirmación
        confirmMenu.afterClose((confirmed) -> {
            if (confirmed) {
                TribeMember member = tribeDB.getMember(p.getUniqueId());

                // Verificar si el jugador es el dueño de la tribu
                if (member.getRange().getName().equals(RankManager.OWNER_RANK.getName())) {
                    TribeMember newOwner = tribeDB.getDifferentMember(p.getUniqueId());
                    newOwner.setRange(RankManager.OWNER_RANK);
                    tribeDB.replaceMember(UUID.fromString(newOwner.getId()), newOwner);
                }

                // Quitar al jugador de la tribu y guardarla
                tribeDB.removeMember(member);
                TribesCollection.saveTribe(tribeDB);

                // Crear una nueva tribu para el jugador
                Tribe newTribe = TribesCollection.createTribe(p);
                playerDB.setTribeId(newTribe.getId());
                playerDB.setTribe(null);
                UsersCollection.saveUser(playerDB);

                Utils.successMessage(p, "Has salido correctamente de tu tribu y se creó tu nueva tribu.");
            }
        });

        // Abrir el menú de confirmación
        confirmMenu.open();
    }

}