package me.winflix.vitalcore.addons.interfaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import me.winflix.vitalcore.addons.handlers.EntityModelHandler;
import me.winflix.vitalcore.addons.utils.MathUtils;

public class BbModel {
    private final String name;
    private final List<Bone> bones;
    private final List<Cube> elements;
    private final List<Animation> animations;
    private final Map<Integer, ModelTexture> textures;
    private ModelHandler handler = new EntityModelHandler();
    private final int baseCustomModelData;

    public BbModel(String name, List<Bone> bones, List<Cube> elements, List<Animation> animations,
            Map<Integer, ModelTexture> textures) {
        this.name = name;
        this.bones = bones;
        this.animations = animations;
        this.textures = textures;
        this.baseCustomModelData = Math.abs(name.hashCode() % 900_000) + 100_000;
        this.elements = Objects.requireNonNullElseGet(elements, ArrayList::new);
    }

    public String getName() {
        return name;
    }

    public List<Bone> getBones() {
        return bones;
    }

    public List<Cube> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public List<Animation> getAnimations() {
        return animations;
    }

    public Map<Integer, ModelTexture> getTextures() {
        return textures;
    }

    public ModelHandler getHandler() {
        return handler;
    }

    public void setHandler(ModelHandler handler) {
        this.handler = handler;
    }

    public int getBaseCustomModelData() {
        return baseCustomModelData;
    }

    /**
     * Encuentra el hueso padre de un hueso dado.
     * Utiliza la referencia directa al padre almacenada en la propia clase Bone.
     *
     * @param childBone El hueso del cual se quiere obtener el padre.
     * @return El objeto Bone padre, o null si childBone es null o no tiene padre
     *         (es un hueso raíz).
     */
    public Bone getParentBone(Bone childBone) {
        if (childBone == null) {
            return null;
        }
        // Simplemente llamamos al getter que ya existe en la clase Bone
        return childBone.getParent();
    }

    /**
     * Obtiene el CustomModelData único para un hueso específico dentro de este
     * modelo.
     * Estrategia: CMD Base del Modelo + Índice del Hueso + 1.
     *
     * @param targetBone El hueso para el cual obtener el CMD.
     * @return El CustomModelData único para ese hueso, o 0 si el hueso no pertenece
     *         a este modelo.
     */
    public int getBoneCustomModelData(Bone targetBone) {
        if (targetBone == null || this.bones == null) {
            System.err.println(
                    "Error en getBoneCustomModelData: Hueso objetivo o lista de huesos es nula para modelo " + name);
            return 0;
        }

        int boneIndex = this.bones.indexOf(targetBone); // Busca el hueso en la lista

        if (boneIndex == -1) {
            // El hueso proporcionado no fue encontrado en la lista de este modelo.
            System.err.println("Advertencia en getBoneCustomModelData: El hueso '" + targetBone.getName()
                    + "' no se encontró en la lista del modelo '" + this.name + "'. Devolviendo CMD 0.");
            return 0;
        }

        // Calcula el CMD: Base + índice + 1 (para evitar colisión con el base si el
        // índice es 0)
        return this.baseCustomModelData + boneIndex + 1;
    }

    public List<Bone> getRootBones() {
        return bones.stream().filter(b -> getParentBone(b) == null).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\": \"").append(escapeJsonString(getName())).append("\"");
        sb.append(", \"bones_hierarchy\": [");
        List<Bone> rootBones = getRootBones();
        if (!rootBones.isEmpty()) {
            StringJoiner rootBoneJoiner = new StringJoiner(",");
            for (Bone rootBone : rootBones) {
                rootBoneJoiner.add(buildBoneJson(rootBone));
            }
            sb.append(rootBoneJoiner.toString());
        }
        sb.append("]");
        sb.append(", \"elements_count\": ").append(getElements().size());
        sb.append(", \"animations_count\": ").append(getAnimations().size());
        sb.append(", \"textures_count\": ").append(getTextures().size());
        sb.append(", \"baseCustomModelData\": ").append(getBaseCustomModelData());
        sb.append("}");
        return sb.toString();
    }

    /**
     * Método auxiliar recursivo para construir la representación JSON de un hueso
     * y sus descendientes.
     * 
     * @param bone El hueso a representar.
     * @return String con la representación JSON del subárbol del hueso.
     */
    /**
     * Método auxiliar recursivo para construir la representación JSON de un hueso,
     * incluyendo sus cubos asociados y sus huesos hijos.
     * 
     * @param bone El hueso a representar.
     * @return String con la representación JSON del subárbol del hueso.
     */
    private String buildBoneJson(Bone bone) {
        if (bone == null) {
            return "null";
        }
        StringBuilder boneSb = new StringBuilder();
        boneSb.append("{");
        // Información básica del Hueso
        boneSb.append("\"name\": \"").append(escapeJsonString(bone.getName())).append("\"");
        boneSb.append(", \"uuid\": \"").append(bone.getUuid()).append("\"");

        // Array de Cubos asociados DIRECTAMENTE a este hueso
        List<Cube> cubes = bone.getCubes(); // Obtiene los cubos de ESTE hueso
        boneSb.append(", \"cubes\": [");
        if (cubes != null && !cubes.isEmpty()) {
            StringJoiner cubeJoiner = new StringJoiner(",");
            for (Cube cube : cubes) {
                // Representación simplificada del cubo: {"name": "...", "uuid": "..."}
                // Asegúrate de que cube.getName() no sea null o maneja ese caso.
                String cubeName = cube.getName() != null ? cube.getName() : "unnamed_cube";
                cubeJoiner.add(String.format("{\"name\": \"%s\", \"uuid\": \"%s\"}",
                        escapeJsonString(cubeName),
                        cube.getUuid()));
            }
            boneSb.append(cubeJoiner.toString());
        }
        boneSb.append("]"); // Cierra el array de cubos

        // Array de Huesos Hijos (recursivo)
        List<Bone> childBones = bone.getChildren(); // Obtiene los huesos hijos
        boneSb.append(", \"child_bones\": ["); // Nombre cambiado para claridad
        if (childBones != null && !childBones.isEmpty()) {
            StringJoiner childrenJoiner = new StringJoiner(",");
            for (Bone child : childBones) {
                childrenJoiner.add(buildBoneJson(child)); // Llamada recursiva
            }
            boneSb.append(childrenJoiner.toString());
        }
        boneSb.append("]"); // Cierra el array de child_bones

        boneSb.append("}");
        return boneSb.toString();
    }

    /**
     * Método auxiliar simple para escapar caracteres especiales en strings JSON.
     * Para producción, se recomienda usar una librería JSON como Jackson o Gson.
     * 
     * @param value El string a escapar.
     * @return El string escapado o "null" si el input es null.
     */
    private String escapeJsonString(String value) {
        if (value == null) {
            return "null";
        }
        // Escapa comillas dobles y barras invertidas
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static class Bone {
        private final String uuid;
        private final String name;
        private Bone parent = null;
        private final Float[] pivot;
        private final Float[] rotation;
        private final List<Cube> cubes = new ArrayList<>();
        private final List<String> cubesUuid = new ArrayList<>();
        private final List<Bone> children = new ArrayList<>();
        private boolean isIgnored;

        public Bone(String uuid, String name, Float[] pivot, Float[] rotation) {
            this.uuid = Objects.requireNonNull(uuid, "UUID no puede ser nulo");
            this.name = Objects.requireNonNull(name, "Nombre no puede ser nulo");
            this.pivot = pivot;
            this.rotation = rotation;
        }

        public String getName() {
            return name;
        }

        public List<String> getCubesUuid() {
            return cubesUuid;
        }

        public void addCubeUuid(String uuid) {
            cubesUuid.add(uuid);
        }

        public void clearCubeUuids() {
            cubesUuid.clear();
        }

        public Float[] getPivot() {
            return pivot;
        }

        public Float[] getRotation() {
            return rotation;
        }

        public String getUuid() {
            return uuid;
        }

        public List<Bone> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public Bone getParent() {
            return parent;
        }

        public void addCube(Cube cube) {
            if (cube != null) {
                this.cubes.add(cube);
            }
        }

        public List<Cube> getCubes() {
            return Collections.unmodifiableList(cubes);
        }

        public void addChildBone(Bone child) {
            children.add(child);
        }

        public void setParent(Bone parent) {
            this.parent = parent;
        }

        public Bone getChildByName(String childName) {
            for (Bone child : children) {
                if (child.getName().equals(childName)) {
                    return child;
                }
                Bone foundInChildren = child.getChildByName(childName);
                if (foundInChildren != null) {
                    return foundInChildren;
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Bone bone = (Bone) o;
            return uuid.equals(bone.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }

    }

    public static class Cube {
        private final String uuid;
        private final String name;
        private final Float[] from;
        private final Float[] to;
        private final Float[] pivot;
        private final Float[] rotation;
        private final float inflate;
        private final Map<String, Face> faces;

        public Cube(String uuid, String name, Float[] from, Float[] to, Float[] pivot, Float[] rotation,
                float inflate,
                Map<String, Face> faces) {
            this.uuid = Objects.requireNonNull(uuid, "Cube UUID no puede ser nulo");
            this.name = name;
            this.from = from;
            this.to = to;
            this.pivot = pivot;
            this.rotation = rotation;
            this.inflate = inflate;
            this.faces = faces != null ? faces : new HashMap<>();
        }

        public float width() {
            return Math.abs(this.to[0] - this.from[0]);
        }
        
        public float height() {
            return Math.abs(this.to[1] - this.from[1]);
        }
        
        public float depth() {
            return Math.abs(this.to[2] - this.from[2]);
        }

        public String getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public Float[] getFrom() {
            return from;
        }

        public Float[] getTo() {
            return to;
        }

        public Float[] getPivot() {
            return pivot;
        }

        public Float[] getRotation() {
            return rotation;
        }

        public float getInflate() {
            return inflate;
        }

        public Map<String, Face> getFaces() {
            return Collections.unmodifiableMap(faces);
        }

        public Face getFace(String direction) {
            return faces.get(direction);
        }

        @Override
        public String toString() {
            return "Cube{" +
                    "uuid='" + uuid + '\'' +
                    ", name='" + name + '\'' +
                    ", from=" + from +
                    ", to=" + to +
                    ", faces=" + faces.size() +
                    '}';
        }
    }

    public static class Face {
        private final Float[] uv;
        private final Integer textureId;
        private final Integer rotation;

        // Constructor
        public Face(Float[] uv, Integer textureId, Integer rotation) {
            if (uv == null || uv.length != 4) {
                this.uv = new Float[] { 0f, 0f, 1f, 1f };
            } else {
                this.uv = uv;
            }
            this.textureId = textureId;
            this.rotation = (rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270) ? rotation : 0;
        }

        public Float[] getUv() {
            return uv;
        }

        public Integer getTextureId() {
            return textureId;
        }

        public Integer getRotation() {
            return rotation;
        }

        public boolean isEmpty() {
            return this.textureId == null || MathUtils.isSimilar(this.uv[0], this.uv[2])
                    || MathUtils.isSimilar(this.uv[1], this.uv[3]);
        }

        @Override
        public String toString() {
            return "Face{" +
                    "uv=" + Arrays.toString(uv) +
                    ", textureId='" + textureId + '\'' +
                    ", rotation=" + rotation +
                    '}';
        }

    }

    public static class Animation {
        private final String name;
        private final String uuid;
        private final LoopType loop;
        private final float length;
        private final boolean override;
        private final List<Animator> animators = new ArrayList<>();

        public Animation(String name, LoopType loop, float length, String uuid, boolean override) {
            this.name = name;
            this.loop = loop;
            this.length = length;
            this.override = override;
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public boolean isLooping() {
            return loop.equals(LoopType.LOOP);
        }

        public float getLength() {
            return length;
        }

        public LoopType getLoop() {
            return loop;
        }

        public String getUuid() {
            return uuid;
        }

        public void addAnimator(Animator animator) {
            if (animator != null) {
                this.animators.add(animator);
            }
        }

        public List<Animator> getAnimators() {
            return Collections.unmodifiableList(animators);
        }

        /**
         * Calcula la transformación LOCAL interpolada para un animator específico (por
         * UUID)
         * en un momento dado de la animación.
         *
         * @param animatorUuid El UUID del hueso/grupo cuya transformación se busca.
         * @param time         El tiempo actual de la animación (en segundos).
         * @param bindPose     La transformación de la pose inicial (bind pose) del
         *                     hueso/grupo,
         *                     usada como fallback. (Quizás necesites obtener esto
         *                     externamente).
         * @return Un objeto InterpolatedTransform con los valores JOML interpolados.
         */
        public InterpolatedTransform getInterpolatedTransform(String animatorUuid, float time,
                Transformation bindPose) {
            // 1. Encontrar el Animator por UUID
            Animator targetAnimator = null;
            for (Animator anim : this.animators) {
                // Podrías querer filtrar aquí por anim.type().equals("bone") si solo buscas
                // huesos
                if (anim.uuid().equals(animatorUuid)) {
                    targetAnimator = anim;
                    break;
                }
            }

            // 2. Animator no encontrado o sin keyframes
            if (targetAnimator == null || targetAnimator.keyframes().isEmpty()) {
                if (bindPose != null) {
                    // Devolver transformación de la bind pose
                    return new InterpolatedTransform(
                            bindPose.getTranslation(),
                            bindPose.getLeftRotation(), // Asume JOML Quaternionf o convierte
                            bindPose.getScale());
                } else {
                    // Devolver transformación identidad por defecto
                    return new InterpolatedTransform(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1));
                }
            }

            // 3. Obtener keyframes (ya deberían estar ordenados por tiempo)
            List<Keyframe> keyframes = targetAnimator.keyframes();

            // --- Lógica de búsqueda de keyframes prev/next e interpolación ---
            // (Esta parte es la misma que tenías antes, pero opera sobre la lista
            // 'keyframes')

            Keyframe firstFrame = keyframes.get(0);
            if (time <= firstFrame.getTime()) {
                return new InterpolatedTransform(firstFrame.getPosition(), firstFrame.getRotation(),
                        firstFrame.getScale());
            }

            Keyframe lastFrame = keyframes.get(keyframes.size() - 1);
            // TODO: Manejar loop si es necesario. Si loop == hold (o no loop), usa el
            // último frame.
            if (time >= lastFrame.getTime()) {
                return new InterpolatedTransform(lastFrame.getPosition(), lastFrame.getRotation(),
                        lastFrame.getScale());
            }

            // Encontrar keyframes prev y next
            Keyframe prevFrame = firstFrame;
            Keyframe nextFrame = firstFrame; // Inicialización segura
            for (int i = 1; i < keyframes.size(); i++) {
                nextFrame = keyframes.get(i);
                if (time <= nextFrame.getTime()) {
                    prevFrame = keyframes.get(i - 1);
                    break;
                }
            }

            // Calcular alpha
            float frameDeltaTime = nextFrame.getTime() - prevFrame.getTime();
            float alpha = (frameDeltaTime > 0.00001f)
                    ? Math.max(0.0f, Math.min(1.0f, (time - prevFrame.getTime()) / frameDeltaTime))
                    : 0.0f;

            // Interpolar (usando el modo de prevFrame.getInterpolation() si lo implementas)
            Vector3f interpolatedPos = new Vector3f();
            prevFrame.getPosition().lerp(nextFrame.getPosition(), alpha, interpolatedPos);

            Quaternionf interpolatedRot = new Quaternionf();
            prevFrame.getRotation().slerp(nextFrame.getRotation(), alpha, interpolatedRot); // SLERP es crucial

            Vector3f interpolatedScale = new Vector3f();
            prevFrame.getScale().lerp(nextFrame.getScale(), alpha, interpolatedScale);

            return new InterpolatedTransform(interpolatedPos, interpolatedRot, interpolatedScale);
        }

        // Clase interna o Record para devolver los resultados de la interpolación
        public record InterpolatedTransform(Vector3f position, Quaternionf rotation, Vector3f scale) {
        }

        public static class Keyframe {
            private final float time;
            private final Quaternionf rotation; // Cambiado a Quaternionf
            private final Vector3f position; // Cambiado a Vector3f
            private final Vector3f scale; // Cambiado a Vector3f
            private final String interpolation;

            public Keyframe(float time, Quaternionf rotation, Vector3f position, Vector3f scale, String interpolation) {
                this.time = time;
                this.rotation = rotation; // Debe venir ya convertido del loader
                this.position = position; // Debe venir ya convertido del loader
                this.scale = scale;
                this.interpolation = interpolation != null ? interpolation : "linear"; // Asegura valor por defecto
            }

            public String getInterpolation() {
                return interpolation;
            }

            public float getTime() {
                return time;
            }

            public Quaternionf getRotation() {
                return rotation;
            }

            public Vector3f getPosition() {
                return position;
            }

            public Vector3f getScale() {
                return scale;
            }
        }

        public enum LoopType {

            LOOP("loop");

            private final String jsonKey;

            LoopType(String jsonKey) {
                this.jsonKey = jsonKey;
            }

            public String getJsonKey() {
                return jsonKey;
            }

            public static LoopType fromJsonKey(String key) {
                if (key == null)
                    return null;
                for (LoopType direction : values()) {
                    if (direction.jsonKey.equalsIgnoreCase(key)) {
                        return direction;
                    }
                }
                return null;
            }
        }

        /**
         * Representa la animación de un componente específico (hueso, grupo, efecto).
         * Contiene los metadatos del componente animado y su lista de keyframes
         * procesados.
         */
        public static record Animator(
                String uuid, // UUID del hueso/grupo/componente animado
                String name, // Nombre del hueso/grupo/componente animado
                String type, // Tipo ("bone", "effect", etc.)
                List<Keyframe> keyframes // Lista ordenada de Keyframes combinados
        ) {
            // Constructor compacto para validación o inmutabilidad de la lista (opcional)
            public Animator {
                Objects.requireNonNull(uuid, "Animator UUID cannot be null");
                Objects.requireNonNull(name, "Animator name cannot be null");
                Objects.requireNonNull(type, "Animator type cannot be null");
                // Devuelve una copia inmutable de la lista para seguridad
                keyframes = keyframes == null ? List.of() : List.copyOf(keyframes);
            }
        }

    }

    public static class ModelTexture {
        private final String uuid;
        private final String id;
        private final String name;
        private final int width, height; // tamaño real
        private final int uvWidth, uvHeight;
        private final boolean particle;
        private final String source; // el field "source" base64
        private final byte[] data; // bytes decodificados

        public ModelTexture(String uuid,
                String id,
                String name,
                int width,
                int height,
                int uvWidth,
                int uvHeight,
                boolean particle,
                String sourceBase64) {
            this.id = id;
            this.uuid = uuid;
            this.name = name;
            this.width = width;
            this.height = height;
            this.uvWidth = uvWidth;
            this.uvHeight = uvHeight;
            this.particle = particle;
            this.source = sourceBase64;
            this.data = decodeBase64(sourceBase64);
        }

        private byte[] decodeBase64(String raw) {
            // raw == "data:image/png;base64,AAA..."
            String b64 = raw.substring(raw.indexOf(',') + 1);
            return Base64.getDecoder().decode(b64);
        }

        // getters
        public String getUuid() {
            return uuid;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getUvWidth() {
            return uvWidth;
        }

        public int getUvHeight() {
            return uvHeight;
        }

        public boolean isParticle() {
            return particle;
        }

        public byte[] getData() {
            return data;
        }

        public String getSource() {
            return source;
        }
    }

}
