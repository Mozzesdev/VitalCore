package me.winflix.vitalcore.tribes.commands.members;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.tribe.TribesDAO;
import me.winflix.vitalcore.general.database.collections.tribe.UsersDAO;
import me.winflix.vitalcore.general.utils.Placeholders;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribes.models.Invitation;
import me.winflix.vitalcore.tribes.models.Tribe;
import me.winflix.vitalcore.tribes.models.TribeMember;
import me.winflix.vitalcore.tribes.models.User;
import me.winflix.vitalcore.tribes.utils.InvitationsManager;
import me.winflix.vitalcore.tribes.utils.RankManager;
import me.winflix.vitalcore.tribes.utils.TribeUtils;

public class Accept extends SubCommand {

    @Override
    public String getName() {
        return "accept";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getVariants() {
        return "acc";
    }

    @Override
    public String getDescription() {
        return "This command invite a player or players to your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/tribe accept <tribe>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player sender, String[] args) {
        Map<String, String> placeholders = new HashMap<>();
        FileConfiguration messagesFile = VitalCore.fileManager.getMessagesFile().getConfig();
        placeholders.put(Placeholders.COMMAND_SYNTAX, getSyntax());

        // Verificar la cantidad de argumentos
        if (args.length < 2) {
            String syntaxMessage = messagesFile.getString("tribes.commands.syntax");
            String finalMessage = Placeholders.replacePlaceholders(syntaxMessage, placeholders);
            Utils.errorMessage(sender, finalMessage);
            return;
        }

        // Obtener el nombre de la tribu a unirse
        String tribeName = String.join("_", Arrays.copyOfRange(args, 1, args.length));

        // Obtener la tribu a la que se va a unir
        Tribe tribeToJoin = TribesDAO.getTribeByName(tribeName);

        User senderUser = UsersDAO.getUser(sender.getUniqueId());

        // Verificar si la tribu existe
        if (tribeToJoin == null) {
            Utils.errorMessage(sender, "Esa tribu no existe.");
            return;
        }

        // Verificar si el jugador tiene una invitación pendiente de esa tribu
        List<Invitation> playerInvitations = InvitationsManager.getPlayerInvitations(sender.getUniqueId());
        Optional<Invitation> invitation = playerInvitations.stream()
                .filter(inv -> inv.getType() == Invitation.InvitationType.TRIBE_TO_PLAYER)
                .filter(inv -> inv.getSenderId().equals(tribeToJoin.getId()))
                .findFirst();

        if (!invitation.isPresent()) {
            Utils.errorMessage(sender, "No tienes ninguna invitación pendiente de esa tribu.");
            return;
        }

        // Obtener la tribu actual del jugador (si tiene)
        Tribe tribeToLeave = TribesDAO.getTribeByMember(sender.getUniqueId());

        // Si el jugador está en una tribu, manejar la salida
        if (tribeToLeave != null) {
            TribeMember senderMember = tribeToLeave.getMember(sender.getUniqueId());

            // Si la tribu tiene más de un miembro, transferir el cargo si es necesario
            if (tribeToLeave.getMembers().size() > 1) {
                if (senderMember.getRange().getName().equals(RankManager.OWNER_RANK.getName())) {
                    TribeMember newOwner = tribeToLeave.getDifferentMember(sender.getUniqueId());
                    newOwner.setRange(RankManager.OWNER_RANK);
                    tribeToLeave.replaceMember(newOwner.getPlayerId(), newOwner);
                    tribeToLeave.setTribeName("Tribe_of_" + newOwner.getPlayerName());
                }
                tribeToLeave.removeMember(senderMember);
                TribesDAO.saveTribe(tribeToLeave);
            } else {
                // Si la tribu solo tiene al jugador actual, eliminarla
                TribesDAO.deleteTribe(tribeToLeave);
            }
        }

        // Crear el nuevo miembro y agregarlo a la tribu
        TribeMember newMember = new TribeMember(
                sender.getName(),
                sender.getUniqueId(),
                tribeToJoin.getId());
        newMember.setRange(RankManager.MEMBER_RANK);
        tribeToJoin.addMember(newMember);

        // Eliminar la invitación
        InvitationsManager.removeInvitation(invitation.get());

        // Guardar cambios
        TribesDAO.saveTribe(tribeToJoin);

        // Actualizar información del jugador
        senderUser.setTribe(tribeToJoin);
        UsersDAO.saveUser(senderUser);

        // Notificaciones
        Utils.successMessage(sender,
                "&cTe has unido a la tribu de &6"
                        + tribeToJoin.getTribeName().replaceAll("_", " "));
        TribeUtils.castMessageToAllMembersTribe(tribeToJoin,
                "Se ha añadido a la tribu a: " + sender.getDisplayName());
    }

}
