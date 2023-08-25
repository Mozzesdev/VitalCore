package me.winflix.vitalcore.commands.tribe.members;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.database.collections.TribeCollection;
import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.interfaces.ConfirmMessages;
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
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        PlayerModel playerDB = UserCollection.getPlayerWithTribe(p.getUniqueId());
        TribeModel tribeDB = playerDB.getTribe();

        if (tribeDB.getMembers().size() <= 1) {
            Utils.errorMessage(p, "No puedes salirte de tu tribu si solo estas tu.");
            return;
        }

        String confirmMessage = "&aInvitar a";
        List<String> confirmLore = new ArrayList<String>();
        confirmLore.add("&7Click para invitar a");
        String cancelMessage = "&cCancelar invitacion";
        List<String> cancelLore = new ArrayList<String>();
        cancelLore.add("&7Click para cancelar la invitacion!");

        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage,
                cancelLore);

        ConfirmMenu confirmMenu = new ConfirmMenu(VitalCore.getPlayerMenuUtility(p),
                VitalCore.fileManager.getMessagesFile().getConfig(), confirmMessages, "");
        confirmMenu.afterClose((condition) -> {
            if (condition) {
                TribeMember member = tribeDB.getMember(p.getUniqueId());

                if (member.getRange().getName().equals(RankManager.OWNER_RANK.getName())) {
                    TribeMember newOwner = tribeDB.getDiferentMember(p.getUniqueId());
                    newOwner.setRange(RankManager.OWNER_RANK);
                    tribeDB.replaceMember(UUID.fromString(newOwner.getId()), newOwner);
                }

                tribeDB.removeMember(member);
                TribeCollection.saveTribe(tribeDB);

                TribeModel t = TribeCollection.createTribe(p);
                playerDB.setTribeId(t.getId());
                playerDB.setTribe(null);
                UserCollection.savePlayer(playerDB);

                Utils.successMessage(p, "Has salido correctamente de tu tribu y se creo tu nueva tribu.");
            }
        });

        confirmMenu.open();

    }
}