package me.winflix.vitalcore.structures.models;

import java.util.Map;

import org.bukkit.Material;

public class Recipe {
    private Map<Character, Material> ingredients;
    private String[] shape;

    public Recipe(Map<Character, Material> ingredients, String[] shape) {
        this.ingredients = ingredients;
        this.shape = shape;
    }

    public Map<Character, Material> getIngredients() {
        return ingredients;
    }

    public String[] getShape() {
        return shape;
    }

    public void setIngredients(Map<Character, Material> ingredients) {
        this.ingredients = ingredients;
    }

    public void setShape(String[] shape) {
        this.shape = shape;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Recipe {\n");

        sb.append("  Ingredients:\n");
        for (Map.Entry<Character, Material> entry : ingredients.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        sb.append("  Shape:\n");
        for (String row : shape) {
            sb.append("    ").append(row).append("\n");
        }

        sb.append("}");

        return sb.toString();
    }
}
