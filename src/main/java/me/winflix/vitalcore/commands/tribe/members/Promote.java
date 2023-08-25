package me.winflix.vitalcore.commands.tribe.members;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.commands.SubCommand;
import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.interfaces.ConfirmMessages;
import me.winflix.vitalcore.menu.ConfirmMenu;
import me.winflix.vitalcore.models.PlayerModel;
import me.winflix.vitalcore.models.TribeMember;
import me.winflix.vitalcore.models.TribeModel;
import me.winflix.vitalcore.utils.Utils;

public class Promote extends SubCommand {

    @Override
    public String getName() {
        return "promote";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getVariants() {
        return "pr";
    }

    @Override
    public String getDescription() {
        return "This command invite a player or players to your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/tribe promote <player> <rankname>";
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

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target.getDisplayName().equalsIgnoreCase(sender.getDisplayName())) {
            Utils.errorMessage(sender, "No puedes promoverte a ti mismo.");
            return;
        }

        PlayerModel senderDB = UserCollection.getPlayer(sender.getUniqueId());
        TribeModel senderTribe = senderDB.getTribe();
        TribeMember senderMember = senderTribe.getMember(sender.getUniqueId());

        if (!senderMember.getRange().canPromote()) {
            Utils.errorMessage(sender,
                    "No tienes el suficiente privilegio para promover a otros miembros de tu tribu.");
            return;
        }

        PlayerModel targetDB = UserCollection.getPlayer(target.getUniqueId());
        TribeModel targetTribe = targetDB.getTribe();

        String confirmMessage = "&aInvitar a" + target.getDisplayName();
        List<String> confirmLore = new ArrayList<String>();
        confirmLore.add("&7Click para invitar a" + target.getDisplayName() + "!");
        String cancelMessage = "&cCancelar invitacion";
        List<String> cancelLore = new ArrayList<String>();
        cancelLore.add("&7Click para cancelar la invitacion!");

        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage,
                cancelLore);

        ConfirmMenu confirmMenu = new ConfirmMenu(VitalCore.getPlayerMenuUtility(sender),
                VitalCore.fileManager.getMessagesFile().getConfig(), confirmMessages,
                "&c      ¿Deseas Promover a " + target.getDisplayName());
        confirmMenu.afterClose((confirmed) -> {
            if (confirmed) {

            }
        });
        confirmMenu.open();

    }

}
