package me.winflix.vitalcore.skins.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Placeholders;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.skins.models.Skin;
import me.winflix.vitalcore.skins.utils.SkinGrabber;
import me.winflix.vitalcore.skins.utils.SkinManager;

public class Set extends SubCommand {

    private final SkinManager skinManager;

    public Set(SkinManager skinManager) {
        this.skinManager = skinManager;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getVariants() {
        return "s";
    }

    @Override
    public String getDescription(Player p) {
        return "Cambia tu skin";
    }

    @Override
    public String getPermission() {
        return "skin.command.set";
    }

    @Override
    public String getSyntax(Player p) {
        return "/skin set <nombre | uuid>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void perform(Player p, String[] args) {
        Map<String, String> placeholders = new HashMap<>();
        FileConfiguration messageFile = VitalCore.fileManager.getMessagesFile(p).getConfig();
        placeholders.put(Placeholders.COMMAND_SYNTAX, getSyntax(p));

        // Verificar la cantidad de argumentos
        if (args.length != 2) {
            String syntaxMessage = messageFile.getString("general.commands.syntax");
            String finalMessage = Placeholders.replacePlaceholders(syntaxMessage, placeholders);
            Utils.errorMessage(p, finalMessage);
            return;
        }

        Skin skin = SkinGrabber.changeSkin(p, args[1]);

        if (skin == null) {
            Utils.errorMessage(p, "Error al cambiar la skin. ¿El nombre/UUID es válido?");
            return;
        }

        skinManager.updateSkin(p, skin);
        Utils.successMessage(p, "&a¡Skin cambiada con éxito!");
    }
}