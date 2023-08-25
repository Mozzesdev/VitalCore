package me.winflix.vitalcore.commands.tribe.members;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.database.collections.TribeCollection;
import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.interfaces.ConfirmMessages;
import me.winflix.vitalcore.menu.ConfirmMenu;
import me.winflix.vitalcore.models.PlayerModel;
import me.winflix.vitalcore.models.PlayerRank;
import me.winflix.vitalcore.models.TribeMember;
import me.winflix.vitalcore.models.TribeModel;
import me.winflix.vitalcore.utils.Utils;

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
    public String getDescription() {
        return "This command kick a player or players to your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/tribe kick <player>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player sender, String[] args) {

        if (args.length <= 1) {
            Utils.errorMessage(sender, "Syntax error: use " + getSyntax());
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);

        if (targetPlayer.getDisplayName().equalsIgnoreCase(sender.getDisplayName())) {
            Utils.errorMessage(sender, "No puedes expulsarte a ti mismo, intenta con /tribe leave.");
            return;
        }

        PlayerModel senderDB = UserCollection.getPlayerWithTribe(sender.getUniqueId());
        TribeModel tribeDB = senderDB.getTribe();
        PlayerRank senderRange = tribeDB.getMember(sender.getUniqueId()).getRange();

        TribeMember member = tribeDB.getMember(targetPlayer.getUniqueId());

        if (!senderRange.canKick(member)) {
            Utils.errorMessage(sender,
                    "Solo puedes expulsar a jugadores teniendo un rango superior al expulsado.");
            return;
        }

        String confirmMessage = "&aInvitar a" + targetPlayer.getDisplayName();
        List<String> confirmLore = new ArrayList<String>();
        confirmLore.add("&7Click para invitar a" + targetPlayer.getDisplayName() + "!");
        String cancelMessage = "&cCancelar invitacion";
        List<String> cancelLore = new ArrayList<String>();
        cancelLore.add("&7Click para cancelar la invitacion!");

        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage,
                cancelLore);

        ConfirmMenu confirmMenu = new ConfirmMenu(VitalCore.getPlayerMenuUtility(sender),
                VitalCore.fileManager.getMessagesFile().getConfig(), confirmMessages, "");

        confirmMenu.afterClose((condition) -> {
            if (condition) {
                tribeDB.removeMember(member);
                TribeCollection.saveTribe(tribeDB);

                TribeModel t = TribeCollection.createTribe(targetPlayer);
                PlayerModel playerDB = UserCollection.getPlayer(targetPlayer.getUniqueId());
                playerDB.setTribeId(t.getId());
                UserCollection.savePlayer(playerDB);

                String kickSenderMessage = "Se ha expulsado correctamente a" + targetPlayer.getDisplayName();
                String kickTargetMessage = "Te han expulsado de la tribu de &6" + tribeDB.getTribeName();
                Utils.successMessage(sender, kickSenderMessage);
                Utils.errorMessage(targetPlayer, kickTargetMessage);
            }
        });

        confirmMenu.open();

    }
}
