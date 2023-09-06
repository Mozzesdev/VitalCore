package me.winflix.vitalcore.tribe.commands.members;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
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
    @SuppressWarnings("deprecation")
    public void perform(Player sender, String[] args) {
        // Validar la cantidad de argumentos
        if (args.length <= 2) {
            Utils.errorMessage(sender, "Syntax error: use " + getSyntax());
            return;
        }

        String targetName = args[1];

        // Obtener el jugador objetivo
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target == null) {
            Utils.errorMessage(sender, "El jugador objetivo no está en línea.");
            return;
        }

        // Validar si el jugador intenta promoverse a sí mismo
        if (target.getName().equalsIgnoreCase(sender.getName())) {
            Utils.errorMessage(sender, "No puedes promoverte a ti mismo.");
            return;
        }

        // Obtener información del jugador que ejecuta la promoción
        User senderDB = UsersCollection.getUserWithTribe(sender.getUniqueId());
        Tribe senderTribe = senderDB.getTribe();
        TribeMember senderMember = senderTribe.getMember(sender.getUniqueId());

        // Obtener el rango deseado
        Rank rank = senderTribe.getRanks().stream()
                .filter((r) -> r.matchesNameOrDisplayName(args[2]))
                .findFirst()
                .orElse(null);

        if (rank == null) {
            Utils.errorMessage(sender, "El rango seleccionado no está disponible en tu tribu.");
            return;
        }

        // Verificar si el jugador tiene permisos para promover
        if (!senderMember.getRange().canPromote()) {
            Utils.errorMessage(sender, "No tienes suficientes privilegios para promover a otros miembros de tu tribu.");
            return;
        }

        //Obtener el miembro a promover
        TribeMember targetMember = senderTribe.getMember(target.getUniqueId());

        if(targetMember == null){
            Utils.errorMessage(sender, "Ese jugador no se encuentra en tu tribu.");
            return;
        }

        // Crear mensajes para el menú de confirmación
        String confirmMessage = "&aPromover a " + target.getName();
        List<String> confirmLore = Collections.singletonList("&7Click para promover a " + target.getName() + "!");

        String cancelMessage = "&cCancelar promoción";
        List<String> cancelLore = Collections.singletonList("&7Click para cancelar la promoción!");

        String titleMenu = "&c¿Deseas Promover a " + target.getName();

        // Instanciar los mensajes
        ConfirmMessages confirmMessages = new ConfirmMessages(confirmMessage, confirmLore, cancelMessage, cancelLore);

        // Instanciar el menu de confirmacion
        ConfirmMenu confirmMenu = new ConfirmMenu(
                VitalCore.getPlayerMenuUtility(sender),
                VitalCore.fileManager.getMessagesFile().getConfig(),
                confirmMessages,
                titleMenu);

        // Accion despues de cerrar el menu de confirmacion
        confirmMenu.afterClose((confirmed) -> {
            if (confirmed) {
                targetMember.setRange(rank);
                senderTribe.replaceMember(target.getUniqueId(), targetMember);
                TribesCollection.saveTribe(senderTribe);
                Utils.successMessage(sender, "Has promovido correctamente a &b" + target.getName());
                if (target.isOnline()) {
                    Utils.successMessage(target.getPlayer(), "Has sido promovido al rango " + rank.getDisplayName());
                }
            } else {
                Utils.errorMessage(sender, "Has cancelado la promoción de " + target.getName());
            }
        });

        // Abrir el menu de confirmacion
        confirmMenu.open();
    }

}
