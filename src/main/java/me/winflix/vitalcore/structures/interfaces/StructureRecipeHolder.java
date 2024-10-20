package me.winflix.vitalcore.structures.interfaces;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import com.google.gson.annotations.Expose;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.structures.models.Recipe;
import me.winflix.vitalcore.structures.models.StructureItem;

public abstract class StructureRecipeHolder {
    protected ShapedRecipe shapedRecipe;
    @Expose
    protected Recipe rawRecipe;
    @Expose
    protected StructureItem item;
    @Expose
    protected boolean fullDrop = true;

    public Recipe getRawRecipe() {
        return rawRecipe;
    }

    public void setFullDrop(boolean fullDrop) {
        this.fullDrop = fullDrop;
    }

    public void setRawRecipe(Recipe rawRecipe) {
        this.rawRecipe = rawRecipe;
    }

    public void setItem(StructureItem item) {
        this.item = item;
    }

    public StructureItem getItem() {
        return item;
    }

    public ShapedRecipe getShapedRecipe() {
        return shapedRecipe;
    }

    public void setShapedRecipe(ShapedRecipe shapedRecipe) {
        this.shapedRecipe = shapedRecipe;
    }

    public void setShapedRecipeIngredients(String[] shape, Map<Character, Material> ingredients) {
        if (shapedRecipe == null) {
            VitalCore.Log.info("El valor de la receta de la estructura es null.");
            return;
        }
        shapedRecipe.shape(shape);

        for (Map.Entry<Character, Material> entry : ingredients.entrySet()) {
            shapedRecipe.setIngredient(entry.getKey(), entry.getValue());
        }
    }

    public boolean registerShapedRecipe() {
        if (shapedRecipe != null) {
            return Bukkit.addRecipe(shapedRecipe);
        }
        VitalCore.Log.info("El valor de la receta de la estructura es null.");
        return false;
    }

    public boolean requireFullDrop() {
        return fullDrop;
    }

    public abstract void dropItem();

    public abstract ItemStack getItemStack();

}
