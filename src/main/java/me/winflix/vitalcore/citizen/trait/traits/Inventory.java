package me.winflix.vitalcore.citizen.trait.traits;

import me.winflix.vitalcore.citizen.interfaces.Trait;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

@TraitName("inventory")
public class Inventory extends Trait {
    private ItemStack[] contents;
    private boolean registeredListener;
    private org.bukkit.inventory.Inventory view;
    private final Set<InventoryView> viewers = new HashSet<InventoryView>();

    public Inventory() {
        super("inventory");
        contents = new ItemStack[72];
    }
}
