package me.winflix.vitalcore.tribes.commands.members;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.tribe.TribesDAO;
import me.winflix.vitalcore.general.utils.ClickableMessage;
import me.winflix.vitalcore.general.utils.Placeholders;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribes.models.Invitation;
import me.winflix.vitalcore.tribes.models.Rank;
import me.winflix.vitalcore.tribes.models.Tribe;
import me.winflix.vitalcore.tribes.utils.InvitationsManager;

public class Invite extends SubCommand {

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getVariants() {
        return "inv";
    }

    @Override
    public String getDescription() {
        return "tribes.commands.invite.description";
    }

    @Override
    public String getSyntax() {
        return "/tribe invite <player>";
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
        if (args.length != 2) {
            String syntaxMessage = messageFile.getString("tribes.commands.syntax");
            String finalMessage = Placeholders.replacePlaceholders(syntaxMessage, placeholders);
            Utils.errorMessage(p, finalMessage);
            return;
        }

        // Obtener el jugador que realiza la acción
        Tribe senderTribe = TribesDAO.getTribeByMember(p.getUniqueId());
        placeholders.put(Placeholders.OFF_TARGET_NAME, p.getDisplayName());

        if (senderTribe == null) {
            Utils.errorMessage(p, messageFile.getString("tribes.invites.options.no_tribe"));
            return;
        }

        // Verificar si la tribu está cerrada
        if (!senderTribe.isOpen()) {
            String closeTribeMessage = messageFile.getString("tribes.invites.options.close");
            Utils.errorMessage(p, closeTribeMessage);
            return;
        }

        // Obtener el rango del jugador que realiza la acción
        Rank senderRank = senderTribe.getMember(p.getUniqueId()).getRange();

        // Verificar si el rango permite enviar invitaciones
        if (!senderRank.isCanInvite()) {
            String cantInviteMessage = messageFile.getString("tribes.invites.options.cant-invite");
            Utils.errorMessage(p, cantInviteMessage);
            return;
        }

        String playerName = args[1];
        placeholders.put(Placeholders.TARGET_NAME, playerName);
        Player targetPlayer = Bukkit.getPlayer(playerName);

        // Verificar si el jugador objetivo está en línea
        if (targetPlayer == null) {
            String offlineMessage = messageFile.getString("tribes.invites.options.offline");
            String finalMessage = Placeholders.replacePlaceholders(offlineMessage, placeholders);
            Utils.errorMessage(p, finalMessage);
            return;
        }

        // Verificar si el jugador intenta invitarse a sí mismo
        if (targetPlayer.getUniqueId().equals(p.getUniqueId())) {
            String selfMessage = messageFile.getString("tribes.invites.options.self");
            Utils.errorMessage(p, selfMessage);
            return;
        }

        // Verificar si el jugador objetivo ya es miembro de una tribu
        if (senderTribe.getMember(targetPlayer.getUniqueId()) != null) {
            String existMessage = messageFile.getString("tribes.invites.options.exist");
            Utils.errorMessage(p, existMessage);
            return;
        }

        if (InvitationsManager.hasInvitation(targetPlayer.getUniqueId(), senderTribe.getId())) {
            Utils.errorMessage(p, "Ya hay una invitación pendiente para este jugador.");
            return;
        }

        // Crear la invitación
        Invitation invitation = new Invitation(senderTribe.getId(), targetPlayer.getUniqueId());

        // Agregar al manager
        InvitationsManager.addInvitation(invitation);

        String clkAccept = messageFile.getString("general.clk-action.accept");
        String clkReject = messageFile.getString("general.clk-action.reject");
        String clkMessage = messageFile.getString("tribes.invites.target.invite");

        placeholders.put(Placeholders.TRIBE_NAME, senderTribe.getTribeName().replaceAll("_", " "));
        placeholders.put(Placeholders.CLK_ACCEPT, clkAccept);
        placeholders.put(Placeholders.CLK_REJECT, clkReject);
        placeholders.put(Placeholders.CLK_REJECT, clkReject);
        placeholders.put(Placeholders.PLUGIN_NAME, VitalCore.getPlugin().getName());

        String firstMessage = Placeholders.replacePlaceholders(Utils.INFO_PREFIX + clkMessage, placeholders);

        // Crear mensajes de aceptar y rechazar
        ClickableMessage acceptMessage = new ClickableMessage(clkAccept, "tribe accept " + senderTribe.getTribeName(),
                "&aClick para aceptar la invitación.");
        ClickableMessage rejectMessage = new ClickableMessage(clkReject, "tribe cancel " + senderTribe.getTribeName(),
                "&cClick para no aceptar la invitación.");

        // Enviar mensajes de confirmación y éxito
        Utils.sendConfirmationClickableMessage(targetPlayer, firstMessage, acceptMessage, rejectMessage);
        Utils.successMessage(p, "Se ha enviado la invitación a " + targetPlayer.getDisplayName());
    }

}
