package me.winflix.vitalcore.structures.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.structures.models.Structure;
import me.winflix.vitalcore.structures.utils.StructureManager;

public class Get extends SubCommand {

    StructureManager structureManager;

    public Get(StructureManager structureManager) {
        super();
        this.structureManager = structureManager;
    }

    @Override
    public String getName() {
        return "get";
    }

    @Override
    public String getVariants() {
        return "r";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "This command save the home of your tribe.";
    }

    @Override
    public String getSyntax() {
        return "/str get [structure] [amount]";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {

        if (args.length == 2) {
            return structureManager.getAllStructures().stream().map(Structure::getId).collect(Collectors.toList());
        }

        if (args.length == 3) {
            return Arrays.asList("1", "64");
        }

        return Arrays.asList("");
    }

    @Override
    public void perform(Player p, String[] args) {
        if (args.length == 1) {
            Utils.errorMessage(p, "Syntax error: use " + getSyntax());
            return;
        }

        if (args.length >= 2) {
            String structureId = args[1];

            int amount = 1;

            if (args.length == 3) {
                amount = Integer.parseInt(args[2]);
            }

            Structure structure = structureManager.getAllStructures().stream()
                    .filter(str -> str.getId().equalsIgnoreCase(structureId))
                    .findFirst()
                    .orElse(null);

            if (structure != null) {
                ItemStack item = structure.getItemStack();
                item.setAmount(amount);

                Inventory inv = p.getInventory();
                if (inv.firstEmpty() == -1) {
                    p.getWorld().dropItemNaturally(p.getLocation(), item);
                    p.sendMessage(Utils.useColors("&cNo tienes espacio en el inventario, item dropeado"));
                } else {
                    inv.addItem(item);
                }
            }

        }
    }

}
