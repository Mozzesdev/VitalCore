package me.winflix.vitalcore.tribe.commands.members;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.utils.Placeholders;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.Invitation;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.TribeMember;
import me.winflix.vitalcore.tribe.models.User;
import me.winflix.vitalcore.tribe.utils.RankManager;
import me.winflix.vitalcore.tribe.utils.TribeUtils;

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

        // Obtener información del jugador que realiza la acción
        User senderUser = UsersCollection.getUserWithTribe(sender.getUniqueId());
        Tribe senderTribe = senderUser.getTribe();

        // Obtener la tribu a la que se va a unir
        Tribe tribeToJoin = TribesCollection.getTribeByName(tribeName);

        // Verificar si la tribu a unirse existe
        if (tribeToJoin == null) {
            Utils.errorMessage(sender, "Esa tribu no existe.");
            return;
        }

        // Verificar si el jugador tiene una invitación pendiente de esa tribu
        Optional<Invitation> invitation = senderUser.getInvitations().stream()
                .filter((inv) -> inv.getSenderTribeId().equalsIgnoreCase(tribeToJoin.getId()))
                .findFirst();

        if (!invitation.isPresent()) {
            Utils.errorMessage(sender, "No tienes ninguna invitación pendiente de esa tribu.");
            return;
        }

        // Obtener información del miembro receptor
        TribeMember receiverMember = senderTribe.getMember(sender.getUniqueId());

        // Si la tribu actual tiene más de un miembro, verificar si el jugador es el
        // dueño y transferir el cargo si es necesario
        if (senderTribe.getMembers().size() > 1) {
            if (receiverMember.getRange().getName().equals(RankManager.OWNER_RANK.getName())) {
                TribeMember newOwner = senderTribe.getDifferentMember(sender.getUniqueId());
                newOwner.setRange(RankManager.OWNER_RANK);
                senderTribe.replaceMember(UUID.fromString(newOwner.getId()), newOwner);
            }
            senderTribe.removeMember(receiverMember);
        } else {
            // Si la tribu solo tiene al jugador actual, eliminar la tribu
            TribesCollection.deleteTribe(senderTribe);
        }

        // Establecer el rango del miembro receptor y agregarlo a la tribu a unirse
        receiverMember.setRange(RankManager.MEMBER_RANK);
        tribeToJoin.addMember(receiverMember);
        TribesCollection.saveTribe(tribeToJoin);

        // Eliminar la invitación de la tribu actual
        senderTribe.removeInvitation(invitation.get());
        TribesCollection.saveTribe(senderTribe);

        // Actualizar la información del jugador
        senderUser.setTribeId(tribeToJoin.getId());
        senderUser.setTribe(null);
        senderUser.removeInvitation(invitation.get());
        UsersCollection.saveUser(senderUser);

        // Mensajes de éxito
        Utils.successMessage(sender,
                "&cTe has unido a la tribu de &6"
                        + tribeToJoin.getTribeName().replaceAll("-", " "));
        TribeUtils.castMessageToAllMembersTribe(tribeToJoin,
                "Se ha añadido a la tribu a: " + sender.getDisplayName());
    }

}
