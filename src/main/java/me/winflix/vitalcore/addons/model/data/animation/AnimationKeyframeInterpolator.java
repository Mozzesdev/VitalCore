package me.winflix.vitalcore.addons.model.data.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import me.winflix.vitalcore.addons.model.data.EffectKeyframe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Almacena y gestiona la interpolación de keyframes para un canal específico
 * (posición, rotación, escala o efectos) de una animación.
 *
 * @param <T> El tipo de dato que este interpolador maneja (ej. Vector3f,
 *            Quaternionf).
 */
public class AnimationKeyframeInterpolator<T> {

    private final TreeMap<Float, AnimationKeyframe<T>> keyframes;
    private final Class<T> valueType; // Para ayudar con la interpolación específica del tipo

    /**
     * Constructor.
     * 
     * @param valueType La clase del tipo de valor que se interpola (Vector3f.class,
     *                  Quaternionf.class, etc.).
     */
    public AnimationKeyframeInterpolator(Class<T> valueType) {
        this.keyframes = new TreeMap<>();
        this.valueType = valueType;
    }

    /**
     * Añade un keyframe "crudo" directamente desde los datos parseados.
     * 
     * @param time              El tiempo del keyframe.
     * @param value             El valor del keyframe.
     * @param interpolationType El tipo de interpolación (ej. "linear", "step").
     */
    public void addRawKeyframe(float time, T value, String interpolationType) {
        this.keyframes.put(time, new AnimationKeyframe<>(time, value, interpolationType));
    }

    /**
     * Interpola el valor en un tiempo específico.
     * 
     * @param time El tiempo en la animación para el cual obtener el valor
     *             interpolado.
     * @return El valor interpolado.
     */
    @SuppressWarnings("unchecked")
    public T interpolate(float time) {
        if (keyframes.isEmpty()) {
            // Devuelve un valor por defecto según el tipo si no hay keyframes
            if (valueType == Vector3f.class) {
                return (T) new Vector3f(); // Posición/Escala por defecto
            } else if (valueType == Quaternionf.class) {
                return (T) new Quaternionf(); // Rotación identidad
            }
            // Para List<EffectKeyframe> u otros, podría ser null o una lista vacía
            return null;
        }

        Map.Entry<Float, AnimationKeyframe<T>> floorEntry = keyframes.floorEntry(time);
        Map.Entry<Float, AnimationKeyframe<T>> ceilEntry = keyframes.ceilingEntry(time);

        if (floorEntry == null) { // Tiempo antes del primer keyframe
            return keyframes.firstEntry().getValue().getValue();
        }
        if (ceilEntry == null) { // Tiempo después del último keyframe
            return keyframes.lastEntry().getValue().getValue();
        }

        AnimationKeyframe<T> prevKeyframe = floorEntry.getValue();
        AnimationKeyframe<T> nextKeyframe = ceilEntry.getValue();

        if (prevKeyframe.getTime() == nextKeyframe.getTime()) { // Exactamente en un keyframe
            return prevKeyframe.getValue();
        }

        // Determinar el tipo de interpolación desde el keyframe PREVIO
        String interpolation = prevKeyframe.getInterpolationType();
        float t = (time - prevKeyframe.getTime()) / (nextKeyframe.getTime() - prevKeyframe.getTime());

        if ("step".equalsIgnoreCase(interpolation)) {
            return prevKeyframe.getValue();
        } else { // Por defecto, "linear" (o catmullrom/bezier si se implementan)
            // Implementación específica por tipo
            if (prevKeyframe.getValue() instanceof Vector3f && nextKeyframe.getValue() instanceof Vector3f) {
                Vector3f prevVec = (Vector3f) prevKeyframe.getValue();
                Vector3f nextVec = (Vector3f) nextKeyframe.getValue();
                return (T) new Vector3f().lerp(prevVec, t, nextVec);
            } else if (prevKeyframe.getValue() instanceof Quaternionf
                    && nextKeyframe.getValue() instanceof Quaternionf) {
                Quaternionf prevQuat = (Quaternionf) prevKeyframe.getValue();
                Quaternionf nextQuat = (Quaternionf) nextKeyframe.getValue();
                return (T) new Quaternionf().slerp(prevQuat, t, nextQuat);
            }
            // Para List<EffectKeyframe>, la interpolación no aplica, se retornan los
            // efectos en el rango.
            // Este método se enfoca en valores continuos. Los efectos se manejarán de forma
            // diferente.
        }
        // Fallback si el tipo no es manejado o la interpolación es desconocida
        return prevKeyframe.getValue();
    }

    /**
     * Obtiene todos los keyframes de efectos que ocurren entre previousTime
     * (exclusivo) y currentTime (inclusivo).
     * Este método es específico para interpoladores de tipo List<EffectKeyframe>.
     * 
     * @param previousTime El tiempo del tick anterior.
     * @param currentTime  El tiempo del tick actual.
     * @return Una lista de EffectKeyframes.
     */
    @SuppressWarnings("unchecked")
    public List<EffectKeyframe> getEffectsInRange(float previousTime, float currentTime) {
        if (!List.class.isAssignableFrom(valueType)) {
            // Opcionalmente, lanzar una excepción si se llama en un interpolador incorrecto
            return new ArrayList<>();
        }
        List<EffectKeyframe> triggeredEffects = new ArrayList<>();
        // Iteramos sobre los keyframes cuyos tiempos son > previousTime y <=
        // currentTime
        for (Map.Entry<Float, AnimationKeyframe<T>> entry : keyframes.subMap(previousTime, false, currentTime, true)
                .entrySet()) {
            // Asumimos que el valor es una List<EffectKeyframe> o un solo EffectKeyframe
            // Para simplificar, si el valor es una lista, añadimos todos sus elementos.
            // Si Blockbench solo permite un efecto por "data_point" en un keyframe de
            // efecto,
            // entonces getValue() podría devolver un solo EffectKeyframe.
            // Aquí asumimos que AnimationKeyframe<List<EffectKeyframe>> almacena la lista
            // directamente.
            Object val = entry.getValue().getValue();
            if (val instanceof List) {
                triggeredEffects.addAll((List<EffectKeyframe>) val);
            } else if (val instanceof EffectKeyframe) {
                triggeredEffects.add((EffectKeyframe) val);
            }
        }
        return triggeredEffects;
    }

    public boolean isEmpty() {
        return keyframes.isEmpty();
    }

    public int getKeyframeCount() {
        return keyframes.size();
    }

    @Override
    public String toString() {
        return "AnimationKeyframeInterpolator{" +
                "keyframes=" + keyframes.size() + " frames" +
                ", valueType=" + valueType.getSimpleName() +
                '}';
    }
}