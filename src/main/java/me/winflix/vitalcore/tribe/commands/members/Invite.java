package me.winflix.vitalcore.tribe.commands.members;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.interfaces.ConfirmMessages;
import me.winflix.vitalcore.general.interfaces.ConfirmationHandler;
import me.winflix.vitalcore.general.menu.ConfirmMenu;
import me.winflix.vitalcore.general.utils.ConfirmationConversation;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.Rank;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.TribeMember;
import me.winflix.vitalcore.tribe.models.User;
import me.winflix.vitalcore.tribe.utils.RankManager;
import me.winflix.vitalcore.tribe.utils.TribeUtils;

public class Invite extends SubCommand {

    private VitalCore plugin;

    public Invite(VitalCore plugin) {
        this.plugin = plugin;
    }

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
        return "in";
    }

    @Override
    public String getDescription() {
        return "This command invite a player or players to your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/tribe invite <player> | <players>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        if (args.length != 2) {
            Utils.errorMessage(p, "Syntax error: use " + getSyntax());
            return;
        }

        User senderPlayer = UsersCollection.getUserWithTribe(p.getUniqueId());
        Tribe senderTribe = senderPlayer.getTribe();

        if (!senderTribe.isOpen()) {
            Utils.errorMessage(p, "La tribu actualmente está cerrada a nuevos miembros.");
            return;
        }

        Rank senderRank = senderTribe.getMember(p.getUniqueId()).getRange();

        if (!senderRank.isCanInvite()) {
            Utils.errorMessage(p, "No tienes permitido invitar a nuevos miembros.");
            return;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null) {
            Utils.errorMessage(p, "El jugador " + playerName + " no está en línea.");
            return;
        }

        handleOnlineInvitation(p, targetPlayer, senderTribe);
    }

    private void handleOnlineInvitation(Player inviter, Player targetPlayer, Tribe senderTribe) {
        if (targetPlayer.equals(inviter)) {
            Utils.errorMessage(inviter, "No puedes invitarte a ti mismo.");
        } else if (senderTribe.getMember(targetPlayer.getUniqueId()) == null) {
            sendInvitationConfirmation(inviter, targetPlayer, senderTribe);
        } else {
            Utils.errorMessage(inviter,
                    "El jugador &b" + targetPlayer.getDisplayName() + " &cya se encuentra en tu tribu.");
        }
    }

    private void sendInvitationConfirmation(Player inviter, Player targetPlayer, Tribe senderTribe) {
        ConfirmationHandler customHandler = (s, r, confirmed) -> {
            if (confirmed) {

                String confirmMessage = "&aInvitar a" + targetPlayer.getDisplayName();
                List<String> confirmLore = new ArrayList<String>();
                confirmLore.add("&7Click para invitar a" + targetPlayer.getDisplayName() + "!");
                String cancelMessage = "&cCancelar invitacion";
                List<String> cancelLore = new ArrayList<String>();
                cancelLore.add("&7Click para cancelar la invitacion!");

                ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage,
                        cancelLore);
                ConfirmMenu confirmMenu = new ConfirmMenu(VitalCore.getPlayerMenuUtility(r),
                        VitalCore.fileManager.getMessagesFile().getConfig(), confirmMessages, "");
                confirmMenu.afterClose((condition) -> {
                    if (condition) {
                        User recieverPlayer = UsersCollection.getUserWithTribe(r.getUniqueId());
                        Tribe recieverTribe = recieverPlayer.getTribe();

                        TribeMember recieverMember = recieverTribe
                                .getMember(r.getUniqueId());

                        if (recieverMember != null) {
                            if (recieverTribe.getMembers().size() > 1) {
                                if (recieverMember.getRange().getName().equals(RankManager.OWNER_RANK.getName())) {
                                    TribeMember newOwner = recieverTribe.getDiferentMember(r.getUniqueId());
                                    newOwner.setRange(RankManager.OWNER_RANK);
                                    recieverTribe.replaceMember(UUID.fromString(newOwner.getId()), newOwner);
                                    TribesCollection.saveTribe(recieverTribe);
                                }
                                recieverTribe.removeMember(recieverMember);
                            } else {
                                TribesCollection.deleteTribe(recieverTribe);
                            }
                            recieverMember.setRange(RankManager.MEMBER_RANK);
                            senderTribe.addMember(recieverMember);
                            TribesCollection.saveTribe(senderTribe);

                            recieverPlayer.setTribeId(senderTribe.getId());
                            recieverPlayer.setTribe(null);
                            UsersCollection.saveUser(recieverPlayer);

                            Utils.successMessage(r,
                                    "&cTe has unido a la tribu de &6"
                                            + senderTribe.getTribeName().replaceAll("-", " "));
                            TribeUtils.castMessageToAllMembersTribe(senderTribe,
                                    "Se ha añadido a la tribu a: " + r.getDisplayName());
                        }
                    } else {
                        Utils.errorMessage(s, "Han rechazado tu invitacion");
                        Utils.errorMessage(r, "Has rechazado la invitacion");
                    }
                });
                confirmMenu.open();
            } else {
                Utils.errorMessage(s, "Se ha cancelado la conversacion");
                Utils.errorMessage(r, "Has cancelado la conversacion");
            }
        };

        ConfirmationConversation confirmation = new ConfirmationConversation(inviter, targetPlayer, plugin,
                VitalCore.fileManager.getMessagesFile().getConfig(), customHandler);
        confirmation.start();
    }
}
