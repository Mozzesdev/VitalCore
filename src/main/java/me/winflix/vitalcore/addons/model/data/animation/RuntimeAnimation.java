package me.winflix.vitalcore.addons.model.data.animation;

import me.winflix.vitalcore.addons.model.data.BbModel; // Para LoopType

import java.util.HashMap;
import java.util.Map;

/**
 * Almacena la información procesada y lista para el runtime de una animación específica.
 * Contiene las líneas de tiempo para cada hueso afectado por esta animación.
 */
public class RuntimeAnimation {

    private final String name;
    private final float length; // Duración en segundos
    private final BbModel.Animation.LoopType loopType; // Tomado de BbModel.Animation.LoopType
    private final boolean override; // Si esta animación debe reemplazar completamente otras o ser aditiva

    // Mapa de UUID de hueso (o nombre único del animator) a su AnimationTimeline
    private final Map<String, AnimationTimeline> boneTimelines;
    // Línea de tiempo para efectos globales de la animación (scripts, sonidos globales)
    private final AnimationTimeline globalEffectTimeline;

    /**
     * Constructor para RuntimeAnimation.
     * @param name Nombre de la animación.
     * @param length Duración de la animación en segundos.
     * @param loopType El modo de bucle (ONCE, LOOP, HOLD).
     * @param override Si la animación es de override.
     */
    public RuntimeAnimation(String name, float length, BbModel.Animation.LoopType loopType, boolean override) {
        this.name = name;
        this.length = length;
        this.loopType = loopType;
        this.override = override;
        this.boneTimelines = new HashMap<>();
        this.globalEffectTimeline = new AnimationTimeline(); // Timeline para efectos globales
    }

    public String getName() {
        return name;
    }

    public float getLength() {
        return length;
    }

    public BbModel.Animation.LoopType getLoopType() {
        return loopType;
    }

    public boolean isOverride() {
        return override;
    }

    /**
     * Obtiene el AnimationTimeline para un hueso específico (identificado por su UUID o nombre de animator).
     * Si no existe, crea uno nuevo y lo devuelve.
     * @param boneAnimatorId El UUID o nombre del animator del hueso.
     * @return El AnimationTimeline para ese hueso.
     */
    public AnimationTimeline getOrCreateBoneTimeline(String boneAnimatorId) {
        return boneTimelines.computeIfAbsent(boneAnimatorId, k -> new AnimationTimeline());
    }
    
    /**
     * Obtiene el AnimationTimeline para un hueso específico (identificado por su UUID o nombre de animator).
     * @param boneAnimatorId El UUID o nombre del animator del hueso.
     * @return El AnimationTimeline para ese hueso, o null si no existe.
     */
    public AnimationTimeline getBoneTimeline(String boneAnimatorId) {
        return boneTimelines.get(boneAnimatorId);
    }


    public Map<String, AnimationTimeline> getBoneTimelines() {
        return boneTimelines;
    }

    public AnimationTimeline getGlobalEffectTimeline() {
        return globalEffectTimeline;
    }

    @Override
    public String toString() {
        return "RuntimeAnimation{" +
               "name='" + name + '\'' +
               ", length=" + length +
               ", loopType=" + loopType +
               ", override=" + override +
               ", boneTimelinesCount=" + boneTimelines.size() +
               ", hasGlobalEffects=" + !globalEffectTimeline.getEffectInterpolator().isEmpty() +
               '}';
    }
}