package me.winflix.vitalcore.commands.tribe.members;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.database.collections.TribeCollection;
import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.events.ConfirmationConversation;
import me.winflix.vitalcore.interfaces.ConfirmationHandler;
import me.winflix.vitalcore.menu.ConfirmMenu;
import me.winflix.vitalcore.models.PlayerModel;
import me.winflix.vitalcore.models.PlayerRank;
import me.winflix.vitalcore.models.TribeMember;
import me.winflix.vitalcore.models.TribeModel;
import me.winflix.vitalcore.utils.RankManager;
import me.winflix.vitalcore.utils.Utils;

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
    public void perform(Player p, String[] args) {
        if (args.length <= 1) {
            p.sendMessage(Utils.useColors("&cSyntax error: use " + getSyntax()));
            return;
        }
        PlayerModel senderPlayer = UserCollection.getPlayerWithTribe(p.getUniqueId());
        TribeModel senderTribe = senderPlayer.getTribe();

        if (!senderTribe.isOpen()) {
            Utils.logMessage(p, "&cLa tribu actualmente esta cerrada a nuevos miembros.");
            return;
        }

        PlayerRank senderRank = senderTribe.getMember(p.getUniqueId()).getRange();

        if (!senderRank.isCanInvite()) {
            Utils.logMessage(p, "&cNo tienes permitido invitar a nuevos miembros.");
            return;
        }

        List<String> onlinePlayers = new ArrayList<>();
        List<String> offlinePlayers = new ArrayList<>();

        for (int i = 1; i < args.length; i++) {
            String playerName = args[i];
            Player targetPlayer = Bukkit.getPlayer(playerName);

            if (targetPlayer != null) {
                handleOnlineInvitation(p, targetPlayer, onlinePlayers, senderTribe);
            } else {
                offlinePlayers.add(playerName);
            }
        }

        sendOnlineInvitationMessage(p, onlinePlayers);
        sendOfflineInvitationMessage(p, offlinePlayers);
    }

    private void handleOnlineInvitation(Player inviter, Player targetPlayer, List<String> onlinePlayers,
            TribeModel senderTribe) {
        if (targetPlayer.equals(inviter)) {
            inviter.sendMessage(Utils.useColors("&cNo puedes invitarte a ti mismo."));
        } else if (senderTribe.getMember(targetPlayer.getUniqueId()) == null) {
            sendInvitationConfirmation(inviter, targetPlayer, senderTribe);
            onlinePlayers.add(targetPlayer.getName());
        } else {
            inviter.sendMessage(
                    Utils.useColors("&cEl jugador " + targetPlayer.getDisplayName() + " ya se encuentra en tu tribu."));
        }
    }

    private void sendInvitationConfirmation(Player inviter, Player targetPlayer, TribeModel senderTribe) {
        ConfirmationHandler customHandler = (s, r, confirmed) -> {
            if (confirmed) {
                ConfirmMenu confirmMenu = new ConfirmMenu(VitalCore.getPlayerMenuUtility(r),
                        VitalCore.getMessagesConfigManager().getConfig());
                confirmMenu.afterClose((condition) -> {
                    if (condition) {
                        PlayerModel recieverPlayer = UserCollection.getPlayerWithTribe(r.getUniqueId());
                        TribeModel recieverTribe = recieverPlayer.getTribe();

                        TribeMember recieverMember = recieverTribe
                                .getMember(r.getUniqueId());

                        if (recieverMember != null) {
                            if (recieverTribe.getMembers().size() > 1) {
                                if (recieverMember.getRange().isCanInvite()) {
                                    TribeMember newOwner = recieverTribe.getDiferentMember(r.getUniqueId());
                                    newOwner.setRange(RankManager.OWNER_RANK);
                                    recieverTribe.replaceMember(UUID.fromString(newOwner.getId()), newOwner);
                                    TribeCollection.saveTribe(recieverTribe);
                                }
                                recieverTribe.removeMember(recieverMember);
                            } else {
                                TribeCollection.deleteTribe(recieverTribe);
                            }
                            recieverMember.setRange(RankManager.MEMBER_RANK);
                            senderTribe.addMember(recieverMember);
                            TribeCollection.saveTribe(senderTribe);

                            recieverPlayer.setTribeId(senderTribe.getId());
                            UserCollection.savePlayer(recieverPlayer);

                            Utils.logMessage(r,
                                    "&cTe has unido a la tribu de &6"
                                            + senderTribe.getTribeName().replaceAll("-", " "));
                            Utils.castMessageToAllMembersTribe(senderTribe,
                                    "Se ha añadido a la tribu a: " + r.getDisplayName());
                        }
                    } else {
                        s.sendMessage("Han rechazado tu invitacion");
                        r.sendMessage("Has rechazado la invitacion");
                    }
                });
                confirmMenu.open();
            } else {
                s.sendMessage("Se ha cancelado la conversacion");
                r.sendMessage("Has cancelado la conversacion");
            }
        };

        ConfirmationConversation confirmation = new ConfirmationConversation(inviter, targetPlayer, plugin,
                VitalCore.getMessagesConfigManager().getConfig(), customHandler);
        confirmation.start();
    }

    private void sendOnlineInvitationMessage(Player inviter, List<String> onlinePlayers) {
        if (!onlinePlayers.isEmpty()) {
            String onlineMessage = "Se envió la invitación a: " + String.join(", ", onlinePlayers);
            inviter.sendMessage(Utils.useColors(onlineMessage));
        }
    }

    private void sendOfflineInvitationMessage(Player inviter, List<String> offlinePlayers) {
        if (!offlinePlayers.isEmpty()) {
            String offlineMessage = "Estos jugadores no están en línea: " + String.join(", ", offlinePlayers);
            inviter.sendMessage(Utils.useColors(offlineMessage));
        }
    }
}
