package me.winflix.vitalcore.structures.models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.annotations.Expose;

import me.winflix.vitalcore.general.utils.Utils;

public class StructureItem {
    @Expose
    private String type;
    @Expose
    private String displayName;
    @Expose
    private List<String> lore;
    @Expose
    private String id;

    public StructureItem(String type, String displayName, List<String> lore) {
        this.type = type;
        this.displayName = displayName;
        this.lore = lore;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(Material.valueOf(type.toUpperCase()));
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(Utils.useColors(displayName));

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(Utils.useColors(line));
        }
        itemMeta.setLore(coloredLore);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StructureItem {")
                .append("\n  Type: ").append(type)
                .append("\n  Display Name: ").append(displayName)
                .append("\n  Lore: ");

        if (lore != null && !lore.isEmpty()) {
            sb.append("\n    ");
            for (String line : lore) {
                sb.append(line).append("\n    ");
            }
        } else {
            sb.append("null");
        }

        sb.append("\n}");
        return sb.toString();
    }

}
