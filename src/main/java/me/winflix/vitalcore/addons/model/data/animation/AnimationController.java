package me.winflix.vitalcore.addons.model.data.animation;

import me.winflix.vitalcore.addons.model.data.ModelInstance;
import me.winflix.vitalcore.addons.model.data.ProcessedBbModel;
import me.winflix.vitalcore.addons.model.data.EffectKeyframe;
import me.winflix.vitalcore.addons.model.data.BbModel.Animation.LoopType; // Para LoopType

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Gestiona las instancias de animación activas para un ModelInstance.
 * Se encarga de reproducir, detener, actualizar y mezclar animaciones.
 */
public class AnimationController {

    private final ModelInstance modelInstance; // La instancia del modelo a la que este controlador pertenece
    private final Map<String, AnimationPlayInstance> activeAnimations; // Animaciones activas (nombre -> instancia)
    // Para rastrear el tiempo anterior para la obtención de efectos
    private final Map<AnimationPlayInstance, Float> previousAnimationTime;

    // Estructura para almacenar la transformación final calculada para cada hueso
    public static class BoneTransform {
        public Vector3f position = new Vector3f();
        public Quaternionf rotation = new Quaternionf();
        public Vector3f scale = new Vector3f(1, 1, 1);

        // Método para resetear a la identidad (o estado base)
        public void reset() {
            position.set(0, 0, 0);
            rotation.identity();
            scale.set(1, 1, 1);
        }
    }

    public AnimationController(ModelInstance modelInstance) {
        this.modelInstance = Objects.requireNonNull(modelInstance, "ModelInstance no puede ser nulo");
        this.activeAnimations = new HashMap<>();
        this.previousAnimationTime = new HashMap<>();
    }

    /**
     * Inicia la reproducción de una animación.
     * 
     * @param animationName El nombre de la animación a reproducir (debe existir en
     *                      ProcessedBbModel).
     * @param speed         Velocidad de reproducción.
     * @param lerpIn        Duración de la interpolación de entrada (fade in).
     * @param lerpOut       Duración de la interpolación de salida (fade out).
     * @param loopOverride  Opcional: para sobreescribir el modo de bucle de la
     *                      animación. Null para usar el de la animación.
     * @param force         Si es true, detiene cualquier animación existente con el
     *                      mismo nombre antes de reproducir.
     * @return La instancia de AnimationPlayInstance creada o actualizada, o null si
     *         la animación no existe.
     */
    public AnimationPlayInstance playAnimation(String animationName, float speed, float lerpIn, float lerpOut,
            LoopType loopOverride, boolean force) {
        ProcessedBbModel processedModel = modelInstance.getModel();
        if (processedModel == null)
            return null;

        RuntimeAnimation runtimeAnim = processedModel.getRuntimeAnimations().get(animationName);
        if (runtimeAnim == null) {
            // Log: Animación no encontrada
            return null;
        }

        if (force && activeAnimations.containsKey(animationName)) {
            stopAnimation(animationName, true); // Detener inmediatamente si se fuerza
        }

        AnimationPlayInstance playInstance = activeAnimations.get(animationName);
        if (playInstance == null || playInstance.isFinished()) { // Si no existe o ya terminó
            LoopType actualLoopType = (loopOverride != null) ? loopOverride : runtimeAnim.getLoopType();
            playInstance = new AnimationPlayInstance(runtimeAnim, speed, actualLoopType, lerpIn, lerpOut);
            activeAnimations.put(animationName, playInstance);
            previousAnimationTime.put(playInstance, 0f); // Inicializar tiempo previo
        } else { // Si ya existe y está activa (ej. LERPOUT), podría reiniciarse o ajustar
                 // parámetros
            playInstance.setSpeed(speed);
            if (loopOverride != null)
                playInstance.setLoopType(loopOverride);
            // Aquí se podría añadir lógica para re-configurar lerpIn/Out si la animación se
            // "re-dispara"
        }

        playInstance.play();
        return playInstance;
    }

    public void stopAnimation(String animationName, boolean immediate) {
        AnimationPlayInstance playInstance = activeAnimations.get(animationName);
        if (playInstance != null) {
            playInstance.stop(immediate);
            if (immediate || playInstance.isFinished()) { // Si se detiene inmediatamente o ya terminó el LERPOUT
                previousAnimationTime.remove(playInstance);
            }
        }
    }

    public void stopAllAnimations(boolean immediate) {
        // Crear una copia de las claves para evitar ConcurrentModificationException
        new ArrayList<>(activeAnimations.keySet()).forEach(name -> stopAnimation(name, immediate));
    }

    /**
     * Actualiza el estado de todas las animaciones activas.
     * 
     * @param deltaTime Tiempo transcurrido desde el último tick.
     */
    public void tickController(float deltaTime) {
        Iterator<Map.Entry<String, AnimationPlayInstance>> iterator = activeAnimations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AnimationPlayInstance> entry = iterator.next();
            AnimationPlayInstance playInstance = entry.getValue();

            float prevTime = previousAnimationTime.getOrDefault(playInstance, playInstance.getCurrentTime());

            playInstance.tick(deltaTime);

            // Disparar efectos
            triggerEffectsForInstance(playInstance, prevTime, playInstance.getCurrentTime());
            previousAnimationTime.put(playInstance, playInstance.getCurrentTime());

            if (playInstance.isFinished()) {
                iterator.remove(); // Eliminar si ha terminado completamente
                previousAnimationTime.remove(playInstance);
            }
        }
    }

    private void triggerEffectsForInstance(AnimationPlayInstance playInstance, float prevAnimTime,
            float currentAnimTime) {
        RuntimeAnimation rAnim = playInstance.getRuntimeAnimation();
        if (rAnim == null || modelInstance.getBaseEntity().isEmpty())
            return;

        // Efectos globales de la animación
        AnimationTimeline globalTimeline = rAnim.getGlobalEffectTimeline();
        if (globalTimeline != null && globalTimeline.hasEffectKeyframes()) {
            List<EffectKeyframe> globalEffects = globalTimeline.getEffects(prevAnimTime, currentAnimTime);
            for (EffectKeyframe effect : globalEffects) {
                // Lógica para disparar el efecto global (ej. sonido en la entidad, partícula en
                // la entidad)
                // Ejemplo:
                // modelInstance.getEntity().getWorld().playSound(modelInstance.getEntity().getLocation(),
                // effect.getData(), 1f, 1f);
                System.out.println("Disparando efecto GLOBAL: " + effect.getChannel() + " - " + effect.getEffect() + " en t="
                        + effect.getTime());
            }
        }

        // Efectos de hueso (si los tuvieras definidos así)
        for (Map.Entry<String, AnimationTimeline> boneTimelineEntry : rAnim.getBoneTimelines().entrySet()) {
            String boneUuid = boneTimelineEntry.getKey();
            AnimationTimeline boneTimeline = boneTimelineEntry.getValue();
            if (boneTimeline != null && boneTimeline.hasEffectKeyframes()) {
                List<EffectKeyframe> boneEffects = boneTimeline.getEffects(prevAnimTime, currentAnimTime);
                for (EffectKeyframe effect : boneEffects) {
                    // Lógica para disparar efecto en la posición del hueso
                    // Necesitarías la transformación actual del hueso.
                    // Esto podría requerir que los efectos se procesen después de calcular las
                    // transformaciones de los huesos.
                    // O, si son sonidos que no dependen de la posición exacta del hueso, se pueden
                    // disparar aquí.
                    System.out.println("Disparando efecto de HUESO (" + boneUuid + "): " + effect.getChannel() + " - "
                            + effect.getEffect() + " en t=" + effect.getTime());
                }
            }
        }
    }

    /**
     * Calcula las transformaciones locales finales para cada hueso,
     * combinando todas las animaciones activas.
     * 
     * @return Un mapa de (UUID de hueso -> BoneTransform final).
     */
    public Map<String, BoneTransform> getCurrentBoneTransforms() {
        Map<String, BoneTransform> finalTransforms = new HashMap<>();
        ProcessedBbModel processedModel = modelInstance.getModel();
        if (processedModel == null || activeAnimations.isEmpty()) {
            // Si no hay modelo o animaciones activas, devolver transformaciones identidad
            // para todos los huesos del modelo
            if (processedModel != null) {
                processedModel.getBones().keySet()
                        .forEach(boneUuid -> finalTransforms.put(boneUuid, new BoneTransform()) // Identidad
                        );
            }
            return finalTransforms;
        }

        // Inicializar transformaciones para todos los huesos del modelo
        for (String boneUuid : processedModel.getBones().keySet()) {
            finalTransforms.put(boneUuid, new BoneTransform());
        }

        // Iterar sobre cada hueso para el cual necesitamos calcular una transformación
        for (String boneUuid : processedModel.getBones().keySet()) {
            BoneTransform accumulatedTransform = finalTransforms.get(boneUuid); // Ya inicializado a identidad

            // Acumular transformaciones de todas las animaciones activas
            // Esta es una mezcla simple por ahora. Se podría implementar un sistema de
            // capas/prioridades.
            // La mezcla aditiva es compleja y requiere que las animaciones no-override se
            // apliquen
            // diferencialmente a la pose base o a la animación de override subyacente.

            // Por ahora, la última animación con override "gana", o se promedian/suman.
            // Una implementación simple: la animación con mayor peso de lerp y que sea
            // override, domina.
            // Si no hay overrides, se podrían promediar o usar la de mayor peso.

            // Implementación simplificada:
            // 1. Aplicar la animación de override más fuerte (mayor peso).
            // 2. Luego, aditivamente (si se implementa) o por capas, las otras.

            // Para esta versión, haremos una mezcla ponderada simple (si no hay overrides)
            // o dejaremos que la animación de override más fuerte gane.

            Vector3f totalPosition = new Vector3f();
            Quaternionf finalRotation = new Quaternionf(); // Empezar con identidad para slerp
            Vector3f totalScale = new Vector3f();
            float totalWeight = 0.0f;
            boolean rotationSet = false;

            AnimationPlayInstance dominantOverride = null;
            float maxOverrideWeight = -1.0f;

            // Encontrar la animación de override dominante
            for (AnimationPlayInstance playInstance : activeAnimations.values()) {
                if (!playInstance.isPlaying())
                    continue;
                RuntimeAnimation rAnim = playInstance.getRuntimeAnimation();
                if (rAnim.isOverride()) {
                    float weight = playInstance.getLerpWeight();
                    if (weight > maxOverrideWeight) {
                        maxOverrideWeight = weight;
                        dominantOverride = playInstance;
                    }
                }
            }

            if (dominantOverride != null) { // Si hay una animación de override activa
                RuntimeAnimation rAnim = dominantOverride.getRuntimeAnimation();
                AnimationTimeline timeline = rAnim.getBoneTimeline(boneUuid);
                if (timeline != null) {
                    accumulatedTransform.position = timeline.getPosition(dominantOverride.getCurrentTime());
                    accumulatedTransform.rotation = timeline.getRotation(dominantOverride.getCurrentTime());
                    accumulatedTransform.scale = timeline.getScale(dominantOverride.getCurrentTime());
                }
                // Si hay una override, las demás (incluso otras overrides de menor peso) se
                // ignoran en este modelo simple.
            } else { // Si no hay overrides, mezclar todas las animaciones no-override
                for (AnimationPlayInstance playInstance : activeAnimations.values()) {
                    if (!playInstance.isPlaying() || playInstance.getRuntimeAnimation().isOverride())
                        continue;

                    float weight = playInstance.getLerpWeight();
                    if (weight <= 0.001f)
                        continue;

                    RuntimeAnimation rAnim = playInstance.getRuntimeAnimation();
                    AnimationTimeline timeline = rAnim.getBoneTimeline(boneUuid);

                    if (timeline != null) {
                        totalPosition
                                .add(new Vector3f(timeline.getPosition(playInstance.getCurrentTime())).mul(weight));

                        Quaternionf animRotation = timeline.getRotation(playInstance.getCurrentTime());
                        if (!rotationSet) {
                            finalRotation.set(animRotation); // Primera rotación
                            // Podríamos ponderar el ángulo de la primera rotación aquí también, pero slerp
                            // lo maneja.
                            rotationSet = true;
                        } else {
                            finalRotation.slerp(animRotation, weight / (totalWeight + weight)); // Slerp ponderado
                        }
                        // La escala aditiva es usualmente (valor - 1.0) * peso + 1.0, luego producto.
                        // O simplemente promediar:
                        totalScale.add(new Vector3f(timeline.getScale(playInstance.getCurrentTime())).mul(weight));
                        totalWeight += weight;
                    }
                }

                if (totalWeight > 0.001f) {
                    accumulatedTransform.position.set(totalPosition.div(totalWeight)); // Promedio ponderado
                    accumulatedTransform.rotation.set(finalRotation); // Resultado del slerp acumulativo
                    accumulatedTransform.scale.set(totalScale.div(totalWeight)); // Promedio ponderado
                    // Asegurar que la escala no sea cero si el peso total es pequeño pero no cero.
                    if (accumulatedTransform.scale.lengthSquared() < 0.0001f && totalWeight > 0) {
                        accumulatedTransform.scale.set(1, 1, 1); // Fallback a escala identidad
                    }
                } else {
                    // No active non-override animations, defaults to identity (already set)
                }
            }
            finalTransforms.put(boneUuid, accumulatedTransform);
        }
        return finalTransforms;
    }
}