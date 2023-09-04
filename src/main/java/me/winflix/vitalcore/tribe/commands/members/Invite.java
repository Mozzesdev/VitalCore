package me.winflix.vitalcore.tribe.commands.members;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMenu;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMessages;
import me.winflix.vitalcore.general.utils.ClickableMessage;
import me.winflix.vitalcore.general.utils.Placeholders;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.Rank;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.TribeMember;
import me.winflix.vitalcore.tribe.models.User;
import me.winflix.vitalcore.tribe.utils.RankManager;
import me.winflix.vitalcore.tribe.utils.TribeUtils;

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
        return "in";
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

        if (args.length != 2) {
            String syntaxMessage = messageFile.getString("tribes.commands.syntax");
            String finalMessage = Placeholders.replacePlaceholders(syntaxMessage, placeholders);
            Utils.errorMessage(p, finalMessage);
            return;
        }

        User senderPlayer = UsersCollection.getUserWithTribe(p.getUniqueId());
        Tribe senderTribe = senderPlayer.getTribe();
        placeholders.put(Placeholders.OFF_TARGET_NAME, senderPlayer.getPlayerName());

        if (!senderTribe.isOpen()) {
            String closeTribeMessage = messageFile.getString("tribes.invites.options.close");
            Utils.errorMessage(p, closeTribeMessage);
            return;
        }

        Rank senderRank = senderTribe.getMember(p.getUniqueId()).getRange();

        if (!senderRank.isCanInvite()) {
            String cantInviteMessage = messageFile.getString("tribes.invites.options.cant-invite");
            Utils.errorMessage(p, cantInviteMessage);
            return;
        }

        String playerName = args[1];
        placeholders.put(Placeholders.TARGET_NAME, playerName);
        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null) {
            String offlineMessage = messageFile.getString("tribes.invites.options.offline");
            String finalMessage = Placeholders.replacePlaceholders(offlineMessage, placeholders);
            Utils.errorMessage(p, finalMessage);
            return;
        }

        if (targetPlayer.getUniqueId().equals(p.getUniqueId())) {
            String selfMessage = messageFile.getString("tribes.invites.options.self");
            Utils.errorMessage(p, selfMessage);
            return;
        }

        if (senderTribe.getMember(targetPlayer.getUniqueId()) != null) {
            String existMessage = messageFile.getString("tribes.invites.options.exist");
            Utils.errorMessage(p, existMessage);
            return;
        }

        placeholders.put(Placeholders.TRIBE_NAME, senderTribe.getTribeName());

        ClickableMessage preMessage = new ClickableMessage("Dale click al mensaje para abrir el menu de tribus -> ",
                null,
                null,
                null);
        ClickableMessage tribeMenu = new ClickableMessage("/tribe", "tribe",
                "tribe command",
                null);

        Utils.sendClickableAction(targetPlayer, preMessage, tribeMenu);

        // onConfirmMessageInvitation(p, targetPlayer, senderTribe);

    }

    private void onConfirmMessageInvitation(Player inviter, Player targetPlayer, Tribe senderTribe) {
        FileConfiguration messagesFile = VitalCore.fileManager.getMessagesFile().getConfig();

        String menuTitle = messagesFile.getString("tribes.invites.menu.title");
        String confirmMessage = messagesFile.getString("tribes.invites.menu.accept");
        List<String> confirmLore = messagesFile.getStringList("tribes.invites.menu.accept.lore");
        String cancelMessage = messagesFile.getString("tribes.invites.menu.reject");
        List<String> cancelLore = messagesFile.getStringList("tribes.invites.menu.reject.lore");

        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage,
                cancelLore);
        ConfirmMenu confirmMenu = new ConfirmMenu(VitalCore.getPlayerMenuUtility(targetPlayer),
                messagesFile, confirmMessages, menuTitle);

        confirmMenu.afterClose((condition) -> {
            if (condition) {
                User recieverPlayer = UsersCollection.getUserWithTribe(targetPlayer.getUniqueId());
                Tribe recieverTribe = recieverPlayer.getTribe();

                TribeMember recieverMember = recieverTribe
                        .getMember(targetPlayer.getUniqueId());

                if (recieverMember != null) {
                    if (recieverTribe.getMembers().size() > 1) {
                        if (recieverMember.getRange().getName().equals(RankManager.OWNER_RANK.getName())) {
                            TribeMember newOwner = recieverTribe.getDiferentMember(targetPlayer.getUniqueId());
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

                    Utils.successMessage(targetPlayer,
                            "&cTe has unido a la tribu de &6"
                                    + senderTribe.getTribeName().replaceAll("-", " "));
                    TribeUtils.castMessageToAllMembersTribe(senderTribe,
                            "Se ha añadido a la tribu a: " + targetPlayer.getDisplayName());
                }
            } else {
                Utils.errorMessage(inviter, "Han rechazado tu invitacion");
                Utils.errorMessage(targetPlayer, "Has rechazado la invitacion");
            }
        });
        confirmMenu.open();
    }
}
