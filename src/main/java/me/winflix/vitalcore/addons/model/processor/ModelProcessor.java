package me.winflix.vitalcore.addons.model.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.Addons;
import me.winflix.vitalcore.addons.model.data.BbModel;
import me.winflix.vitalcore.addons.model.data.ItemGroup;
import me.winflix.vitalcore.addons.model.data.JavaItemModel;
import me.winflix.vitalcore.addons.model.data.ProcessedBbModel;
import me.winflix.vitalcore.addons.model.data.ProcessedBone;
import me.winflix.vitalcore.addons.model.data.ProcessedCube;
import me.winflix.vitalcore.addons.model.data.ProcessedPackData;
import me.winflix.vitalcore.addons.model.data.animation.AnimationTimeline;
import me.winflix.vitalcore.addons.model.data.animation.RuntimeAnimation;
import me.winflix.vitalcore.addons.model.data.BbModel.Bone;
import me.winflix.vitalcore.addons.model.data.BbModel.Face;
import me.winflix.vitalcore.addons.model.data.BbModel.ModelTexture;
import me.winflix.vitalcore.addons.model.data.EffectKeyframe;
import me.winflix.vitalcore.addons.model.data.JavaItemModel.JavaElement;
import me.winflix.vitalcore.addons.model.data.ProcessedCube.Direction;
import me.winflix.vitalcore.addons.model.data.ProcessedCube.UV;
import me.winflix.vitalcore.addons.model.processor.solvers.RotationSolver;
import me.winflix.vitalcore.addons.utils.MathUtils;

public class ModelProcessor {

    private static final boolean ENABLE_DETAILED_DEBUG = false;

    /**
     * Procesa un BbModel crudo, generando datos para el paquete de recursos
     * y una estructura ProcessedBbModel lista para runtime.
     * CORREGIDO: Maneja múltiples JavaItemModel por hueso y formato 1.21.4+.
     *
     * @param bbModel El modelo BbModel crudo cargado desde el archivo.
     * @return Un ProcessedBbModel que contiene los datos procesados, o null si hay
     *         un error.
     */
    @SuppressWarnings("unused")
    public static ProcessedBbModel process(BbModel bbModel) {
        if (bbModel == null) {
            VitalCore.Log.warning("[ModelProcessor] Intento de procesar un BbModel nulo.");
            return null;
        }

        // Colecciones para los datos del paquete de recursos
        Set<ModelTexture> texturesUsed = new HashSet<>(bbModel.getTextures().values());
        Map<String, JsonObject> geometryJsons = new HashMap<>();
        Map<String, JsonObject> itemModelJsons = new HashMap<>();

        // Mapa para almacenar los huesos procesados
        Map<String, ProcessedBone> processedBonesMap = new HashMap<>();

        // 1. Recolectar todos los huesos de la jerarquía
        List<Bone> allBonesInHierarchy = collectAllBonesRecursive(bbModel.getRootBones());
        if (ENABLE_DETAILED_DEBUG) {
            VitalCore.Log.info("[ModelProcessor] Huesos encontrados en jerarquía: " + allBonesInHierarchy.size());
        }

        // 2. Procesar cada hueso individualmente
        for (Bone bone : allBonesInHierarchy) {
            if (bone == null || bone.getName() == null)
                continue;

            if (bone.getName().equalsIgnoreCase("hitbox")) {
                if (ENABLE_DETAILED_DEBUG)
                    VitalCore.Log.info("[ModelProcessor] Ignorando hueso 'hitbox'.");
                continue;
            }

            if (ENABLE_DETAILED_DEBUG)
                VitalCore.Log.info(
                        "[ModelProcessor] Procesando hueso: " + bone.getName() + " (UUID: " + bone.getUuid() + ")");

            ProcessedBone proBone = new ProcessedBone(bone.getUuid(), bone.getName(),
                    MathUtils.toVector3f(bone.getPivot()),
                    MathUtils.toVector3f(bone.getRotation()));

            List<BbModel.Cube> directCubes = bone.getCubes();

            if (directCubes != null && !directCubes.isEmpty()) {
                if (ENABLE_DETAILED_DEBUG)
                    VitalCore.Log.info("[ModelProcessor]   - Hueso '" + bone.getName() + "' tiene " + directCubes.size()
                            + " cubos directos.");

                for (BbModel.Cube cube : directCubes) {
                    Map<Direction, ProcessedCube.Face> faces = new HashMap<>();
                    for (Map.Entry<String, Face> faceEntry : cube.getFaces().entrySet()) {
                        Face face = faceEntry.getValue();
                        if (face == null || face.isEmpty())
                            continue;

                        Float[] faceUV = face.getUv();
                        UV uv = new UV((faceUV[0] == null) ? 0.0f : faceUV[0],
                                (faceUV[1] == null) ? 0.0f : faceUV[1], (faceUV[2] == null) ? 0.0f : faceUV[2],
                                (faceUV[3] == null) ? 0.0f : faceUV[3],
                                (face.getRotation() == null) ? 0 : face.getRotation());
                        ProcessedCube.Face procFace = new ProcessedCube.Face(uv, face.getTextureId());
                        try {
                            faces.put(Direction.valueOf(faceEntry.getKey().toUpperCase(Locale.ENGLISH)), procFace);
                        } catch (IllegalArgumentException e) {
                            VitalCore.Log.warning(
                                    "[ModelProcessor] Dirección de cara inválida '" + faceEntry.getKey() + "' en cubo '"
                                            + cube.getName() + "' del hueso '" + bone.getName() + "'. Ignorando cara.");
                        }
                    }

                    if (faces.isEmpty()) {
                        if (ENABLE_DETAILED_DEBUG)
                            VitalCore.Log.info("[ModelProcessor]     - Cubo '" + cube.getName()
                                    + "' no tiene caras válidas o texturizadas. Ignorando cubo.");
                        continue;
                    }

                    Vector3d cubeFrom = MathUtils.toVector3d(cube.getFrom()).sub((Vector3fc) proBone.getInitialPivot());
                    Vector3d cubeTo = MathUtils.toVector3d(cube.getTo()).sub((Vector3fc) proBone.getInitialPivot());
                    Vector3d cubePivot = MathUtils.toVector3d(cube.getPivot())
                            .sub((Vector3fc) proBone.getInitialPivot());
                    Vector3d cubeRotation = MathUtils.toVector3d(cube.getRotation());

                    ProcessedCube pCube = new ProcessedCube(cube.getName(), cubeFrom, cubeTo, cubePivot,
                            cubeRotation, faces, cube.getInflate());
                    proBone.getCubes().add(pCube);
                    if (ENABLE_DETAILED_DEBUG)
                        VitalCore.Log.info("[ModelProcessor]     - Añadido ProcessedCube: " + pCube.getName());
                }

                if (!proBone.getCubes().isEmpty()) {
                    Set<ItemGroup> groups = proBone.getGroups();
                    Set<ProcessedCube> cubesForSolver = proBone.getCubes();
                    List<JavaItemModel> modelsForThisBone = new ArrayList<>();

                    RotationSolver.solve(groups, cubesForSolver);
                    if (ENABLE_DETAILED_DEBUG)
                        VitalCore.Log.info("[ModelProcessor]   - RotationSolver generó " + groups.size()
                                + " ItemGroups para hueso '" + bone.getName() + "'.");

                    float maxDistToOrigin = 0.0f;

                    for (final ItemGroup group : groups) {
                        final JavaItemModel jiModel = group.toJavaItemModel(proBone.getName(), bbModel);
                        maxDistToOrigin = Math.max(jiModel.getMaxDistToOrigin(), maxDistToOrigin);
                        modelsForThisBone.add(jiModel);
                        if (ENABLE_DETAILED_DEBUG)
                            VitalCore.Log.info("[ModelProcessor] - Generado JavaItemModel desde ItemGroup");
                    }

                    if (!modelsForThisBone.isEmpty()) {
                        int finalScale = 1;
                        for (final JavaItemModel m : modelsForThisBone) {
                            m.setMaxDistToOrigin(maxDistToOrigin);
                            finalScale = Math.max(finalScale, m.scaleToFit());
                        }
                        proBone.setScale(finalScale);

                        int modelIndex = 0;
                        for (JavaItemModel currentJavaModel : modelsForThisBone) {
                            String geometryId;
                            String itemId;
                            String bonePartName = proBone.getName();

                            if (modelsForThisBone.size() == 1) {
                                geometryId = Addons.ADDONS_NAMESPACE + ":" + bbModel.getName()
                                        + "/" + bonePartName;
                                itemId = Addons.ADDONS_NAMESPACE + ":" + bbModel.getName() + "/"
                                        + bonePartName;
                                currentJavaModel.setName(bonePartName);
                            } else {
                                String partSuffix = (modelIndex == 0) ? "" : "_" + (modelIndex + 1);
                                String pathSuffix = (modelIndex == 0) ? "" : "/" + (modelIndex + 1);

                                geometryId = Addons.ADDONS_NAMESPACE + ":" + bbModel.getName()
                                        + "/" + bonePartName + pathSuffix;
                                itemId = Addons.ADDONS_NAMESPACE + ":" + bbModel.getName() + "/"
                                        + bonePartName + pathSuffix;
                                currentJavaModel.setName(bonePartName + partSuffix);
                            }

                            JsonObject geoJson = createItemGeometryJson(currentJavaModel, bbModel);
                            JsonObject itemJson = createItemModelJson(currentJavaModel, bbModel, geometryId);

                            geometryJsons.put(geometryId, geoJson);
                            itemModelJsons.put(itemId, itemJson);

                            if (ENABLE_DETAILED_DEBUG) {
                                VitalCore.Log.info("[ModelProcessor]     - Generado JSON Geometría: " + geometryId);
                                VitalCore.Log.info("[ModelProcessor]     - Generado JSON Item (1.21+): " + itemId);
                            }

                            modelIndex++;
                        }
                        proBone.getItemModels().addAll(modelsForThisBone);
                    } else {
                        if (ENABLE_DETAILED_DEBUG)
                            VitalCore.Log.info("[ModelProcessor]   - No se generaron JavaItemModels para el hueso '"
                                    + bone.getName() + "'.");
                    }

                } else {
                    if (ENABLE_DETAILED_DEBUG)
                        VitalCore.Log
                                .info("[ModelProcessor]   - Hueso '" + bone.getName() + "' no tiene cubos válidos.");
                }
            } else {
                if (ENABLE_DETAILED_DEBUG)
                    VitalCore.Log.info("[ModelProcessor]   - Hueso '" + bone.getName() + "' no tiene cubos directos.");
            }

            processedBonesMap.put(proBone.getUuid(), proBone);
        }

        for (Bone rawBone : allBonesInHierarchy) {
            ProcessedBone processedParent = processedBonesMap.get(rawBone.getUuid());
            if (processedParent == null)
                continue;

            if (rawBone.getChildren() != null) {
                for (Bone rawChild : rawBone.getChildren()) {
                    // Asegurarse de que rawChild y su UUID no sean nulos
                    if (rawChild != null && rawChild.getUuid() != null) {
                        ProcessedBone processedChild = processedBonesMap.get(rawChild.getUuid());
                        if (processedChild != null) {
                            processedParent.addChild(processedChild);
                            if (ENABLE_DETAILED_DEBUG)
                                VitalCore.Log.info("[ModelProcessor] Enlazando hijo '" + processedChild.getName()
                                        + "' a padre '" + processedParent.getName() + "'.");
                        } else {
                            if (ENABLE_DETAILED_DEBUG)
                                VitalCore.Log
                                        .warning("[ModelProcessor] No se encontró ProcessedBone para el hijo con UUID: "
                                                + rawChild.getUuid() + " (Padre: " + processedParent.getName() + ")");
                        }
                    } else {
                        if (ENABLE_DETAILED_DEBUG)
                            VitalCore.Log.warning("[ModelProcessor] Se encontró un hijo nulo o sin UUID en el padre: "
                                    + processedParent.getName());
                    }
                }
            }
        }

        ProcessedPackData packData = new ProcessedPackData(
                bbModel.getName(), geometryJsons, itemModelJsons, texturesUsed);

        Map<String, ProcessedBone> rootProcessedBones = new HashMap<>();
        for (Bone rootRawBone : bbModel.getRootBones()) {
            if (rootRawBone == null || rootRawBone.getUuid() == null)
                continue; // Chequeo adicional
            ProcessedBone rootProcessed = processedBonesMap.get(rootRawBone.getUuid());
            if (rootProcessed != null) {
                // Usar UUID como clave es más robusto que el nombre
                rootProcessedBones.put(rootProcessed.getUuid(), rootProcessed);
            }
        }

        // Pasar el mapa completo de huesos procesados, no solo las raíces,
        // ya que la jerarquía se reconstruye internamente o se usa para lookup.
        ProcessedBbModel proModel = new ProcessedBbModel(bbModel.getName(), processedBonesMap, packData);

        if (bbModel.getAnimations() != null) {
            if (ENABLE_DETAILED_DEBUG)
                VitalCore.Log.info("[ModelProcessor] Procesando " + bbModel.getAnimations().size() + " animaciones...");
            for (BbModel.Animation bbAnimation : bbModel.getAnimations()) {
                if (bbAnimation == null || bbAnimation.getName() == null)
                    continue;

                RuntimeAnimation runtimeAnimation = new RuntimeAnimation(
                        bbAnimation.getName(),
                        bbAnimation.getLength(),
                        bbAnimation.getLoop(),
                        bbAnimation.isOverride());

                if (bbAnimation.getAnimators() != null) {
                    for (BbModel.Animation.Animator animator : bbAnimation.getAnimators()) {
                        if (animator == null || animator.keyframes() == null)
                            continue;

                        String animatorKey; // UUID del hueso o nombre especial como "effects"
                        AnimationTimeline timeline;

                        // Determinar si es un animator de hueso o global (efectos)
                        // Blockbench puede nombrar el animator de efectos como "effects"
                        // o puede no tener un UUID parseable si la clave en JSON no era un UUID.
                        // El deserializador de VC actualmente intenta parsear todas las claves de
                        // animator como UUID.
                        // Esto podría necesitar un ajuste en BbModelDeserializer.AnimationDeserializer
                        // si
                        // las claves de animator de efectos no son UUIDs válidos.
                        // Por ahora, asumimos que si el UUID es nulo pero el nombre es "effects", es
                        // global.
                        // O, si el nombre del animator es "effects" (convención).
                        boolean isGlobalEffectsAnimator = "effects".equalsIgnoreCase(animator.name());

                        if (isGlobalEffectsAnimator) {
                            animatorKey = "global_effects"; // Clave interna para la timeline global
                            timeline = runtimeAnimation.getGlobalEffectTimeline();
                            if (ENABLE_DETAILED_DEBUG)
                                VitalCore.Log.info("[ModelProcessor]   Procesando animator GLOBAL '"
                                        + animator.name() + "' para animación '" + runtimeAnimation.getName() + "'");
                        } else if (animator.uuid() != null) {
                            animatorKey = animator.uuid().toString();
                            timeline = runtimeAnimation.getOrCreateBoneTimeline(animatorKey);
                            if (ENABLE_DETAILED_DEBUG)
                                VitalCore.Log.info("[ModelProcessor]   Procesando animator de HUESO '"
                                        + animator.name() + "' (UUID: " + animatorKey + ") para animación '"
                                        + runtimeAnimation.getName() + "'");
                        } else {
                            if (ENABLE_DETAILED_DEBUG)
                                VitalCore.Log.warning("[ModelProcessor] Animator sin UUID y no es 'effects': "
                                        + animator.name() + ". Saltando.");
                            continue;
                        }

                        for (BbModel.Animation.Keyframe kf : animator.keyframes()) {
                            if (kf == null || kf.getChannel() == null)
                                continue;

                            String channel = kf.getChannel().toLowerCase();
                            float time = kf.getTime();
                            String interp = kf.getInterpolationType(); // Ya es lowercase o default "linear"

                            switch (channel) {
                                case "position":
                                    if (kf.getPosition() != null) {
                                        timeline.addPositionKeyframe(time, new Vector3f(kf.getPosition()), interp);
                                    }
                                    break;
                                case "rotation":
                                    if (kf.getRotation() != null) {
                                        // La rotación en BbModel.Animation.Keyframe ya es Quaternionf
                                        timeline.addRotationKeyframe(time, new Quaternionf(kf.getRotation()), interp);
                                    }
                                    break;
                                case "scale":
                                    if (kf.getScale() != null) {
                                        timeline.addScaleKeyframe(time, new Vector3f(kf.getScale()), interp);
                                    }
                                    break;
                                case "sound":
                                case "particle":
                                    if (kf.getEffect() != null) {
                                        EffectKeyframe effect = new EffectKeyframe(kf.getChannel(), kf.getEffect(),
                                                time);
                                        // Los efectos se añaden como una lista de un solo elemento
                                        timeline.addEffectKeyframe(time, Collections.singletonList(effect), interp);
                                    }
                                    break;
                                default:
                                    if (ENABLE_DETAILED_DEBUG)
                                        VitalCore.Log.warning("[ModelProcessor] Canal de keyframe desconocido: "
                                                + channel + " en animator " + animator.name());
                                    break;
                            }
                        }
                        if (ENABLE_DETAILED_DEBUG && timeline != null) {
                            VitalCore.Log.info("[ModelProcessor]     - Timeline para " + animatorKey + ": PosKeyframes="
                                    + timeline.getPositionInterpolator().getKeyframeCount() +
                                    ", RotKeyframes=" + timeline.getRotationInterpolator().getKeyframeCount() +
                                    ", ScaleKeyframes=" + timeline.getScaleInterpolator().getKeyframeCount() +
                                    ", EffectKeyframes=" + timeline.getEffectInterpolator().getKeyframeCount());
                        }
                    }
                }
                proModel.addRuntimeAnimation(runtimeAnimation);
                if (ENABLE_DETAILED_DEBUG)
                    VitalCore.Log.info("[ModelProcessor]   Animación procesada: " + runtimeAnimation.getName());
            }
        }
        
        return proModel;
    }

    /**
     * Crea la estructura ProcessedBbModel reconstruyendo la jerarquía
     * directamente a partir de la información de hijos en Bone.
     *
     * @param rawModel     El BbModel original cargado.
     * @param boneMappings El mapa de UUID de hueso a ItemModelID generado por
     *                     ModelProcessor.
     * @return El ProcessedBbModel listo para runtime.
     */
    private static ProcessedBbModel createProcessedBbModel(BbModel rawModel, Map<String, ProcessedBone> bones,
            ProcessedPackData packData) {
        return new ProcessedBbModel(rawModel.getName(), bones, packData);
    }

    private static List<Bone> collectAllBonesRecursive(List<Bone> rootBones) { /* ... */
        List<Bone> allBones = new ArrayList<>();
        if (rootBones != null) {
            for (Bone root : rootBones) {
                collectAllBones(root, allBones);
            }
        }
        return allBones;
    }

    private static void collectAllBones(Bone currentBone, List<Bone> allBones) { /* ... */
        if (currentBone == null)
            return;
        allBones.add(currentBone);
        if (currentBone.getChildren() != null) {
            for (Bone child : currentBone.getChildren()) {
                collectAllBones(child, allBones);
            }
        }
    }

    private static JsonObject createItemModelJson(JavaItemModel itemModel, BbModel bbModel, String geometryId) {
        JsonObject root = new JsonObject();
        JsonObject modelInfo = new JsonObject();

        modelInfo.addProperty("type", "minecraft:model");
        modelInfo.addProperty("model", geometryId.replace("models/", ""));

        // Opcional: Añadir si quieres controlar la animación de subida/bajada en
        // primera persona
        modelInfo.addProperty("hand_animation_on_swap", true);

        root.add("model", modelInfo);

        return root;
    }

    private static JsonObject createItemGeometryJson(JavaItemModel javaItemModel, BbModel bbModel) {
        JsonObject modelJson = new JsonObject();

        JsonObject texturesJson = new JsonObject();
        for (Map.Entry<String, String> entry : javaItemModel.getTextures().entrySet()) {
            String textureId = entry.getKey();
            String texturePath = entry.getValue();
            texturesJson.addProperty(textureId, texturePath);
        }
        if (texturesJson.size() > 0 && texturesJson.has("0")) {
            texturesJson.addProperty("particle", texturesJson.get("0").getAsString());
        } else if (javaItemModel.getTextures().containsKey("particle")) {
            texturesJson.addProperty("particle", javaItemModel.getTextures().get("particle"));
        }

        if (texturesJson.size() > 0) {
            modelJson.add("textures", texturesJson);
        }

        JsonArray elementsArray = new JsonArray();
        for (JavaElement el : javaItemModel.getElements()) {
            if (el == null)
                continue;
            JsonObject elementJson = new JsonObject();

            JsonArray fromArr = new JsonArray();
            for (float val : el.getFrom())
                fromArr.add(val);
            elementJson.add("from", fromArr);

            JsonArray toArr = new JsonArray();
            for (float val : el.getTo())
                toArr.add(val);
            elementJson.add("to", toArr);

            JsonObject facesJson = new JsonObject();
            if (el.getFaces() != null) {
                for (Map.Entry<String, JavaElement.Face> faceEntry : el.getFaces().entrySet()) {
                    String direction = faceEntry.getKey();
                    JavaElement.Face faceInfo = faceEntry.getValue();
                    if (direction == null || faceInfo == null || faceInfo.getTexture() == null)
                        continue;

                    JsonObject faceJson = new JsonObject();
                    faceJson.addProperty("texture", faceInfo.getTexture());

                    float[] uvOrig = faceInfo.getUv();
                    if (uvOrig != null && uvOrig.length == 4) {
                        JsonArray uvJson = new JsonArray();
                        for (float val : uvOrig)
                            uvJson.add(MathUtils.clamp(val, 0f, 16f));
                        faceJson.add("uv", uvJson);
                    }
                    if (faceInfo.getRotation() != 0) {
                        faceJson.addProperty("rotation", faceInfo.getRotation());
                    }
                    facesJson.add(direction.toLowerCase(), faceJson);
                }
            }
            if (facesJson.size() > 0) {
                elementJson.add("faces", facesJson);
            }

            elementsArray.add(elementJson);
        }
        if (elementsArray.size() > 0) {
            modelJson.add("elements", elementsArray);
        }

        JsonObject displayJson = new JsonObject();
        if (javaItemModel.getDisplay() != null) {
            for (Map.Entry<String, Map<String, float[]>> displayEntry : javaItemModel.getDisplay().entrySet()) {
                String view = displayEntry.getKey();
                JsonObject viewJson = new JsonObject();
                for (Map.Entry<String, float[]> transformEntry : displayEntry.getValue().entrySet()) {
                    String transformType = transformEntry.getKey();
                    JsonArray transformValues = new JsonArray();
                    for (float val : transformEntry.getValue())
                        transformValues.add(val);
                    viewJson.add(transformType, transformValues);
                }
                displayJson.add(view, viewJson);
            }
        }
        if (!displayJson.has("gui")) {
            JsonObject guiDisplay = new JsonObject();
            JsonArray guiRot = new JsonArray();
            guiRot.add(30);
            guiRot.add(225);
            guiRot.add(0);
            JsonArray guiTrans = new JsonArray();
            guiTrans.add(0);
            guiTrans.add(0);
            guiTrans.add(0);
            JsonArray guiScale = new JsonArray();
            guiScale.add(0.625);
            guiScale.add(0.625);
            guiScale.add(0.625);
            guiDisplay.add("rotation", guiRot);
            guiDisplay.add("translation", guiTrans);
            guiDisplay.add("scale", guiScale);
            displayJson.add("gui", guiDisplay);
        }

        if (displayJson.size() > 0) {
            modelJson.add("display", displayJson);
        }

        return modelJson;
    }

    public static class ProcessingResult {
        public final ProcessedPackData modelData;
        public final Map<String, List<String>> boneMappings;

        public ProcessingResult(ProcessedPackData modelData, Map<String, List<String>> boneMappings) {
            this.modelData = modelData;
            this.boneMappings = boneMappings;
        }

        @Override
        public String toString() {
            return "ProcessingResult{" +
                    "modelData=" + modelData +
                    ", boneMappings=" + boneMappings +
                    '}';
        }
    }
}
