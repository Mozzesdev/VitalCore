package me.winflix.vitalcore.tribe.commands.members;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.interfaces.ConfirmMessages;
import me.winflix.vitalcore.general.menu.ConfirmMenu;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.TribeMember;
import me.winflix.vitalcore.tribe.models.User;
import me.winflix.vitalcore.tribe.utils.RankManager;

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
        User playerDB = UsersCollection.getUserWithTribe(p.getUniqueId());
        Tribe tribeDB = playerDB.getTribe();

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
                TribesCollection.saveTribe(tribeDB);

                Tribe t = TribesCollection.createTribe(p);
                playerDB.setTribeId(t.getId());
                playerDB.setTribe(null);
                UsersCollection.saveUser(playerDB);

                Utils.successMessage(p, "Has salido correctamente de tu tribu y se creo tu nueva tribu.");
            }
        });

        confirmMenu.open();

    }
}