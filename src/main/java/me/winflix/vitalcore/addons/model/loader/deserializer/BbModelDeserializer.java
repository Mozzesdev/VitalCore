package me.winflix.vitalcore.addons.model.loader.deserializer;

import com.google.gson.*; // Asegúrate de tener las importaciones

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.model.data.BbModel;
import me.winflix.vitalcore.addons.model.data.BbModel.Animation;
import me.winflix.vitalcore.addons.model.data.BbModel.Bone;
import me.winflix.vitalcore.addons.model.data.BbModel.Cube;
import me.winflix.vitalcore.addons.model.data.BbModel.Face;
import me.winflix.vitalcore.addons.model.data.BbModel.ModelTexture;
import me.winflix.vitalcore.addons.model.data.BbModel.Animation.Animator;
import me.winflix.vitalcore.addons.model.data.BbModel.Animation.Keyframe;
import me.winflix.vitalcore.addons.model.data.BbModel.Animation.LoopType;
import me.winflix.vitalcore.addons.utils.GsonUtils;
import me.winflix.vitalcore.addons.utils.MathUtils;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class BbModelDeserializer implements JsonDeserializer<BbModel> {

    @Override
    public BbModel deserialize(final JsonElement jsonElement, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException {

        final JsonObject root = jsonElement.getAsJsonObject();

        String modelName = GsonUtils.get(root, "name", JsonElement::getAsString);

        List<Cube> elements = new ArrayList<>();
        GsonUtils.ifArray(root, "elements", elementJson -> {
            Cube cube = context.deserialize(elementJson, Cube.class);
            if (cube != null) {
                elements.add(cube);
            }
        });

        List<Bone> bones = new ArrayList<>();
        GsonUtils.ifArray(root, "outliner", elementJson -> {
            if (elementJson.isJsonObject()) {
                Bone bone = context.deserialize(elementJson, Bone.class);
                if (bone != null) {
                    bones.add(bone);
                }
            }
        });

        Map<Integer, ModelTexture> textures = new HashMap<>();
        GsonUtils.ifArray(root, "textures", (index, elementJson) -> {
            ModelTexture texture = context.deserialize(elementJson, ModelTexture.class);
            if (texture != null) {
                textures.put(index, texture);
            }
        });

        List<Animation> animations = new ArrayList<>();
        GsonUtils.ifArray(root, "animations", elementJson -> {
            Animation animation = context.deserialize(elementJson, Animation.class);
            if (animation != null) {
                animations.add(animation);
            }
        });

        BbModel model = new BbModel(modelName, bones, elements, animations, textures);

        return model;
    }

    public static class BoneDeserializer implements JsonDeserializer<Bone> {
        @Override
        public Bone deserialize(JsonElement jsonElement, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            String name = GsonUtils.get(jsonElement, "name", JsonElement::getAsString);
            if (name != null) {
                name = name.toLowerCase();
            }
            Float[] origin = GsonUtils.get(jsonElement, "origin", GsonUtils::getAsFloatArray);
            Float[] rotation = GsonUtils.get(jsonElement, "rotation", GsonUtils::getAsFloatArray);
            UUID uuidValue = GsonUtils.get(jsonElement, "uuid", GsonUtils::getAsUUID);
            String uuid = (uuidValue != null) ? uuidValue.toString() : null;

            Bone currentBone = new Bone(uuid, name, origin, rotation);

            JsonObject jsonObj = jsonElement.getAsJsonObject();

            // Procesar children usando context
            if (jsonObj.has("children") && jsonObj.get("children").isJsonArray()) {
                JsonArray childrenArray = jsonObj.getAsJsonArray("children");
                for (JsonElement childElement : childrenArray) {
                    if (childElement.isJsonObject()) {
                        Bone childBone = context.deserialize(childElement, Bone.class);
                        if (childBone != null) {
                            childBone.setParent(currentBone);
                            currentBone.addChildBone(childBone);
                        }
                    } else if (childElement.isJsonPrimitive() && childElement.getAsJsonPrimitive().isString()) {
                        String cubeUuid = childElement.getAsString();
                        currentBone.addCubeUuid(cubeUuid);
                    }
                }
            }
            return currentBone;
        }
    }

    /**
     * Deserializador para Cubes.
     * NOTA: La implementación actual es un placeholder y probablemente incorrecta.
     * Debe ser implementada para extraer todos los campos del Cube, incluyendo el
     * mapa 'faces'.
     */
    public static class CubeDeserializer implements JsonDeserializer<Cube> {
        @Override
        public Cube deserialize(final JsonElement jsonElement, final Type typeOfT,
                final JsonDeserializationContext context) throws JsonParseException {

            if (!jsonElement.isJsonObject()) {
                throw new JsonParseException("Se esperaba un objeto JSON para deserializar Cube");
            }
            JsonObject jsonObj = jsonElement.getAsJsonObject();

            String uuid = GsonUtils.get(jsonObj, "uuid", JsonElement::getAsString);
            String name = GsonUtils.get(jsonObj, "name", JsonElement::getAsString);
            Float[] from = GsonUtils.get(jsonObj, "from", GsonUtils::getAsFloatArray);
            Float[] to = GsonUtils.get(jsonObj, "to", GsonUtils::getAsFloatArray);
            Float[] pivot = GsonUtils.get(jsonObj, "origin", GsonUtils::getAsFloatArray);
            Float[] rotation = GsonUtils.get(jsonObj, "rotation", GsonUtils::getAsFloatArray);
            float inflate = GsonUtils.get(jsonObj, "inflate", JsonElement::getAsFloat, 0.0f);

            Map<String, Face> faces = new HashMap<>();
            if (jsonObj.has("faces") && jsonObj.get("faces").isJsonObject()) {
                JsonObject facesObj = jsonObj.getAsJsonObject("faces");
                for (Map.Entry<String, JsonElement> entry : facesObj.entrySet()) {
                    String direction = entry.getKey();
                    if (direction != null) {
                        Face faceInfo = context.deserialize(entry.getValue(), Face.class);
                        if (faceInfo != null) {
                            faces.put(direction, faceInfo);
                        }
                    }
                }
            }

            return new Cube(uuid, name, from, to, pivot, rotation, inflate, faces);
        }
    }

    /**
     * Deserializador personalizado para BbModel.ModelTexture.
     * Maneja la extracción de campos y la diferencia de nombres para
     * uv_width/uv_height.
     */
    public static class TextureDeserializer implements JsonDeserializer<BbModel.ModelTexture> {

        @Override
        public BbModel.ModelTexture deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {

            if (!json.isJsonObject()) {
                throw new JsonParseException("Se esperaba un objeto JSON para ModelTexture");
            }
            JsonObject jsonObj = json.getAsJsonObject();

            // Extraer campos simples (usando GsonUtils o acceso directo)
            // Proporcionar valores por defecto o lanzar error si faltan campos requeridos
            String uuid = GsonUtils.get(jsonObj, "uuid", JsonElement::getAsString);
            String id = GsonUtils.get(jsonObj, "id", JsonElement::getAsString, "#missing"); // Default ID
            String name = GsonUtils.get(jsonObj, "name", JsonElement::getAsString, "missing.png"); // Default name
            boolean particle = GsonUtils.get(jsonObj, "particle", JsonElement::getAsBoolean, false); // Default false
            String sourceBase64 = GsonUtils.get(jsonObj, "source", JsonElement::getAsString); // Crucial

            // Extraer dimensiones (int)
            int width = GsonUtils.get(jsonObj, "width", JsonElement::getAsInt, 0); // Default 0 o 16?
            int height = GsonUtils.get(jsonObj, "height", JsonElement::getAsInt, 0);

            // Extraer dimensiones UV (int), mapeando nombres JSON a Java
            int uvWidth = GsonUtils.get(jsonObj, "uv_width", JsonElement::getAsInt, width);
            int uvHeight = GsonUtils.get(jsonObj, "uv_height", JsonElement::getAsInt, height);

            // Validar datos mínimos (ej: source y uuid)
            if (uuid == null) {
                // Podrías generar uno o lanzar error
                VitalCore.Log.warning("WARN: Textura encontrada sin UUID en el JSON.");
                uuid = UUID.randomUUID().toString();
            }
            if (sourceBase64 == null || sourceBase64.isEmpty()) {
                VitalCore.Log
                        .warning("WARN: Textura encontrada sin 'source' (Base64). UUID: " + uuid + ", Name: " + name);
                sourceBase64 = "";
            }

            // Llamar al constructor de ModelTexture (asegúrate que sea static)
            // El constructor se encarga de decodificar sourceBase64
            try {
                return new ModelTexture(uuid, id, name, width, height, uvWidth, uvHeight, particle,
                        sourceBase64);
            } catch (Exception e) {
                VitalCore.Log
                        .warning("Error al crear instancia de ModelTexture (UUID: " + uuid + ", Name: " + name + "): "
                                + e.getMessage());
                // Podrías devolver null o lanzar la JsonParseException
                throw new JsonParseException("Fallo al construir ModelTexture: " + name, e);
            }
        }
    }

    /**
     * Deserializador para Animations, incluyendo el manejo detallado de keyframes.
     */
    public static class AnimationDeserializer implements JsonDeserializer<Animation> {

        // Helper interno temporal para agrupar datos por tiempo
        private static class IntermediateKeyframeData {
            Vector3f position = null;
            Vector3f rotationDeg = null; // Guardamos Euler en grados temporalmente
            Vector3f scale = null;
            String interpolation = "linear"; // Default o tomar el último encontrado
        }

        @Override
        public Animation deserialize(final JsonElement jsonElement, final Type typeOfT,
                final JsonDeserializationContext context) throws JsonParseException {

            if (!jsonElement.isJsonObject()) {
                throw new JsonParseException("Se esperaba un objeto JSON para Animation");
            }
            JsonObject animationObj = jsonElement.getAsJsonObject();
            UUID uuidValue = GsonUtils.get(animationObj, "uuid", GsonUtils::getAsUUID);
            String uuid = (uuidValue != null) ? uuidValue.toString() : null;
            String name = GsonUtils.get(animationObj, "name", JsonElement::getAsString);
            LoopType loop = LoopType.fromJsonKey(GsonUtils.get(animationObj, "loop", JsonElement::getAsString));
            boolean override = GsonUtils.get(animationObj, "override", JsonElement::getAsBoolean, false);
            Float lengthValue = GsonUtils.get(animationObj, "length", JsonElement::getAsFloat);
            float length = (lengthValue != null) ? lengthValue : 0.0f;

            Animation animation = new Animation(name, loop, length, uuid, override);

            if (animationObj.has("animators") && animationObj.get("animators").isJsonObject()) {
                JsonObject animatorsObj = animationObj.getAsJsonObject("animators");

                for (Map.Entry<String, JsonElement> animatorEntry : animatorsObj.entrySet()) {
                    String animatorKey = animatorEntry.getKey();
                    if (!animatorEntry.getValue().isJsonObject())
                        continue;

                    JsonObject animatorDataJson = animatorEntry.getValue().getAsJsonObject();
                    String animatorName = GsonUtils.get(animatorDataJson, "name", JsonElement::getAsString);
                    String animatorType = GsonUtils.get(animatorDataJson, "type", JsonElement::getAsString);
                    String parsedAnimatorUuid = null;

                    if (!"effects".equalsIgnoreCase(animatorKey)) { // "effects" no es un UUID
                        try {
                            parsedAnimatorUuid = UUID.fromString(animatorKey).toString();
                        } catch (IllegalArgumentException e) {
                            if (animatorName == null)
                                animatorName = animatorKey;
                        }
                    } else {
                        if (animatorName == null)
                            animatorName = "effects"; // Asegurar que el animator de efectos tenga un nombre
                        animatorType = "effect"; // Asegurar que el tipo sea "effect"
                    }

                    if (parsedAnimatorUuid == null && !"effects".equalsIgnoreCase(animatorName)
                            && animatorType == null) {
                        if (animatorName == null)
                            animatorName = animatorKey;
                        animatorType = "bone";
                    }

                    Map<Float, IntermediateKeyframeData> timeGroupedData = new TreeMap<>();
                    List<Keyframe> finalKeyframesForAnimator = new ArrayList<>();

                    if (animatorDataJson.has("keyframes") && animatorDataJson.get("keyframes").isJsonArray()) {
                        JsonArray keyframesArray = animatorDataJson.getAsJsonArray("keyframes");
                        for (JsonElement keyframeElement : keyframesArray) {
                            if (!keyframeElement.isJsonObject())
                                continue;
                            JsonObject kfObj = keyframeElement.getAsJsonObject();

                            float timeKf = GsonUtils.get(kfObj, "time", JsonElement::getAsFloat, -1.0f);
                            String channel = GsonUtils.get(kfObj, "channel", JsonElement::getAsString);
                            String interpolation = GsonUtils.get(kfObj, "interpolation", JsonElement::getAsString,
                                    "linear");

                            if (timeKf < 0 || channel == null)
                                continue;

                            JsonArray dataPointsArray = GsonUtils.get(kfObj, "data_points",
                                    JsonElement::getAsJsonArray);
                            if (dataPointsArray == null || dataPointsArray.isEmpty())
                                continue;

                            // Procesar efectos directamente y añadirlos a finalKeyframesForAnimator
                            if ("sound".equalsIgnoreCase(channel) || "particle".equalsIgnoreCase(channel)) {
                                if (dataPointsArray.get(0).isJsonPrimitive()
                                        && dataPointsArray.get(0).getAsJsonPrimitive().isString()) {
                                    String effectData = dataPointsArray.get(0).getAsString();
                                    Keyframe effectKf = new Keyframe(timeKf, channel, interpolation);
                                    effectKf.setEffect(effectData);
                                    finalKeyframesForAnimator.add(effectKf);
                                }
                                continue; // Pasar al siguiente keyframe del JSON
                            }

                            // Para canales de transformación, usar IntermediateKeyframeData
                            if (dataPointsArray.get(0).isJsonObject()) {
                                JsonObject dataPoint = dataPointsArray.get(0).getAsJsonObject();
                                try {
                                    float x = Float
                                            .parseFloat(GsonUtils.get(dataPoint, "x", JsonElement::getAsString, "0"));
                                    float y = Float
                                            .parseFloat(GsonUtils.get(dataPoint, "y", JsonElement::getAsString, "0"));
                                    float z = Float
                                            .parseFloat(GsonUtils.get(dataPoint, "z", JsonElement::getAsString, "0"));
                                    Vector3f value = new Vector3f(x, y, z);

                                    IntermediateKeyframeData intermediateData = timeGroupedData.computeIfAbsent(timeKf,
                                            k -> new IntermediateKeyframeData());
                                    intermediateData.interpolation = interpolation; // Usar la interpolación del canal
                                                                                    // actual

                                    switch (channel.toLowerCase()) {
                                        case "position":
                                            intermediateData.position = value;
                                            break;
                                        case "rotation":
                                            intermediateData.rotationDeg = value; // Guardar como Euler grados
                                            break;
                                        case "scale":
                                            intermediateData.scale = value;
                                            break;
                                    }
                                } catch (NumberFormatException e) {
                                    VitalCore.Log
                                            .warning("WARN: No se pudo parsear data_point numérico en keyframe para "
                                                    + animatorName + " time " + timeKf + ": " + dataPoint + " - "
                                                    + e.getMessage());
                                }
                            }
                        }
                    }

                    // Convertir IntermediateKeyframeData agrupada en Keyframes "delgados" finales
                    for (Map.Entry<Float, IntermediateKeyframeData> entry : timeGroupedData.entrySet()) {
                        float time = entry.getKey();
                        IntermediateKeyframeData intermediateData = entry.getValue();
                        String commonInterpolation = intermediateData.interpolation; // La interpolación para este
                                                                                     // tiempo

                        if (intermediateData.position != null) {
                            Keyframe posKf = new Keyframe(time, "position", commonInterpolation);
                            posKf.setPosition(intermediateData.position);
                            finalKeyframesForAnimator.add(posKf);
                        }
                        if (intermediateData.rotationDeg != null) {
                            Keyframe rotKf = new Keyframe(time, "rotation", commonInterpolation);
                            // Usar MathUtils o el helper local. Asumiendo que MathUtils.fromEulerZYX es
                            // correcto.
                            // Blockbench: X=Pitch, Y=Yaw, Z=Roll (en el exportador de JSON)
                            // MathUtils.fromEulerZYX espera (pitch, yaw, roll)
                            rotKf.setRotation(new Quaternionf(MathUtils.fromEulerZYX(new Vector3d(
                                    intermediateData.rotationDeg.x(), // Pitch
                                    intermediateData.rotationDeg.y(), // Yaw
                                    intermediateData.rotationDeg.z() // Roll
                            ))));
                            finalKeyframesForAnimator.add(rotKf);
                        }
                        if (intermediateData.scale != null) {
                            Keyframe scaleKf = new Keyframe(time, "scale", commonInterpolation);
                            scaleKf.setScale(intermediateData.scale);
                            finalKeyframesForAnimator.add(scaleKf);
                        }
                    }
                    // Ordenar los keyframes finales por tiempo, ya que ahora se mezclan efectos y
                    // transformaciones
                    finalKeyframesForAnimator.sort((kf1, kf2) -> Float.compare(kf1.getTime(), kf2.getTime()));

                    animation.addAnimator(new Animator(parsedAnimatorUuid != null ? parsedAnimatorUuid : animatorKey,
                                    animatorName, animatorType, finalKeyframesForAnimator));
                }
            }
            return animation;
        }

    }

    /**
     * Deserializador para FaceInfo.
     */
    public static class FaceDeserializer implements JsonDeserializer<Face> {
        @Override
        public Face deserialize(JsonElement jsonElement, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {

            if (!jsonElement.isJsonObject()) {
                throw new JsonParseException("Se esperaba un objeto JSON para deserializar FaceInfo");
            }
            JsonObject jsonObj = jsonElement.getAsJsonObject();

            // Extraer UV - Usa context si necesitas deserializar tipos específicos
            // Para float[], Gson por defecto suele funcionar, pero usar context es más
            // seguro.
            Float[] uv = GsonUtils.get(jsonObj, "uv", e -> {
                JsonArray jsonArray = e.getAsJsonArray();
                Float[] result = new Float[jsonArray.size()];
                for (int i = 0; i < jsonArray.size(); i++) {
                    result[i] = jsonArray.get(i).getAsFloat();
                }
                return result;
            });
            Integer rotationValue = GsonUtils.get(jsonObj, "rotation", JsonElement::getAsInt);
            Integer rotation = (rotationValue != null) ? rotationValue : 0;
            Integer textureId = GsonUtils.get(jsonObj, "texture", JsonElement::getAsInt);

            return new Face(uv, textureId, rotation);
        }
    }

}