package me.winflix.vitalcore.addons.model.data.animation;


/**
 * Representa un único keyframe en una línea de tiempo de animación.
 * Contiene el valor en un tiempo específico y el tipo de interpolación
 * hacia el siguiente keyframe.
 *
 * @param <T> El tipo de dato que este keyframe almacena (ej. Vector3f para posición).
 */
public class AnimationKeyframe<T> {

    private final float time; // Tiempo en segundos desde el inicio de la animación.
    private final T value;    // El valor de la propiedad animada en este 'time'.
    private final String interpolationType; // Tipo de interpolación al siguiente frame (ej. "linear", "step").
    // Podríamos añadir campos para argumentos de Bezier si se soportan más adelante.
    // private float[] bezierArgs;

    /**
     * Constructor para AnimationKeyframe.
     * @param time El tiempo de este keyframe.
     * @param value El valor en este keyframe.
     * @param interpolationType El tipo de interpolación hacia el siguiente keyframe.
     */
    public AnimationKeyframe(float time, T value, String interpolationType) {
        this.time = time;
        this.value = value;
        this.interpolationType = interpolationType != null ? interpolationType.toLowerCase() : "linear";
    }

    public float getTime() {
        return time;
    }

    public T getValue() {
        return value;
    }

    public String getInterpolationType() {
        return interpolationType;
    }

    // Si se implementa Bezier:
    // public float[] getBezierArgs() { return bezierArgs; }
    // public void setBezierArgs(float[] bezierArgs) { this.bezierArgs = bezierArgs; }

    @Override
    public String toString() {
        return "AnimationKeyframe{" +
               "time=" + time +
               ", value=" + value +
               ", interpolationType='" + interpolationType + '\'' +
               '}';
    }
}