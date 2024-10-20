package me.winflix.vitalcore.structures.utils;

import me.winflix.vitalcore.structures.interfaces.StructuresType;
import me.winflix.vitalcore.structures.models.Facing;
import me.winflix.vitalcore.structures.models.Foundation;
import me.winflix.vitalcore.structures.models.Recipe;
import me.winflix.vitalcore.structures.models.Structure;
import me.winflix.vitalcore.structures.models.StructureItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class StructureTypeAdapter extends TypeAdapter<Structure> {

    @Override
    public void write(JsonWriter out, Structure structure) throws IOException {
        out.beginObject(); // Inicia un objeto JSON

        // Escribe los atributos de la clase Structure
        out.name("name").value(structure.getName()); // Acceso a nombre directamente, ya que es protegido
        out.name("type").value(structure.getType().toString()); // Obtiene el tipo de estructura
        out.name("health").value(structure.getHealth());
        out.name("id").value(structure.getId());
        out.name("fullDrop").value(structure.requireFullDrop());
        out.name("buildTime").value(structure.getBuildTime());
        if (structure.getFace() != null) {
            out.name("face").value(structure.getFace().toString());
        }

        // Escribiendo la matriz de materiales
        out.name("matriz");
        out.beginArray(); // Inicia un arreglo para la matriz
        for (Material[][] layer : structure.getMatriz()) {
            out.beginArray(); // Inicia un arreglo para cada capa de la matriz
            for (Material[] row : layer) {
                out.beginArray(); // Inicia un arreglo para cada fila
                for (Material material : row) {
                    if (material != null) {
                        out.value(material.toString()); // Escribe el material si no es null
                    } else {
                        out.nullValue(); // Escribe un valor null para materiales null
                    }
                }
                out.endArray(); // Finaliza la fila
            }
            out.endArray(); // Finaliza la capa
        }
        out.endArray(); // Finaliza el arreglo de la matriz

        // Escribiendo las posiciones de los bloques
        out.name("blockPositions");
        out.beginArray(); // Inicia un arreglo para las posiciones de los bloques
        for (StructureBlockPosition blockPosition : structure.getBlockPositions()) {
            out.beginObject(); // Inicia un objeto JSON para cada bloque

            // Serializa la ubicación como un objeto JSON
            Map<String, Object> locationData = blockPosition.getLocation().serialize();
            out.name("location");
            out.beginObject(); // Inicia un objeto JSON para la ubicación
            for (Map.Entry<String, Object> entry : locationData.entrySet()) {
                out.name(entry.getKey()).value(entry.getValue().toString()); // Escribe cada entrada del mapa
            }
            out.endObject(); // Finaliza el objeto de ubicación

            // Escribe el material y la distancia al centro
            out.name("material").value(blockPosition.getMaterial().toString()); // Utiliza serialize para el material
            out.name("distanceToCenter").value(blockPosition.getDistanceToCenter());
            out.endObject(); // Finaliza el objeto del bloque
        }
        out.endArray(); // Finaliza el arreglo de posiciones de bloques

        // Serializa el mapa de ingredientes
        out.name("recipe");
        out.beginObject(); // Comienza la escritura del objeto recipe

        out.name("ingredients");
        out.beginObject();
        for (Map.Entry<Character, Material> entry : structure.getRawRecipe().getIngredients().entrySet()) {
            out.name(String.valueOf(entry.getKey())).value(entry.getValue().toString());
        }
        out.endObject();

        // Serializa la shape (forma)
        out.name("shape");
        out.beginArray();
        for (String row : structure.getRawRecipe().getShape()) {
            out.value(row); // Cada línea de la forma
        }
        out.endArray();

        out.endObject(); // Termina la escritura del objeto recipe

        StructureItem item = structure.getItem();

        if (item != null) {
            out.name("item");
            out.beginObject();
            out.name("type").value(item.getType());
            out.name("displayName").value(item.getDisplayName());
            out.name("id").value(item.getId());
            out.name("lore");
            out.beginArray();
            for (String line : item.getLore()) {
                out.value(line);
            }
            out.endArray();
            out.endObject();
        }

        out.endObject(); // Finaliza el objeto JSON
    }

    @Override
    public Structure read(JsonReader in) throws IOException {
        String name = "";
        StructuresType type = null;
        float health = 0;
        String id = "";
        Material[][][] matriz = new Material[0][][];
        List<StructureBlockPosition> blockPositions = new ArrayList<>();
        StructureItem item = null;
        Structure structure = null;
        Recipe recipe = null;
        boolean fullDrop = true;
        int buildTime = 3;
        Facing face = null;

        in.beginObject(); // Inicia la lectura del objeto JSON

        while (in.hasNext()) {
            String fieldName = in.nextName(); // Lee el nombre del campo
            switch (fieldName) {
                case "name":
                    name = in.nextString(); // Lee el nombre
                    break;
                case "face":
                    face = Facing.valueOf(in.nextString()); // Lee el nombre
                    break;
                case "fullDrop":
                    fullDrop = in.nextBoolean();
                    break;
                case "type":
                    type = StructuresType.valueOf(in.nextString()); // Lee el tipo
                    break;
                case "buildTime":
                    buildTime = in.nextInt();
                    break;
                case "health":
                    health = (float) in.nextDouble(); // Lee la salud
                    break;
                case "id":
                    id = in.nextString(); // Lee el id
                    break;
                case "matriz":
                    matriz = readMatriz(in); // Convierte la lista a array
                    break;
                case "blockPositions":
                    blockPositions = readBlockPositions(in);
                    break;
                case "item": // Lee el StructureItem
                    item = readStructureItem(in);
                    break;
                case "recipe":
                    recipe = readRecipe(in);
                    break;
            }
        }

        in.endObject(); // Finaliza el objeto JSON

        if (type == StructuresType.FOUNDATION) {
            if (recipe != null) {
                structure = new Foundation(name, health, item, matriz, id, recipe);
            } else {
                structure = new Foundation(name, health, item, matriz, id);
            }
        } else {
            if (item != null) {
                structure = new Structure(name, type, health, matriz, id, item);
            } else {
                structure = new Structure(name, type, health, matriz, id);
            }
        }

        structure.setBlockPositions(blockPositions);
        structure.setFullDrop(fullDrop);
        structure.setBuildTime(buildTime);

        structure.setFace(face);

        if (structure.getRawRecipe() != null && structure.getShapedRecipe() != null)
            structure.setShapedRecipeIngredients(structure.getRawRecipe().getShape(),
                    structure.getRawRecipe().getIngredients());

        return structure;
    }

    private List<StructureBlockPosition> readBlockPositions(JsonReader in) throws IOException {
        List<StructureBlockPosition> blockPositions = new ArrayList<>();

        in.beginArray(); // Inicia la lectura del arreglo de posiciones de bloques
        while (in.hasNext()) {
            in.beginObject();
            Location location = null;
            Material material = null;
            double distanceToCenter = 0;

            while (in.hasNext()) {
                String blockFieldName = in.nextName();
                switch (blockFieldName) {
                    case "location":
                        location = readLocation(in); // Lee la ubicación
                        break;
                    case "material":
                        String materialString = in.nextString();
                        material = Material.valueOf(materialString); // Método hipotético
                        break;
                    case "distanceToCenter":
                        distanceToCenter = in.nextDouble();
                        break;
                }
            }

            blockPositions.add(new StructureBlockPosition(location, material, distanceToCenter));
            in.endObject(); // Finaliza el objeto del bloque
        }
        in.endArray(); // Finaliza la lectura del arreglo de posiciones de bloques

        return blockPositions;
    }

    private Material[][][] readMatriz(JsonReader in) throws IOException {
        List<List<List<Material>>> layers = new ArrayList<>(); // Lista para almacenar las capas

        in.beginArray(); // Inicia la lectura del arreglo de capas

        while (in.hasNext()) {
            List<List<Material>> layer = new ArrayList<>(); // Lista para almacenar una capa
            in.beginArray(); // Inicia la lectura de una capa

            while (in.hasNext()) {
                List<Material> row = new ArrayList<>(); // Lista para almacenar una fila
                in.beginArray(); // Inicia la lectura de una fila

                while (in.hasNext()) {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull(); // Consume el valor nulo
                        row.add(null); // Añade un valor nulo a la fila
                    } else {
                        String materialString = in.nextString(); // Lee el material como cadena
                        Material material = Material.valueOf(materialString); // Convierte la cadena en un Material
                        row.add(material); // Añade el material a la fila
                    }
                }

                in.endArray(); // Finaliza la lectura de la fila
                layer.add(row); // Añade la fila a la capa
            }

            in.endArray(); // Finaliza la lectura de la capa
            layers.add(layer); // Añade la capa a la lista de capas
        }

        in.endArray(); // Finaliza la lectura de la matriz

        // Convierte la lista de listas en un arreglo 3D
        return layers.stream()
                .map(layer -> layer.stream()
                        .map(row -> row.toArray(new Material[0]))
                        .toArray(Material[][]::new))
                .toArray(Material[][][]::new);
    }

    private Location readLocation(JsonReader in) throws IOException {
        String worldName = null;
        double x = 0, y = 0, z = 0;
        float yaw = 0, pitch = 0;

        in.beginObject(); // Inicia la lectura del objeto de ubicación
        while (in.hasNext()) {
            String fieldName = in.nextName(); // Lee el nombre del campo
            switch (fieldName) {
                case "world":
                    worldName = in.nextString(); // Lee el nombre del mundo
                    break;
                case "x":
                    x = in.nextDouble(); // Lee la coordenada x
                    break;
                case "y":
                    y = in.nextDouble(); // Lee la coordenada y
                    break;
                case "z":
                    z = in.nextDouble(); // Lee la coordenada z
                    break;
                case "yaw":
                    yaw = (float) in.nextDouble(); // Lee el yaw
                    break;
                case "pitch":
                    pitch = (float) in.nextDouble(); // Lee el pitch
                    break;
            }
        }
        in.endObject(); // Finaliza el objeto de ubicación

        // Aquí debes obtener el mundo usando el nombre
        World world = Bukkit.getServer().getWorld(worldName); // Asumiendo que usas Bukkit para obtener el mundo

        // Retorna el nuevo objeto Location
        return new Location(world, x, y, z, yaw, pitch);
    }

    private StructureItem readStructureItem(JsonReader in) throws IOException {
        String type = null;
        String displayName = null;
        List<String> lore = new ArrayList<>();

        in.beginObject();
        while (in.hasNext()) {
            String itemKey = in.nextName();
            switch (itemKey) {
                case "type":
                    type = in.nextString();
                    break;
                case "displayName":
                    displayName = in.nextString();
                    break;
                case "lore":
                    in.beginArray();
                    while (in.hasNext()) {
                        lore.add(in.nextString());
                    }
                    in.endArray();
                    break;
            }
        }
        in.endObject();

        return new StructureItem(type, displayName, lore);
    }

    private Recipe readRecipe(JsonReader in) throws IOException {
        Map<Character, Material> ingredients = new HashMap<>();
        List<String> shape = new ArrayList<>();

        in.beginObject();

        while (in.hasNext()) {
            String fieldName = in.nextName(); // Lee el nombre del campo
            switch (fieldName) {
                case "ingredients":
                    ingredients = readIngredients(in); // Llama a un método para leer los ingredientes
                    break;
                case "shape":
                    in.beginArray(); // Comienza a leer el array de la forma
                    while (in.hasNext()) {
                        shape.add(in.nextString()); // Agrega cada línea de la forma
                    }
                    in.endArray();
                    break;
                default:
                    in.skipValue(); // Salta valores desconocidos
                    break;
            }
        }

        in.endObject(); // Termina de leer el objeto recipe

        return new Recipe(ingredients, shape.toArray(new String[0])); // Retorna una nueva instancia de Recipe
    }

    private Map<Character, Material> readIngredients(JsonReader in) throws IOException {
        Map<Character, Material> ingredients = new HashMap<>();

        in.beginObject(); // Comienza a leer el objeto ingredients

        while (in.hasNext()) {
            String key = in.nextName(); // Lee la clave (un solo carácter)
            String materialStr = in.nextString(); // Lee el valor (material)
            Material material = Material.valueOf(materialStr); // Convierte el string del material en el enum Material
            ingredients.put(key.charAt(0), material); // Agrega al mapa, donde la clave es el carácter de la shape
        }

        in.endObject(); // Termina de leer el objeto ingredients

        return ingredients; // Retorna el mapa de ingredientes
    }
}
