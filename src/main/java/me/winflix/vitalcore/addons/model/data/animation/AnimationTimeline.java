package me.winflix.vitalcore.addons.model.data.animation;

import me.winflix.vitalcore.addons.model.data.EffectKeyframe;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Contiene todas las líneas de tiempo de interpolación (posición, rotación, escala, efectos)
 * para un hueso específico dentro de una animación, o para efectos globales de la animación.
 */
public class AnimationTimeline {

    // Interpoladores para cada canal de transformación.
    private final AnimationKeyframeInterpolator<Vector3f> positionInterpolator;
    private final AnimationKeyframeInterpolator<Quaternionf> rotationInterpolator;
    private final AnimationKeyframeInterpolator<Vector3f> scaleInterpolator;
    // Interpolador para efectos especiales (sonidos, partículas).
    private final AnimationKeyframeInterpolator<List<EffectKeyframe>> effectInterpolator;

    /**
     * Constructor para AnimationTimeline.
     * Inicializa los interpoladores para cada tipo de dato.
     */
    public AnimationTimeline() {
        this.positionInterpolator = new AnimationKeyframeInterpolator<>(Vector3f.class);
        this.rotationInterpolator = new AnimationKeyframeInterpolator<>(Quaternionf.class);
        this.scaleInterpolator = new AnimationKeyframeInterpolator<>(Vector3f.class);
        // Nota: EffectKeyframe ya es una lista en BbModel.Animation.Keyframe,
        // pero aquí podríamos querer una lista de esas listas si un solo keyframe de efectos
        // en el JSON pudiera definir múltiples efectos a la vez.
        // Por simplicidad inicial, asumimos que un keyframe de efectos en el JSON
        // se traduce a una List<EffectKeyframe> para ese tiempo.
        this.effectInterpolator = new AnimationKeyframeInterpolator<List<EffectKeyframe>>(null);
    }

    /**
     * Añade un keyframe de posición.
     * @param time Tiempo del keyframe.
     * @param value Valor del Vector3f de posición.
     * @param interpolationType Tipo de interpolación al siguiente keyframe.
     */
    public void addPositionKeyframe(float time, Vector3f value, String interpolationType) {
        this.positionInterpolator.addRawKeyframe(time, value, interpolationType);
    }

    /**
     * Añade un keyframe de rotación.
     * @param time Tiempo del keyframe.
     * @param value Valor del Quaternionf de rotación.
     * @param interpolationType Tipo de interpolación al siguiente keyframe.
     */
    public void addRotationKeyframe(float time, Quaternionf value, String interpolationType) {
        this.rotationInterpolator.addRawKeyframe(time, value, interpolationType);
    }

    /**
     * Añade un keyframe de escala.
     * @param time Tiempo del keyframe.
     * @param value Valor del Vector3f de escala.
     * @param interpolationType Tipo de interpolación al siguiente keyframe.
     */
    public void addScaleKeyframe(float time, Vector3f value, String interpolationType) {
        this.scaleInterpolator.addRawKeyframe(time, value, interpolationType);
    }

    /**
     * Añade un keyframe de efectos.
     * @param time Tiempo del keyframe.
     * @param effects Lista de EffectKeyframes para este tiempo.
     * @param interpolationType Tipo de interpolación (generalmente "step" para efectos).
     */
    public void addEffectKeyframe(float time, List<EffectKeyframe> effects, String interpolationType) {
        this.effectInterpolator.addRawKeyframe(time, effects, interpolationType);
    }
    
    /**
     * Obtiene la posición interpolada en un tiempo específico.
     * @param time El tiempo de la animación.
     * @return El Vector3f de posición interpolado.
     */
    public Vector3f getPosition(float time) {
        return positionInterpolator.interpolate(time);
    }

    /**
     * Obtiene la rotación interpolada en un tiempo específico.
     * @param time El tiempo de la animación.
     * @return El Quaternionf de rotación interpolado.
     */
    public Quaternionf getRotation(float time) {
        return rotationInterpolator.interpolate(time);
    }

    /**
     * Obtiene la escala interpolada en un tiempo específico.
     * @param time El tiempo de la animación.
     * @return El Vector3f de escala interpolado.
     */
    public Vector3f getScale(float time) {
        return scaleInterpolator.interpolate(time);
    }

    /**
     * Obtiene los efectos que deben activarse entre el tiempo anterior y el tiempo actual.
     * @param previousTime El tiempo del tick anterior.
     * @param currentTime El tiempo del tick actual.
     * @return Una lista de EffectKeyframes a activar.
     */
    public List<EffectKeyframe> getEffects(float previousTime, float currentTime) {
        return effectInterpolator.getEffectsInRange(previousTime, currentTime);
    }

    // Getters para los interpoladores si se necesita acceso directo (opcional)
    public AnimationKeyframeInterpolator<Vector3f> getPositionInterpolator() {
        return positionInterpolator;
    }

    public AnimationKeyframeInterpolator<Quaternionf> getRotationInterpolator() {
        return rotationInterpolator;
    }

    public AnimationKeyframeInterpolator<Vector3f> getScaleInterpolator() {
        return scaleInterpolator;
    }

    public AnimationKeyframeInterpolator<List<EffectKeyframe>> getEffectInterpolator() {
        return effectInterpolator;
    }
    
    public boolean hasPositionKeyframes() {
        return !positionInterpolator.isEmpty();
    }

    public boolean hasRotationKeyframes() {
        return !rotationInterpolator.isEmpty();
    }

    public boolean hasScaleKeyframes() {
        return !scaleInterpolator.isEmpty();
    }

    public boolean hasEffectKeyframes() {
        return !effectInterpolator.isEmpty();
    }
}