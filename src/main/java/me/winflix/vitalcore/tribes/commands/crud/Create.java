package me.winflix.vitalcore.tribes.commands.crud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.database.collections.tribe.TribesDAO;
import me.winflix.vitalcore.general.database.collections.tribe.UsersDAO;
import me.winflix.vitalcore.general.utils.Placeholders;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribes.models.Tribe;
import me.winflix.vitalcore.tribes.models.User;

public class Create extends SubCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getVariants() {
        return "crt";
    }

    @Override
    public String getDescription(Player p) {
        return VitalCore.fileManager.getMessagesFile(p).getConfig().getString("tribes.create.cmd.desc");
    }

    @Override
    public String getSyntax(Player p) {
        return VitalCore.fileManager.getMessagesFile(p).getConfig().getString("tribes.create.cmd.usage");
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player p, String[] args) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(Placeholders.COMMAND_SYNTAX, getSyntax(p));

        if (args.length < 2) {
            String syntaxMessage = VitalCore.fileManager.getMessagesFile(p).getConfig()
                    .getString("general.commands.syntax");
            String finalMessage = Placeholders.replacePlaceholders(syntaxMessage, placeholders);
            Utils.errorMessage(p, finalMessage);
            return;
        }

        User user = UsersDAO.getUser(p.getUniqueId());

        if (user.getTribe() != null) {
            Utils.errorMessage(p,
                    VitalCore.fileManager.getMessagesFile(p).getConfig().getString("tribes.create.already_in_tribe"));
            return;
        }

        String tribeName = args[1];
        String tribeTag = args.length >= 3 ? args[2] : "";

        if (!isValidTribeName(tribeName)) {
            Utils.errorMessage(p,
                    VitalCore.fileManager.getMessagesFile(p).getConfig().getString("tribes.create.invalid_name")
                            .replace("{min}", "3")
                            .replace("{max}", "16"));
            return;
        }

        if (!tribeTag.isEmpty() && !isValidTag(tribeTag)) {
            Utils.errorMessage(p,
                    VitalCore.fileManager.getMessagesFile(p).getConfig().getString("tribes.create.invalid_tag")
                            .replace("{min}", "2")
                            .replace("{max}", "5"));
            return;
        }

        // Verificar nombre único
        if (TribesDAO.getTribeByName(tribeName) != null) {
            Utils.errorMessage(p,
                    VitalCore.fileManager.getMessagesFile(p).getConfig().getString("tribes.create.name_taken"));
            return;
        }

        try {
            Tribe newTribe = TribesDAO.createTribe(p, tribeName, tribeTag);

            user.setTribe(newTribe);
            UsersDAO.saveUser(user);

            Utils.successMessage(p,
                    VitalCore.fileManager.getMessagesFile(p).getConfig().getString("tribes.create.success")
                            .replace("{tribe_name}", tribeName));

        } catch (Exception e) {
            VitalCore.Log.log(Level.SEVERE, "Error creating tribe", e);
            Utils.errorMessage(p,
                    VitalCore.fileManager.getMessagesFile(p).getConfig().getString("tribes.create.error"));
        }
    }

    private boolean isValidTribeName(String name) {
        return name.matches("^[a-zA-Z0-9_]{3,16}$");
    }

    private boolean isValidTag(String tag) {
        return tag.matches("^[a-zA-Z0-9]{2,5}$");
    }

}
