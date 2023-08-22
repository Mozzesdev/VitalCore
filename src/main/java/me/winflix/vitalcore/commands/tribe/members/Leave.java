package me.winflix.vitalcore.commands.tribe.members;

import java.util.UUID;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.database.collections.TribeCollection;
import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.menu.ConfirmMenu;
import me.winflix.vitalcore.models.PlayerModel;
import me.winflix.vitalcore.models.TribeMember;
import me.winflix.vitalcore.models.TribeModel;
import me.winflix.vitalcore.utils.RankManager;
import me.winflix.vitalcore.utils.Utils;

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
    public void perform(Player p, String[] args) {
        PlayerModel playerDB = UserCollection.getPlayerWithTribe(p.getUniqueId());
        TribeModel tribeDB = playerDB.getTribe();

        if (tribeDB.getMembers().size() <= 1) {
            p.sendMessage(Utils.useColors("&cNo puedes salirte de tu tribu si solo estas tu."));
            return;
        }

        ConfirmMenu confirmMenu = new ConfirmMenu(VitalCore.getPlayerMenuUtility(p),
                VitalCore.getMessagesConfigManager().getConfig());
        confirmMenu.afterClose((condition) -> {
            if (condition) {
                TribeMember member = tribeDB.getMember(p.getUniqueId());

                if (member.getRange() == RankManager.OWNER_RANK) {
                    TribeMember newOwner = tribeDB.getDiferentMember(p.getUniqueId());
                    newOwner.setRange(RankManager.OWNER_RANK);
                    tribeDB.replaceMember(UUID.fromString(newOwner.getId()), newOwner);
                }

                tribeDB.removeMember(member);
                TribeCollection.saveTribe(tribeDB);

                TribeModel t = TribeCollection.createTribe(p);
                playerDB.setTribeId(t.getId());
                UserCollection.savePlayer(playerDB);

                p.sendMessage(Utils.useColors("&bHas salido correctamente de tu tribu y se creo tu nueva tribu"));
            }
        });

        confirmMenu.open();

    }
}