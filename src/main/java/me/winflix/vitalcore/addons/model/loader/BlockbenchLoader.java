package me.winflix.vitalcore.addons.model.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.model.data.BbModel;
import me.winflix.vitalcore.addons.model.data.BbModel.Animation;
import me.winflix.vitalcore.addons.model.data.BbModel.Bone;
import me.winflix.vitalcore.addons.model.data.BbModel.Cube;
import me.winflix.vitalcore.addons.model.data.BbModel.Face;
import me.winflix.vitalcore.addons.model.data.BbModel.ModelTexture;
import me.winflix.vitalcore.addons.model.loader.deserializer.BbModelDeserializer;

/**
 * Clase encargada de cargar modelos Blockbench (.bbmodel) desde archivos,
 * utilizando Gson y deserializadores personalizados, e incluyendo el
 * post-procesamiento necesario para enlazar referencias.
 */
public class BlockbenchLoader {

    /**
     * Carga un modelo Blockbench desde un archivo.
     *
     * @param file El archivo .bbmodel a cargar.
     * @return Un objeto BbModel completamente cargado y procesado, o null si ocurre
     *         un error.
     */
    public static BbModel load(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            VitalCore.Log.warning("Error: El archivo proporcionado es nulo, no existe o no es un archivo válido: "
                    + ((file != null) ? file.getPath() : "null"));
            return null;
        }

        // Usar try-with-resources con BufferedReader y especificando UTF-8
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {

            // 1. Crear GsonBuilder
            GsonBuilder gsonBuilder = new GsonBuilder();

            // 2. Registrar TODOS los deserializadores personalizados necesarios
            gsonBuilder.registerTypeAdapter(BbModel.class, new BbModelDeserializer());
            gsonBuilder.registerTypeAdapter(Cube.class, new BbModelDeserializer.CubeDeserializer());
            gsonBuilder.registerTypeAdapter(Bone.class, new BbModelDeserializer.BoneDeserializer());
            gsonBuilder.registerTypeAdapter(Animation.class, new BbModelDeserializer.AnimationDeserializer());
            gsonBuilder.registerTypeAdapter(ModelTexture.class, new BbModelDeserializer.TextureDeserializer());
            gsonBuilder.registerTypeAdapter(Face.class,new BbModelDeserializer.FaceDeserializer());

            // 3. Crear la instancia Gson
            Gson gson = gsonBuilder.create();

            // 4. Deserializar el JSON
            BbModel model = gson.fromJson(reader, BbModel.class);

            // 5. Ejecutar Post-Procesamiento (¡Esencial!)
            if (model != null) {
                resolveCubeReferences(model); // Llamar a la función de enlace
            } else {
                VitalCore.Log.warning("Error: El modelo deserializado resultó nulo para el archivo: " + file.getName());
                return null; // El modelo es nulo, no se puede continuar
            }

            return model; // Devuelve el modelo completamente procesado

        } catch (IOException e) {
            VitalCore.Log.warning("Error de I/O cargando modelo Blockbench: " + file.getName());
            e.printStackTrace();
            return null;
        } catch (JsonParseException e) {
            VitalCore.Log.warning(
                    "Error de formato JSON cargando modelo Blockbench: " + file.getName() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) { // Captura general para otros posibles errores (ej. en post-proc)
            VitalCore.Log.warning("Error Crítico/Inesperado cargando modelo Blockbench: " + file.getName());
            e.printStackTrace();
            return null;
        }
    }

    // ===========================================
    // LÓGICA DE POST-PROCESAMIENTO
    // ===========================================

    /**
     * Realiza el post-procesamiento necesario después de deserializar un
     * Principalmente, enlaza los objetos Cube reales a los Bones correspondientes
     * utilizando los UUIDs recolectados durante la deserialización.
     *
     * @param model El objeto BbModel recién deserializado.
     */
    private static void resolveCubeReferences(BbModel model) {
        // Validaciones iniciales
        if (model == null) {
            VitalCore.Log.warning("Error en resolveCubeReferences: El modelo es nulo.");
            return;
        }
        List<Cube> elements = model.getElements(); // Necesita getElements() en BbModel
        List<Bone> rootBones = model.getRootBones(); // Necesita getRootBones() en BbModel

        if (elements == null || rootBones == null) {
            VitalCore.Log.warning(
                    "WARN en resolveCubeReferences: La lista de elementos (cubos) o huesos raíz es nula para el modelo '"
                            + model.getName() + "'. No se pueden enlazar cubos.");
            // Podría no ser un error fatal si el modelo simplemente no tiene cubos o
            // huesos.
            return;
        }
        if (elements.isEmpty()) {
            VitalCore.Log.warning("INFO en resolveCubeReferences: El modelo '" + model.getName()
                    + "' no tiene elementos (cubos) para enlazar.");
            return; // No hay cubos que enlazar
        }

        // 1. Crear un mapa de todos los cubos por su UUID para búsqueda rápida
        Map<String, Cube> cubeMap;
        try {
            cubeMap = elements.stream()
                    .filter(Objects::nonNull) // Asegurarse de no procesar cubos nulos si fuera posible
                    .filter(cube -> cube.getUuid() != null) // Asegurarse de que el cubo tenga UUID
                    .collect(Collectors.toMap(
                            Cube::getUuid, // Clave: UUID del cubo
                            cube -> cube, // Valor: el objeto cubo
                            (existing, replacement) -> existing // En caso de UUID duplicado, mantener el existente
                    ));
        } catch (IllegalStateException e) {
            VitalCore.Log
                    .warning("Error Crítico en resolveCubeReferences: UUID de cubo duplicado detectado en el modelo '"
                            + model.getName() + "'.");
            // Podrías querer lanzar una excepción o detener la carga aquí.
            // Imprimir duplicados podría ayudar:
            // elements.stream().collect(Collectors.groupingBy(Cube::getUuid)).forEach((uuid,
            // list) -> { if (list.size() > 1) VitalCore.Log.warning(" - UUID Duplicado: " +
            // uuid); });
            return; // Detener el proceso de enlace si hay duplicados
        }

        if (cubeMap.isEmpty() && !elements.isEmpty()) {
            VitalCore.Log.warning(
                    "WARN en resolveCubeReferences: No se pudo crear el mapa de cubos aunque la lista de elementos no estaba vacía (posiblemente faltan UUIDs en los cubos?). Modelo: "
                            + model.getName());
            // Continuar podría tener sentido si algunos huesos no referencian cubos.
        }

        // 2. Recorrer el árbol de huesos (raíces y sus descendientes)
        for (Bone bone : rootBones) {
            resolveCubeReferencesRecursive(bone, cubeMap);
        }
    }

    /**
     * Función recursiva auxiliar para enlazar cubos a un hueso y sus descendientes.
     *
     * @param bone    El hueso actual a procesar.
     * @param cubeMap El mapa de búsqueda de Cubes por UUID.
     * @return El número de enlaces cubo-hueso realizados en este subárbol.
     */
    private static int resolveCubeReferencesRecursive(Bone bone, Map<String, Cube> cubeMap) {
        if (bone == null) {
            return 0;
        }

        int count = 0;
        // Resolver para el hueso actual
        List<String> cubeUuids = bone.getCubesUuid(); // Necesita getCubesUuid() en Bone
        if (cubeUuids != null && !cubeUuids.isEmpty()) {
            for (String cubeUuid : cubeUuids) {
                if (cubeUuid == null)
                    continue; // Ignorar UUIDs nulos en la lista

                Cube foundCube = cubeMap.get(cubeUuid);
                if (foundCube != null) {
                    bone.addCube(foundCube); // Necesita addCube() en Bone
                    count++;
                } else {
                    // Es común que el outliner contenga referencias a grupos que no son cubos
                    // O que un cubo haya sido eliminado pero la referencia quede. Ser un WARN es
                    // apropiado.
                    VitalCore.Log.warning("WARN: No se encontró Cube con UUID '" + cubeUuid
                            + "' referenciado por Bone '" + bone.getName() + "' (" + bone.getUuid() + ")");
                }
            }
            bone.clearCubeUuids();
        }

        // Llamada recursiva para los hijos
        List<Bone> children = bone.getChildren(); // Necesita getChildren() en Bone
        if (children != null) {
            for (Bone childBone : children) {
                count += resolveCubeReferencesRecursive(childBone, cubeMap);
            }
        }
        return count;
    }

}