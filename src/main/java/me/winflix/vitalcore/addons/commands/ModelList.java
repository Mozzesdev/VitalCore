package me.winflix.vitalcore.addons.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.addons.model.runtime.ModelEngineManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;

public class ModelList extends SubCommand {

    private final ModelEngineManager modelEngine;

    public ModelList(ModelEngineManager modelEngine) {
        this.modelEngine = modelEngine;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getVariants() {
        // Puedes añadir alias separados por "|" si quisieras: "list|ls"
        return "list";
    }

    @Override
    public String getDescription(Player p) {
        return "Lista todos los modelos disponibles";
    }

    @Override
    public String getPermission() {
        return "vitalcore.model.list";
    }

    @Override
    public String getSyntax(Player p) {
        return "/addons list";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        // Si quisieras autocompletar un segundo parámetro, devolvemos todos los nombres
        // de modelo
        Set<String> names = modelEngine.getModelNames();
        return new ArrayList<>(names);
    }

    @Override
    public void perform(Player player, String[] args) {
        Set<String> names = modelEngine.getModelNames();
        if (names.isEmpty()) {
            Utils.errorMessage(player, "No hay modelos disponibles.");
        } else {
            Utils.infoMessage(player, "Modelos disponibles: &r" + String.join(", ", names));
        }
    }
}