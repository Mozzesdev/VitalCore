package me.winflix.vitalcore.structures.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.structures.files.StructureFile;
import me.winflix.vitalcore.structures.models.Structure;

public class StructureManager {

    private List<Structure> allStructures = new ArrayList<>();
    private StructureFile structuresFile = VitalCore.fileManager.getStructuresFile();
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Structure.class, new StructureTypeAdapter())
            .create();

    public StructureManager() {
        loadStructuresFromJson();
        registerRecipes();
    }

    private void loadStructuresFromJson() {
        File file = structuresFile.getFile();
        if (file != null && file.exists()) {
            try (FileReader reader = new FileReader(file)) {

                JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
                for (JsonElement element : jsonArray) {
                    Structure structure = gson.fromJson(element, Structure.class);
                    allStructures.add(structure);
                }

            } catch (IOException e) {
                VitalCore.Log.log(Level.SEVERE, "Error loading structures from JSON: " + e.getMessage());
            }
        } else {
            VitalCore.Log.log(Level.SEVERE, "Structures file not found or doesn't exist.");
        }
    }

    public List<Structure> getAllStructures() {
        return allStructures;
    }

    public static Structure fromJson(String json) {
        return gson.fromJson(json, Structure.class);
    }

    public static String toJson(Structure structure) {
        return gson.toJson(structure, Structure.class);
    }

    public void registerRecipes() {
        for (Structure structure : allStructures) {
            if (structure.getShapedRecipe() != null)
                structure.registerShapedRecipe();
        }
    }

    public static double getStructureBorderDistance(Material[][][] matriz) {
        // Comprobar si la matriz está vacía
        if (matriz.length == 0) {
            return 0;
        }

        // Inicializar las dimensiones
        int height = matriz.length; // Número de capas
        int maxWidth = 0; // Ancho máximo
        int maxDepth = 0; // Profundidad máxima

        // Calcular el ancho máximo y la profundidad máxima
        for (Material[][] layer : matriz) {
            int currentWidth = layer.length; // Ancho de la capa actual
            if (currentWidth > maxWidth) {
                maxWidth = currentWidth;
            }

            for (Material[] row : layer) {
                if (row.length > maxDepth) {
                    maxDepth = row.length; // Profundidad de la fila actual
                }
            }
        }

        // Calcular la distancia al borde
        double borderDistanceX = maxWidth / 2.0; // Distancia al borde en X
        double borderDistanceY = height / 2.0; // Distancia al borde en Y
        double borderDistanceZ = maxDepth / 2.0; // Distancia al borde en Z

        // Retornar la mayor distancia al borde
        return Math.max(borderDistanceX, Math.max(borderDistanceY, borderDistanceZ));
    }

}
