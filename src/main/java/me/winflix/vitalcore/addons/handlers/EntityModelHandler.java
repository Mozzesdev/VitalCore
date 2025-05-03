package me.winflix.vitalcore.addons.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Transformation;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.interfaces.DefaultModelInstance;
import me.winflix.vitalcore.addons.interfaces.JavaItemModel;
import me.winflix.vitalcore.addons.interfaces.ModelHandler;
import me.winflix.vitalcore.addons.interfaces.ModelInstance;
import me.winflix.vitalcore.addons.interfaces.ProcessedBbModel;
import me.winflix.vitalcore.addons.interfaces.ProcessedBone;
import me.winflix.vitalcore.addons.managers.ResourcePackManager; // Para el namespace
import me.winflix.vitalcore.addons.utils.MathUtils;

public class EntityModelHandler implements ModelHandler {

    private final String METADATA_KEY = "addons_custom_model";
    // Activar para logs muy detallados de transformación
    private static final boolean ENABLE_DEBUG_LOGS = true; // Mantener desactivado por defecto

    /**
     * Spawnea todas las entidades ItemDisplay necesarias para representar un modelo
     * procesado
     * en una ubicación específica.
     *
     * @param model    El modelo procesado (ProcessedBbModel) a instanciar.
     * @param location La ubicación base donde aparecerá el modelo.
     * @param owner    El jugador dueño (puede ser null).
     * @return La ModelInstance creada que contiene las entidades, o null si falla.
     */
    @Override
    public ModelInstance spawn(ProcessedBbModel model, Location location, Player owner) {
        if (model == null || location == null || location.getWorld() == null) {
            VitalCore.Log.warning("[EntityModelHandler] Intento de spawn con modelo o ubicación nulos.");
            return null;
        }
        // Crear la instancia que contendrá las entidades
        DefaultModelInstance instance = new DefaultModelInstance(model, location, owner, this);

        // Matriz de transformación inicial en el mundo (solo traslación a la ubicación
        // base)
        // Usamos Matrix4d para los cálculos de jerarquía por precisión
        Matrix4d initialWorldTransform = new Matrix4d().translation(location.getX(), location.getY(), location.getZ());

        if (ENABLE_DEBUG_LOGS) {
            VitalCore.Log.info("[DEBUG] Spawning model '" + model.getName() + "' at " + location.toVector());
        }

        // Empezar el proceso recursivo desde los huesos raíz del modelo procesado
        // Es crucial obtener las raíces correctamente desde ProcessedBbModel
        Map<String, ProcessedBone> allProcessedBones = model.getBones(); // Obtener TODOS los huesos procesados
        if (allProcessedBones == null || allProcessedBones.isEmpty()) {
            VitalCore.Log.warning("[EntityModelHandler] El modelo procesado '" + model.getName()
                    + "' no contiene huesos procesados.");
            // Podríamos devolver la instancia vacía o null dependiendo del caso de uso
            return instance; // Devolver instancia vacía por ahora
        }

        for (ProcessedBone proBone : allProcessedBones.values()) {
            // Solo procesar raíces (aquellos cuyo padre no está en el mapa o es null)
            if (proBone.getParent() == null) {
                if (ENABLE_DEBUG_LOGS) {
                    VitalCore.Log.info("[DEBUG] Spawning root bone: " + proBone.getName());
                }
                spawnBoneRecursive(instance, proBone, initialWorldTransform);
            }
        }

        if (instance.getBoneEntities().isEmpty() && !allProcessedBones.isEmpty() && ENABLE_DEBUG_LOGS) {
            VitalCore.Log.warning("[DEBUG] ModelInstance " + instance.getInstanceUuid()
                    + " no tiene entidades asociadas después del spawn recursivo (¿Ningún hueso tenía JavaItemModels?).");
        }
        VitalCore.Log.info("[EntityModelHandler] Modelo '" + model.getName() + "' spawneado con "
                + instance.getBoneEntities().size() + " entidades de hueso.");
        return instance;
    }

    /**
     * Método recursivo para spawnear la entidad ItemDisplay de un hueso y sus
     * hijos.
     *
     * @param instance             La instancia del modelo a la que pertenece este
     *                             hueso.
     * @param bone                 El ProcessedBone actual a spawnear.
     * @param parentWorldTransform La matriz de transformación acumulada del padre.
     */
    private void spawnBoneRecursive(DefaultModelInstance instance, ProcessedBone bone, Matrix4d parentWorldTransform) {
        if (bone == null)
            return;

        // --- 1. Calcular la Transformación Local del Hueso ---
        // Pivote relativo al padre (ya está en ProcessedBone.initialPivot)
        // Rotación inicial del hueso (ya está en ProcessedBone.initialRotation)

        // Convertir pivote y rotación de Blockbench a coordenadas/rotación de
        // Minecraft/JOML
        // Pivote: Negar X/Z y escalar / 16
        Vector3d scaledPivot = new Vector3d(
                bone.getInitialPivot().x / 16.0,
                bone.getInitialPivot().y / 16.0,
                bone.getInitialPivot().z / 16.0);
        // Rotación: Convertir Euler ZYX (grados) a Cuaternión
        Quaterniond localRotationQuat = MathUtils.fromEulerZYX(new Vector3d(bone.getInitialRotation()));

        // Crear la matriz de transformación local: Traslación(pivote) * Rotación
        // La escala inicial del hueso suele ser 1, se aplica después globalmente si es
        // necesario
        Matrix4d localTransform = new Matrix4d()
                .translate(scaledPivot) // Mover al pivote
                .rotate(localRotationQuat); // Aplicar rotación inicial

        Matrix4d currentWorldTransform = new Matrix4d(parentWorldTransform).mul(localTransform);

        if (ENABLE_DEBUG_LOGS) {
            VitalCore.Log.info("-----------------------------------------");
            VitalCore.Log.info("[DEBUG] Bone: " + bone.getName() + " (UUID: " + bone.getUuid() + ")");
            VitalCore.Log.info("[DEBUG]   Initial Pivot (Raw BB): " + vectorToString(bone.getInitialPivot()));
            VitalCore.Log.info("[DEBUG]   Scaled Pivot (MC Coords): " + vectorToString(scaledPivot));
            VitalCore.Log
                    .info("[DEBUG]   Initial Rotation (Euler ZYX Deg): " + vectorToString(bone.getInitialRotation()));
            VitalCore.Log.info("[DEBUG]   Local Rotation (Quat): " + quatToString(localRotationQuat));
            // VitalCore.Log.info("[DEBUG] Parent World Transform:\n" +
            // matrixToString(parentWorldTransform));
            // VitalCore.Log.info("[DEBUG] Local Transform:\n" +
            // matrixToString(localTransform));
            // VitalCore.Log.info("[DEBUG] Current World Transform:\n" +
            // matrixToString(currentWorldTransform));
        }

        // --- 3. Spawnear ItemDisplay si el hueso tiene modelos visuales ---
        if (bone.getItemModels() != null && !bone.getItemModels().isEmpty()) {
            int modelIndex = 0; // Para manejar huesos divididos por RotationSolver
            for (JavaItemModel javaModel : bone.getItemModels()) {
                String modelPartName = javaModel.getName(); // Nombre asignado por ModelProcessor (ej. "mundo" o
                                                            // "mundo_2")
                String bonePartKey = bone.getUuid() + ":" + modelPartName; // Clave única para el mapa

                // Construir el NamespacedKey para el modelo JSON
                String modelPath = instance.getModel().getName() + "/" + modelPartName;
                // Ajustar si hay subdirectorios para partes (ej. "mundo/2")
                if (modelPartName.contains("_")) { // Asumiendo sufijo _<num>
                    String baseName = modelPartName.substring(0, modelPartName.lastIndexOf('_'));
                    String indexStr = modelPartName.substring(modelPartName.lastIndexOf('_') + 1);
                    modelPath = instance.getModel().getName() + "/" + baseName + "/" + indexStr;
                } else if (bone.getItemModels().size() > 1 && modelIndex > 0) {
                    // Si no hay sufijo pero hay múltiples, usar índice (menos ideal)
                    modelPath = instance.getModel().getName() + "/" + modelPartName + "/" + (modelIndex + 1);
                }

                NamespacedKey modelKey = null;
                try {
                    // Usar el namespace correcto y la ruta generada
                    modelKey = new NamespacedKey(ResourcePackManager.VITALCORE_NAMESPACE, modelPath);
                    if (ENABLE_DEBUG_LOGS)
                        VitalCore.Log.info("[DEBUG]   Attempting to use ItemModel Key: " + modelKey);
                } catch (Exception e) { // Captura excepciones de NamespacedKey (caracteres inválidos, etc.)
                    VitalCore.Log.warning("[EntityModelHandler] Error creando NamespacedKey para: "
                            + ResourcePackManager.VITALCORE_NAMESPACE + ":" + modelPath + " - " + e.getMessage());
                    continue; // Saltar este JavaItemModel si la clave es inválida
                }

                // Crear el ItemStack (usando STICK o el material base configurado)
                ItemStack displayItem = new ItemStack(ResourcePackManager.BASE_ITEM_MATERIAL);
                ItemMeta meta = displayItem.getItemMeta();

                if (meta != null) {
                    try {
                        // --- Aplicar el modelo usando setItemModel (1.21+) ---
                        meta.setItemModel(modelKey);
                        displayItem.setItemMeta(meta);
                        if (ENABLE_DEBUG_LOGS)
                            VitalCore.Log.info("[DEBUG]   Applied ItemModel Key: " + modelKey);

                    } catch (Exception e) { // Capturar errores específicos si es posible
                        VitalCore.Log.log(Level.SEVERE, "[EntityModelHandler] Error aplicando item_model '" + modelKey
                                + "' a ItemMeta para hueso " + bone.getName(), e);
                        continue; // Saltar si falla la aplicación del modelo
                    }
                } else {
                    VitalCore.Log.warning("[EntityModelHandler] No se pudo obtener ItemMeta para "
                            + displayItem.getType() + " (hueso: " + bone.getName() + ")");
                    continue; // Saltar si no hay meta
                }

                // --- Calcular la Transformación para Bukkit ---
                // Extraer componentes de la matriz MUNDIAL
                Vector3d worldPosDouble = currentWorldTransform.getTranslation(new Vector3d());
                Quaterniond worldRotDouble = currentWorldTransform.getNormalizedRotation(new Quaterniond());
                Vector3d worldScaleDouble = currentWorldTransform.getScale(new Vector3d());

                // Convertir a float para Bukkit Transformation
                Vector3f finalTranslation = new Vector3f((float) worldPosDouble.x, (float) worldPosDouble.y,
                        (float) worldPosDouble.z);
                Quaternionf finalRotation = new Quaternionf((float) worldRotDouble.x, (float) worldRotDouble.y,
                        (float) worldRotDouble.z, (float) worldRotDouble.w);
                Vector3f finalScale = new Vector3f((float) worldScaleDouble.x, (float) worldScaleDouble.y,
                        (float) worldScaleDouble.z);

                // Ajustar escala global del hueso (calculada por ModelProcessor)
                finalScale.mul((float) bone.getScale()); // Aplicar escala calculada para ajustar tamaño

                // *** AJUSTE IMPORTANTE: La transformación del ItemDisplay es RELATIVA a su
                // ubicación de spawn ***
                // Necesitamos calcular la traslación relativa a la baseLocation de la
                // instancia.
                Location baseLoc = instance.getBaseLocation();
                Vector3f relativeTranslation = new Vector3f(finalTranslation)
                        .sub((float) baseLoc.getX(), (float) baseLoc.getY(), (float) baseLoc.getZ());

                finalRotation.rotateY((float) Math.PI);

                Transformation transformation = new Transformation(
                        relativeTranslation, // Usar traslación relativa
                        finalRotation, // Rotación izquierda
                        finalScale, // Escala
                        new Quaternionf() // Rotación derecha (usualmente identidad)
                );

                if (ENABLE_DEBUG_LOGS) {
                    VitalCore.Log.info("[DEBUG]   World Pos: " + vectorToString(worldPosDouble));
                    VitalCore.Log.info("[DEBUG]   Base Loc: " + baseLoc.toVector());
                    VitalCore.Log.info("[DEBUG]   Relative Translation for Bukkit: " + relativeTranslation);
                    VitalCore.Log.info("[DEBUG]   Final Rotation for Bukkit (Quat): " + quatToString(finalRotation));
                    VitalCore.Log.info("[DEBUG]   Final Scale for Bukkit (con ajuste): " + finalScale);
                }

                // --- Spawneo de la Entidad ItemDisplay ---
                ItemDisplay display = null;
                try {
                    // Spawnea la entidad EN la baseLocation, la transformación la moverá
                    display = (ItemDisplay) instance.getBaseLocation().getWorld()
                            .spawnEntity(instance.getBaseLocation(), EntityType.ITEM_DISPLAY);

                    if (display != null && display.isValid()) {
                        display.setItemStack(displayItem);
                        display.setBillboard(Display.Billboard.FIXED); // Para que rote con el modelo
                        display.setInterpolationDuration(1); // Interpolación suave (1 tick)
                        display.setInterpolationDelay(-1); // Iniciar interpolación inmediatamente
                        // Guardar UUID de la instancia en metadata para identificación posterior
                        display.setMetadata(METADATA_KEY,
                                new FixedMetadataValue(VitalCore.getPlugin(), instance.getInstanceUuid().toString()));

                        // Aplicar la transformación calculada
                        display.setTransformation(transformation);

                        // Añadir al mapa de la instancia usando la clave única
                        instance.addBoneEntity(bonePartKey, display);
                        if (ENABLE_DEBUG_LOGS) {
                            VitalCore.Log.info(
                                    "[DEBUG]   SUCCESS: Spawneado y transformado ItemDisplay para " + bonePartKey);
                        }
                    } else {
                        VitalCore.Log.warning(
                                "[EntityModelHandler] Spawn de ItemDisplay falló o inválido para hueso " + bonePartKey);
                    }

                } catch (Exception e) {
                    VitalCore.Log.log(Level.SEVERE,
                            "[EntityModelHandler] FAILED al spawnear o transformar ItemDisplay para hueso "
                                    + bonePartKey + "!",
                            e);
                    // Considerar si continuar con los hijos o abortar
                }
                modelIndex++;
            } // Fin del bucle for (JavaItemModel)
        } else {
            if (ENABLE_DEBUG_LOGS) {
                VitalCore.Log.info("[DEBUG]   Saltando spawn de ItemDisplay para hueso '" + bone.getName()
                        + "' (no tiene JavaItemModels). Calculando transform para hijos.");
            }
        }

        // --- 4. Procesar Hijos Recursivamente ---
        if (bone.getChildren() != null) {
            for (ProcessedBone childBone : bone.getChildren()) {
                spawnBoneRecursive(instance, childBone, currentWorldTransform); // Pasar la matriz MUNDIAL actual
            }
        }
        if (ENABLE_DEBUG_LOGS && (bone.getChildren() == null || bone.getChildren().isEmpty())) {
            VitalCore.Log.info("[DEBUG]   No hay hijos para hueso " + bone.getName());
        }
        if (ENABLE_DEBUG_LOGS) {
            VitalCore.Log.info("-----------------------------------------");
        }
    }

    /**
     * Método de Tick (a implementar). Aquí iría la lógica para actualizar
     * las transformaciones de los ItemDisplay basados en las animaciones.
     *
     * @param instance La instancia del modelo a actualizar.
     */
    @Override
    public void tick(ModelInstance instance) {
        // TODO: Implementar lógica de animación
        // 1. Obtener la animación activa y el tiempo actual.
        // 2. Calcular la transformación interpolada para cada hueso usando
        // ProcessedBone y datos de animación.
        // 3. Obtener el ItemDisplay correspondiente del mapa
        // instance.getBoneEntities().
        // 4. Calcular la nueva Transformation de Bukkit (relativa a la baseLocation).
        // 5. Aplicar la nueva transformación al ItemDisplay:
        // display.setTransformation(newTransformation);
        // (Considerar setInterpolationDuration/Delay para suavizar).
    }

    /**
     * Destruye todas las entidades ItemDisplay asociadas a una instancia del
     * modelo.
     *
     * @param instance La instancia del modelo a destruir.
     */
    @Override
    public void destroy(ModelInstance instance) {
        if (instance == null)
            return;

        Map<String, ItemDisplay> boneEntities = instance.getBoneEntities(); // Obtiene vista no modificable

        // Crear una copia para iterar de forma segura mientras se modifica el mapa
        // original (indirectamente)
        List<ItemDisplay> displaysToRemove = new ArrayList<>(boneEntities.values());

        if (!displaysToRemove.isEmpty()) {
            if (ENABLE_DEBUG_LOGS) {
                VitalCore.Log.info("[DEBUG] Destruyendo " + displaysToRemove.size() + " entidades para instancia "
                        + instance.getInstanceUuid());
            }
            for (ItemDisplay display : displaysToRemove) {
                if (display != null && display.isValid()) {
                    // Limpiar metadata antes de remover
                    if (display.hasMetadata(METADATA_KEY)) {
                        display.removeMetadata(METADATA_KEY, VitalCore.getPlugin());
                    }
                    display.remove(); // Remover la entidad del mundo
                } else if (display != null) {
                    // Log si intentamos remover una entidad ya inválida
                    VitalCore.Log.warning("[DEBUG] Intentando destruir entidad ya inválida: " + display.getUniqueId());
                }
            }
        } else {
            if (ENABLE_DEBUG_LOGS) {
                VitalCore.Log.info("[DEBUG] Instancia " + instance.getInstanceUuid()
                        + " no tenía entidades de hueso para destruir.");
            }
        }

        // Limpiar el mapa interno de la instancia después de remover las entidades
        if (instance instanceof DefaultModelInstance) {
            ((DefaultModelInstance) instance).clearBoneEntities();
            if (ENABLE_DEBUG_LOGS) {
                VitalCore.Log.info(
                        "[DEBUG] Mapa de entidades de hueso limpiado para instancia " + instance.getInstanceUuid());
            }
        }
    }

    // --- Métodos Helper para Logging (JOML double) ---
    private String matrixToString(Matrix4d m) { // Cambiado a Matrix4d
        if (!ENABLE_DEBUG_LOGS || m == null)
            return "";
        StringBuilder sb = new StringBuilder();
        double[] v = new double[16];
        m.get(v); // Obtener como double
        sb.append(String.format(" [%.3f, %.3f, %.3f, %.3f]\n", v[0], v[4], v[8], v[12]));
        sb.append(String.format(" [%.3f, %.3f, %.3f, %.3f]\n", v[1], v[5], v[9], v[13]));
        sb.append(String.format(" [%.3f, %.3f, %.3f, %.3f]\n", v[2], v[6], v[10], v[14]));
        sb.append(String.format(" [%.3f, %.3f, %.3f, %.3f]", v[3], v[7], v[11], v[15]));
        return sb.toString();
    }

    private String vectorToString(Vector3d v) { // Cambiado a Vector3d
        if (!ENABLE_DEBUG_LOGS || v == null)
            return "";
        return String.format("(%.3f, %.3f, %.3f)", v.x, v.y, v.z);
    }

    private String vectorToString(Vector3f v) { // Sobrecarga para Vector3f
        if (!ENABLE_DEBUG_LOGS || v == null)
            return "";
        return String.format("(%.3f, %.3f, %.3f)", v.x, v.y, v.z);
    }

    private String quatToString(Quaterniond q) { // Cambiado a Quaterniond
        if (!ENABLE_DEBUG_LOGS || q == null)
            return "";
        return String.format("(x:%.3f, y:%.3f, z:%.3f, w:%.3f)", q.x, q.y, q.z, q.w);
    }

    private String quatToString(Quaternionf q) { // Sobrecarga para Quaternionf
        if (!ENABLE_DEBUG_LOGS || q == null)
            return "";
        return String.format("(x:%.3f, y:%.3f, z:%.3f, w:%.3f)", q.x, q.y, q.z, q.w);
    }
}