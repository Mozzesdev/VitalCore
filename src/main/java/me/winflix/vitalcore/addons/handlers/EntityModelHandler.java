package me.winflix.vitalcore.addons.handlers;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Transformation;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.Addons;
import me.winflix.vitalcore.addons.model.data.JavaItemModel;
import me.winflix.vitalcore.addons.model.data.ModelContext;
import me.winflix.vitalcore.addons.model.data.ModelHandler;
import me.winflix.vitalcore.addons.model.data.ModelInstance;
import me.winflix.vitalcore.addons.model.data.ProcessedBbModel;
import me.winflix.vitalcore.addons.model.data.ProcessedBone;
import me.winflix.vitalcore.addons.model.data.animation.AnimationController;
import me.winflix.vitalcore.addons.utils.MathUtils;

/**
 * Handles spawning and managing model instances by attaching visual parts
 * (ItemDisplay entities) as passengers to an invisible base entity.
 */
public class EntityModelHandler implements ModelHandler {

    private final String METADATA_KEY = "addons_custom_model";
    private final String BASE_METADATA_KEY = METADATA_KEY + "_base";
    private static final boolean ENABLE_DEBUG_LOGS = false;
    private static final Matrix4f Y_FLIP_MATRIX = new Matrix4f().rotateY((float) Math.PI);
    private static final float DELTA_TIME = 1.0f / 20.0f;
    private static final String METADATA_VALUE_HANDLER_CREATED = "_handler_created";

    @SuppressWarnings("unused")
    @Override
    public ModelInstance createInstance(ProcessedBbModel model, ModelContext context) {
        Location location = context.getLocation();
        location.setPitch(0);
        Player owner = context.getOwner().orElse(null);
        Entity baseEntity = context.getTargetEntity().orElse(null);

        if (baseEntity == null) {
            if (ENABLE_DEBUG_LOGS) {
                VitalCore.Log.info("[EntityModelHandler DEBUG] No targetEntity in context for model '" + model.getName()
                        + "'. Spawning default base entity.");
            }
            try {
                final Location spawnLocation = location.clone();
                // Determine base entity type from context or default to Pig
                EntityType baseEntityType = context.getCustomData("base_entity_type", EntityType.class)
                        .orElse(EntityType.PIG);

                baseEntity = spawnLocation.getWorld().spawn(spawnLocation, baseEntityType.getEntityClass(), entity -> {
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).setSilent(true);
                    }
                    if (entity instanceof Ageable) {
                        ((Ageable) entity).setAdult();
                    }
                    entity.setPersistent(true);
                    entity.setMetadata(METADATA_KEY, new FixedMetadataValue(VitalCore.getPlugin(),
                            model.getName() + METADATA_VALUE_HANDLER_CREATED));
                });

                if (baseEntity == null || !baseEntity.isValid()) {
                    VitalCore.Log.severe("[EntityModelHandler] Failed to spawn BASE entity (" + baseEntityType.name()
                            + ") for model: " + model.getName());
                    return null;
                }
                if (ENABLE_DEBUG_LOGS) {
                    VitalCore.Log.info("[EntityModelHandler DEBUG] Spawned new base entity (ID: "
                            + baseEntity.getEntityId()
                            + ", Type: " + baseEntityType.name() + ") for model '" + model.getName() + "'");
                }
            } catch (Exception e) {
                VitalCore.Log.log(Level.SEVERE,
                        "[EntityModelHandler] Exception spawning NEW base entity for model: " + model.getName(), e);
                return null;
            }
        } else {
            VitalCore.Log.info("[EntityModelHandler] Using provided targetEntity (ID: " + baseEntity.getEntityId()
                    + ") for model '" + model.getName() + "'.");
            if (!baseEntity.hasMetadata(BASE_METADATA_KEY)) {
                baseEntity.setMetadata(BASE_METADATA_KEY,
                        new FixedMetadataValue(VitalCore.getPlugin(), model.getName() + "_linked"));
            }
        }

        ModelInstance instance = new ModelInstance(model, context, this);
        instance.setBaseEntity(baseEntity); // Ensure ModelInstance has the correct base entity reference

        // Initial transformation for root bones relative to the base entity's origin
        // (use Matrix4d)
        Matrix4d initialRootParentTransform = new Matrix4d().translate(0.0, -baseEntity.getHeight(), 0.0);

        Map<String, ProcessedBone> allProcessedBones = model.getBones();
        if (allProcessedBones == null || allProcessedBones.isEmpty()) {
            VitalCore.Log.warning("[EntityModelHandler] Processed model '" + model.getName()
                    + "' has no bones (linked to entity " + baseEntity.getEntityId() + ").");
            return instance;
        }

        for (ProcessedBone proBone : allProcessedBones.values()) {
            if (proBone.getParent() == null) {
                spawnBoneRecursiveAttached(instance, proBone, initialRootParentTransform, baseEntity);
            }
        }

        if (instance.getBoneEntities().isEmpty() && !allProcessedBones.isEmpty()) {
            if (ENABLE_DEBUG_LOGS)
                VitalCore.Log.warning("[EntityModelHandler DEBUG] ModelInstance " + instance.getInstanceUuid()
                        + " has no associated bone entities after recursive spawn (attached to "
                        + baseEntity.getEntityId() + ")");
        }
        if (ENABLE_DEBUG_LOGS)
            VitalCore.Log.info("[EntityModelHandler DEBUG] Model '" + model.getName()
                    + "' instance created, linked to entity "
                    + baseEntity.getEntityId() + " with " + instance.getBoneEntities().size() + " bone entities.");

        return instance;
    }

    /**
     * Spawnea recursivamente las partes visuales (ItemDisplay) de un hueso y sus
     * hijos
     *
     * @param instance                La instancia del modelo.
     * @param bone                    El hueso a procesar.
     * @param parentRelativeTransform La transformación relativa acumulada del padre
     *                                respecto a la entidad base.
     * @param baseEntity              La entidad base a la que se adjuntarán los
     *                                displays.
     */
    private void spawnBoneRecursiveAttached(ModelInstance instance, ProcessedBone bone,
            Matrix4d parentRelativeTransform, Entity baseEntity) {
        if (bone == null || baseEntity == null || !baseEntity.isValid())
            return;

        Vector3d scaledPivot = new Vector3d(
                bone.getInitialPivot().x / 16.0,
                bone.getInitialPivot().y / 16.0,
                bone.getInitialPivot().z / 16.0);
        Quaterniond localRotationQuat = MathUtils.fromEulerZYX(new Vector3d(bone.getInitialRotation()));

        Matrix4d localInitialTransform = new Matrix4d()
                .translate(scaledPivot)
                .rotate(localRotationQuat)
                .rotateY(Math.PI);

        Matrix4d currentRelativeTransform = new Matrix4d(parentRelativeTransform).mul(localInitialTransform);

        // 3. Spawnear ItemDisplays para las partes visuales de ESTE hueso
        if (bone.getItemModels() != null && !bone.getItemModels().isEmpty()) {
            int modelPartIndex = 0;
            for (JavaItemModel javaModel : bone.getItemModels()) {
                String modelPartName = javaModel.getName();
                String bonePartKey = bone.getUuid() + ":" + modelPartName;

                ItemStack displayItem = new ItemStack(Material.PAPER);
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    String itemModelPath = instance.getModel().getName() + "/" + bone.getName();
                    if (bone.getItemModels().size() > 1) {
                        itemModelPath += "/" + (modelPartIndex + 1);
                    }

                    NamespacedKey modelKey = null;
                    try {
                        // Usar el namespace definido en ResourcePackManager
                        modelKey = new NamespacedKey(Addons.ADDONS_NAMESPACE, itemModelPath);
                    } catch (Exception e) { // Captura errores de formato en NamespacedKey
                        VitalCore.Log.warning("[EntityModelHandler] Error creando NamespacedKey para '"
                                + Addons.ADDONS_NAMESPACE + ":" + itemModelPath + "': "
                                + e.getMessage());
                        continue;
                    }

                    try {
                        meta.setItemModel(modelKey);
                        displayItem.setItemMeta(meta);
                        if (ENABLE_DEBUG_LOGS)
                            VitalCore.Log
                                    .info("[DEBUG ItemModel] Aplicado ItemModel '" + modelKey + "' a " + bonePartKey);
                    } catch (Exception e) {
                        VitalCore.Log.log(Level.SEVERE,
                                "[EntityModelHandler] Error al aplicar ItemModel '" + modelKey + "' a " + bonePartKey,
                                e);
                        continue;
                    }

                } else {
                    VitalCore.Log.warning("[EntityModelHandler] No se pudo obtener ItemMeta para "
                            + displayItem.getType() + " (bone: " + bone.getName() + ")");
                    continue;
                }
                Transformation bukkitTransform = matrixToBukkitTransformRelative(currentRelativeTransform,
                        bone.getScale());

                try {
                    ItemDisplay display = baseEntity.getWorld().spawn(baseEntity.getLocation(), ItemDisplay.class,
                            d -> {
                                d.setItemStack(displayItem);
                                d.setTransformation(bukkitTransform);
                                d.setBillboard(Billboard.FIXED);
                                d.setInterpolationDuration(1);
                                d.setInterpolationDelay(-1);
                            });

                    if (display != null && display.isValid()) {
                        instance.addBoneEntity(bonePartKey, display);
                        boolean added = baseEntity.addPassenger(display);
                        if (added) {
                            if (ENABLE_DEBUG_LOGS)
                                VitalCore.Log.info("[DEBUG Spawn Attached]   SUCCESS: Spawned and added PASSENGER "
                                        + bonePartKey + " to base " + baseEntity.getEntityId());
                        } else {
                            VitalCore.Log.warning("[EntityModelHandler] Could not add ItemDisplay "
                                    + display.getUniqueId() + " as passenger to base " + baseEntity.getEntityId()
                                    + " for " + bonePartKey);
                            if (display.isValid())
                                display.remove();
                        }
                    } else {
                        VitalCore.Log
                                .warning("[EntityModelHandler] Spawn of ItemDisplay failed or was invalid for bone "
                                        + bonePartKey);
                    }
                } catch (Exception e) {
                    VitalCore.Log.log(Level.SEVERE,
                            "[EntityModelHandler] Excepción al spawnear/adjuntar ItemDisplay para " + bonePartKey, e);
                }
                modelPartIndex++;
            }
        } else {
            if (ENABLE_DEBUG_LOGS)
                VitalCore.Log.info("[DEBUG Spawn Attached]   Skipping ItemDisplay spawn for bone '" + bone.getName()
                        + "' (no JavaItemModels).");
        }

        if (bone.getChildren() != null) {
            for (ProcessedBone childBone : bone.getChildren()) {
                spawnBoneRecursiveAttached(instance, childBone, currentRelativeTransform, baseEntity);
            }
        }
    }

    @Override
    public void tick(ModelInstance instance) {
        if (instance == null || instance.getModel() == null)
            return;

        Optional<Entity> baseEntityOpt = instance.getBaseEntity();
        if (baseEntityOpt.isEmpty() || !baseEntityOpt.get().isValid()) {
            // Base entity is gone or invalid, ModelEngineManager's tick will handle
            // destroying the instance
            return;
        }
        // Entity baseEntity = baseEntityOpt.get();

        AnimationController animController = instance.getAnimationController();
        if (animController == null)
            return;

        // 1. Tick the animation controller
        animController.tickController(DELTA_TIME);

        // 2. Get the calculated local animated transforms for each bone
        Map<String, AnimationController.BoneTransform> animatedLocalTransforms = animController
                .getCurrentBoneTransforms();

        // 3. Initial parent transform (offset for base entity height)
        Matrix4f rootParentTransform = new Matrix4f().translate(0.0f, (float) -baseEntityOpt.get().getHeight(), 0.0f);

        // 4. Recursively update bone visual transforms
        for (ProcessedBone rootBone : instance.getModel().getBones().values()) {
            if (rootBone.getParent() == null) { // Start with root bones
                updateBoneVisualRecursive(instance, rootBone, rootParentTransform, animatedLocalTransforms);
            }
        }
    }

    @Override
    public void destroy(ModelInstance instance) {
        if (instance == null)
            return;

        if (instance.getAnimationController() != null) {
            instance.getAnimationController().stopAllAnimations(true);
        }

        // Clean up base entity IF created by this handler
        instance.getBaseEntity().ifPresent(baseEntity -> {
            if (baseEntity.isValid()) {
                boolean createdByHandler = false;
                for (MetadataValue metaValue : baseEntity.getMetadata(BASE_METADATA_KEY)) {
                    if (metaValue.getOwningPlugin() == VitalCore.getPlugin()
                            && metaValue.asString().endsWith(METADATA_VALUE_HANDLER_CREATED)) {
                        createdByHandler = true;
                        break;
                    }
                }

                if (createdByHandler) {
                    if (ENABLE_DEBUG_LOGS)
                        VitalCore.Log.info("[EntityModelHandler DEBUG] Destroying handler-created base entity: "
                                + baseEntity.getUniqueId());
                    // IMPORTANT: Remove passengers BEFORE removing the base entity
                    baseEntity.getPassengers().forEach(p -> {
                        if (p != null && p.isValid())
                            p.remove();
                    });
                    baseEntity.remove();
                } else {
                    if (ENABLE_DEBUG_LOGS)
                        VitalCore.Log.info("[EntityModelHandler DEBUG] Base entity " + baseEntity.getUniqueId()
                                + " was not created by this handler. Not removing it.");
                    // Even if not removing base, ensure passengers (our displays) are gone
                    // ModelInstance.remove() should have been called by ModelEngineManager,
                    // but double-checking passengers is safer.
                    baseEntity.getPassengers().forEach(p -> {
                        // Check if passenger is one of our ItemDisplays
                        if (p instanceof ItemDisplay && instance.getBoneEntities().containsValue(p)) {
                            if (p.isValid())
                                p.remove();
                        }
                    });
                }
            }
        });
        if (ENABLE_DEBUG_LOGS)
            VitalCore.Log.info("[EntityModelHandler DEBUG] Destroy called for instance: " + instance.getInstanceUuid());
        // Note: ModelInstance.remove() which clears boneEntities map is called by
        // ModelEngineManager
    }

    private void updateBoneVisualRecursive(ModelInstance instance, ProcessedBone bone, Matrix4f parentAbsoluteMatrix,
            Map<String, AnimationController.BoneTransform> animatedLocalTransforms) {
        if (bone == null)
            return;

        // Get the bone's initial pivot (scaled) and bind pose rotation
        Vector3f scaledInitialPivot = new Vector3f(bone.getInitialPivot()).div(16.0f);
        // Quaternionf initialLocalRotation = MathUtils.fromEulerZYX(new
        // org.joml.Vector3d(bone.getInitialRotation())).normalize(); // ZYX order,
        // degrees

        // Get the animated local transform (P, R, S) for this bone
        // This transform is assumed to be the bone's final local pose relative to its
        // pivot
        AnimationController.BoneTransform animTransform = animatedLocalTransforms.get(bone.getUuid());
        if (animTransform == null) { // Should not happen if controller initializes all bones
            animTransform = new AnimationController.BoneTransform(); // Default to identity
            if (ENABLE_DEBUG_LOGS)
                VitalCore.Log.warning("[EntityModelHandler DEBUG] No animTransform for bone " + bone.getUuid()
                        + " in tick. Using identity.");
        }

        // Construct the bone's local matrix using its initial pivot and animated P, R,
        // S
        // The animTransform.rotation is the final orientation of the bone in its local
        // space.
        // The animTransform.position is the translation from its pivot.
        // The animTransform.scale is the scale around its pivot.
        Matrix4f boneCurrentLocalMatrix = new Matrix4f()
                .translate(scaledInitialPivot) // 1. Move to the bone's pivot point in parent space
                .rotate(animTransform.rotation) // 2. Apply animated rotation AT THE PIVOT
                .scale(animTransform.scale) // 3. Apply animated scale AT THE PIVOT
                .translate(animTransform.position); // 4. Apply animated translation (relative to the pivot)

        // Calculate the bone's new absolute matrix (relative to the base entity)
        Matrix4f boneCurrentAbsoluteMatrix = new Matrix4f(parentAbsoluteMatrix).mul(boneCurrentLocalMatrix);

        // Update all ItemDisplay parts associated with this bone
        if (bone.getItemModels() != null && !bone.getItemModels().isEmpty()) {
            for (JavaItemModel javaModel : bone.getItemModels()) {
                String bonePartDisplayKey = bone.getUuid() + ":" + javaModel.getName();
                ItemDisplay displayEntity = instance.getBoneEntities().get(bonePartDisplayKey);

                if (displayEntity != null && displayEntity.isValid()) {
                    // Apply the Y-flip for Blockbench compatibility before converting to Bukkit
                    // transform
                    Matrix4d finalTransformForDisplay = new Matrix4d(boneCurrentAbsoluteMatrix).mul(Y_FLIP_MATRIX);
                    Transformation newBukkitTransform = matrixToBukkitTransformRelative(finalTransformForDisplay,
                            bone.getScale());

                    displayEntity.setTransformation(newBukkitTransform);
                    // Interpolation duration is set at spawn, but can be re-set if needed:
                    // displayEntity.setInterpolationDuration(1);
                }
            }
        }

        // Recursively update children
        if (bone.getChildren() != null) {
            for (ProcessedBone childBone : bone.getChildren()) {
                updateBoneVisualRecursive(instance, childBone, boneCurrentAbsoluteMatrix, animatedLocalTransforms);
            }
        }
    }

    /**
     * Convierte una Matrix4d (JOML, transformación RELATIVA a la base) a
     * Transformation (Bukkit)
     */
    private Transformation matrixToBukkitTransformRelative(Matrix4d relativeMatrix, float boneScaleFactor) {
        Vector3d translationDouble = relativeMatrix.getTranslation(new Vector3d());
        Quaterniond rotationDouble = relativeMatrix.getNormalizedRotation(new Quaterniond());
        Vector3d scaleDouble = relativeMatrix.getScale(new Vector3d());

        Vector3f translation = new Vector3f((float) translationDouble.x, (float) translationDouble.y,
                (float) translationDouble.z);
        Quaternionf rotation = new Quaternionf((float) rotationDouble.x, (float) rotationDouble.y,
                (float) rotationDouble.z, (float) rotationDouble.w);
        Vector3f scale = new Vector3f((float) scaleDouble.x, (float) scaleDouble.y, (float) scaleDouble.z);

        scale.mul(boneScaleFactor);

        return new Transformation(translation, rotation, scale, new Quaternionf());
    }
}