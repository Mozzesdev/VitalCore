package me.winflix.vitalcore.commands.tribe.members;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.database.collections.TribeCollection;
import me.winflix.vitalcore.database.collections.UserCollection;
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
    public void perform(Player sender, String[] args) {

        if (args.length <= 1) {
            Utils.logMessage(sender, "&cSyntax error: use " + getSyntax());
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);

        if (targetPlayer.getDisplayName().equalsIgnoreCase(sender.getDisplayName())) {
            Utils.logMessage(sender, "&cNo puedes expulsarte a ti mismo, intenta con /tribe leave.");
            return;
        }

        PlayerModel senderDB = UserCollection.getPlayerWithTribe(sender.getUniqueId());
        TribeModel tribeDB = senderDB.getTribe();
        PlayerRank senderRange = tribeDB.getMember(sender.getUniqueId()).getRange();

        TribeMember member = tribeDB.getMember(targetPlayer.getUniqueId());

        if (!senderRange.canKick(member)) {
            Utils.logMessage(sender,
                    "&cSolo puedes expulsar a jugadores teniendo un rango superior al expulsado.");
            return;
        }

        ConfirmMenu confirmMenu = new ConfirmMenu(VitalCore.getPlayerMenuUtility(sender),
                VitalCore.getMessagesConfigManager().getConfig());

        confirmMenu.afterClose((condition) -> {
            if (condition) {
                tribeDB.removeMember(member);
                TribeCollection.saveTribe(tribeDB);

                TribeModel t = TribeCollection.createTribe(targetPlayer);
                PlayerModel playerDB = UserCollection.getPlayer(targetPlayer.getUniqueId());
                playerDB.setTribeId(t.getId());
                UserCollection.savePlayer(playerDB);

                Utils.logMessage(targetPlayer, "&cTe han expulsado de la tribu de &6" + tribeDB.getTribeName());
            }

            String onlineMessage = "&aSe ha expulsado correctamente a" + targetPlayer.getDisplayName();
            Utils.logMessage(sender, onlineMessage);

        });

        confirmMenu.open();

    }
}
