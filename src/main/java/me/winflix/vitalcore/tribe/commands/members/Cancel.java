package me.winflix.vitalcore.tribe.commands.members;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import me.winflix.vitalcore.tribe.models.User;

public class Cancel extends SubCommand {

    @Override
    public String getName() {
        return "cancel";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getVariants() {
        return "can";
    }

    @Override
    public String getDescription() {
        return "tribes.commands.cancel.description";
    }

    @Override
    public String getSyntax() {
        return "/tribe cancel <tribe>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        Map<String, String> placeholders = new HashMap<>();
        FileConfiguration messageFile = VitalCore.fileManager.getMessagesFile().getConfig();
        placeholders.put(Placeholders.COMMAND_SYNTAX, getSyntax());

        // Verificar la cantidad de argumentos
        if (args.length < 2) {
            String syntaxMessage = messageFile.getString("tribes.commands.syntax");
            String finalMessage = Placeholders.replacePlaceholders(syntaxMessage, placeholders);
            Utils.errorMessage(p, finalMessage);
            return;
        }

        // Obtener el jugador que realiza la acción
        User senderPlayer = UsersCollection.getUserWithTribe(p.getUniqueId());
        placeholders.put(Placeholders.OFF_TARGET_NAME, senderPlayer.getPlayerName());

        // Obtener el nombre de la tribu
        String tribeName = String.join("_", Arrays.copyOfRange(args, 1, args.length));
        placeholders.put(Placeholders.TRIBE_NAME, tribeName);

        // Verificar si la tribu existe
        Tribe targetTribe = TribesCollection.getTribeByName(tribeName);

        if (targetTribe == null) {
            Utils.errorMessage(p, "Esa tribu no existe.");
            return;
        }

        // Obtener la invitación pendiente del jugador
        Optional<Invitation> invitationOptional = senderPlayer.getInvitations().stream()
                .filter(inv -> inv.getSenderTribeId().equalsIgnoreCase(targetTribe.getId()))
                .findFirst();

        if (!invitationOptional.isPresent()) {
            Utils.errorMessage(p, "No hay invitaciones pendientes de esa tribu.");
            return;
        }

        Invitation invitation = invitationOptional.get();

        boolean removedFromSender = senderPlayer.removeInvitation(invitation);
        boolean removedFromTarget = targetTribe.removeInvitation(invitation);

        // Mensaje de éxito
        String cancelMessage = messageFile.getString("tribes.invites.options.cancel");
        String finalMessage = Placeholders.replacePlaceholders(cancelMessage, placeholders);

        if (removedFromSender && removedFromTarget) {
            // Ambas eliminaciones fueron exitosas
            UsersCollection.saveUser(senderPlayer);
            TribesCollection.saveTribe(targetTribe);

            Utils.successMessage(p, finalMessage);
        } else {
            // Al menos una de las eliminaciones falló
            Utils.errorMessage(p, "Hubo un problema al cancelar la invitación, intentalo de nuevo.");
        }
    }
}
