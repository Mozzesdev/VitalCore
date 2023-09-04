package me.winflix.vitalcore.tribe.commands.members;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMenu;
import me.winflix.vitalcore.general.menu.confirm.ConfirmMessages;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.Rank;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.TribeMember;
import me.winflix.vitalcore.tribe.models.User;

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
        // Validar la cantidad de argumentos
        if (args.length <= 2) {
            Utils.errorMessage(sender, "Syntax error: use " + getSyntax());
            return;
        }

        // Obtener el jugador objetivo
        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            Utils.errorMessage(sender, "El jugador objetivo no está en línea.");
            return;
        }

        // Validar si el jugador intenta promoverse a sí mismo
        if (target.getDisplayName().equalsIgnoreCase(sender.getDisplayName())) {
            Utils.errorMessage(sender, "No puedes promoverte a ti mismo.");
            return;
        }

        // Obtener información del jugador que ejecuta la promoción
        User senderDB = UsersCollection.getUserWithTribe(sender.getUniqueId());
        Tribe senderTribe = senderDB.getTribe();
        TribeMember senderMember = senderTribe.getMember(sender.getUniqueId());

        // Obtener el rango deseado
        Rank rank = senderTribe.getRanks().stream()
                .filter((r) -> r.getName().equalsIgnoreCase(args[2]) || r.getDisplayName().equalsIgnoreCase(args[2]))
                .findFirst()
                .orElse(null);

        if (rank == null) {
            Utils.errorMessage(target, "El rango seleccionado no está disponible en tu tribu.");
            return;
        }

        // Verificar si el jugador tiene permisos para promover
        if (!senderMember.getRange().canPromote()) {
            Utils.errorMessage(sender, "No tienes suficientes privilegios para promover a otros miembros de tu tribu.");
            return;
        }

        // Obtener información del jugador objetivo
        User targetDB = UsersCollection.getUserWithTribe(target.getUniqueId());
        Tribe targetTribe = targetDB.getTribe();

        // Crear mensajes para el menú de confirmación
        StringBuilder confirmMessage = new StringBuilder();
        confirmMessage.append("&aPromover a ").append(target.getDisplayName());

        List<String> confirmLore = new ArrayList<>();
        confirmLore.add("&7Click para promover a" + target.getDisplayName() + "!");

        StringBuilder cancelMessage = new StringBuilder();
        cancelMessage.append("&cCancelar promoción");

        List<String> cancelLore = new ArrayList<>();
        cancelLore.add("&7Click para cancelar la promoción!");

        String titleMenu = "&c¿Deseas Promover a " + target.getDisplayName();

        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage.toString(), confirmLore,
                cancelMessage.toString(), cancelLore);

        ConfirmMenu confirmMenu = new ConfirmMenu(VitalCore.getPlayerMenuUtility(sender),
                VitalCore.fileManager.getMessagesFile().getConfig(), confirmMessages, titleMenu);

        confirmMenu.afterClose((confirmed) -> {
            if (confirmed) {
                TribeMember targetMember = targetTribe.getMember(target.getUniqueId());
                targetMember.setRange(rank);
                Utils.successMessage(sender, "Has promovido correctamente a &b" + target.getDisplayName());
                Utils.successMessage(target, "Has sido promovido al rango " + rank.getDisplayName());
                return;
            }
            Utils.errorMessage(sender, "Has cancelado la promoción de " + target.getDisplayName());
        });

        confirmMenu.open();
    }

}
