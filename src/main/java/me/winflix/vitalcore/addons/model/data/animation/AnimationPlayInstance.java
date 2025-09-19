package me.winflix.vitalcore.addons.model.data.animation;

import me.winflix.vitalcore.addons.model.data.BbModel.Animation.LoopType;

/**
 * Representa una instancia de una animación que se está reproduciendo activamente
 * en un ModelInstance.
 * Rastrea el tiempo actual, velocidad, estado de bucle, y fases de interpolación (lerp).
 */
public class AnimationPlayInstance {

    private final RuntimeAnimation runtimeAnimation; // La definición de la animación que se está reproduciendo
    private float currentTime; // Tiempo actual de la animación en segundos
    private float speed;       // Multiplicador de velocidad (1.0f es normal)
    private LoopType loopType; // Cómo debe comportarse la animación al llegar al final
    private boolean isPlaying;
    
    // Para interpolación suave (blending) entre animaciones
    private float lerpInDuration;
    private float lerpOutDuration;
    private float currentLerpTime; // Tiempo actual en la fase de lerp
    private Phase currentPhase;

    public enum Phase {
        LERPIN,  // Interpolando hacia adentro (comenzando)
        PLAY,    // Reproducción normal
        LERPOUT, // Interpolando hacia afuera (terminando)
        STOPPED  // Detenida y lista para ser removida
    }

    /**
     * Constructor para AnimationPlayInstance.
     * @param runtimeAnimation La RuntimeAnimation a reproducir.
     * @param speed Velocidad de reproducción.
     * @param loopType Modo de bucle.
     * @param lerpInDuration Duración de la interpolación de entrada en segundos.
     * @param lerpOutDuration Duración de la interpolación de salida en segundos.
     */
    public AnimationPlayInstance(RuntimeAnimation runtimeAnimation, float speed, LoopType loopType, float lerpInDuration, float lerpOutDuration) {
        this.runtimeAnimation = runtimeAnimation;
        this.speed = speed;
        this.loopType = loopType; // Puede ser sobreescrito desde el constructor si es diferente al de RuntimeAnimation
        this.lerpInDuration = Math.max(0, lerpInDuration);
        this.lerpOutDuration = Math.max(0, lerpOutDuration);
        
        this.currentTime = 0.0f;
        this.currentLerpTime = 0.0f;
        this.isPlaying = false; // Se activa con play()
        this.currentPhase = Phase.STOPPED;
    }

    /**
     * Inicia la reproducción de la animación.
     */
    public void play() {
        this.isPlaying = true;
        if (this.lerpInDuration > 0.001f) {
            this.currentPhase = Phase.LERPIN;
            this.currentLerpTime = 0.0f;
        } else {
            this.currentPhase = Phase.PLAY;
            this.currentLerpTime = 0.0f; // No se usa en PLAY, pero se resetea
        }
        this.currentTime = 0.0f; // Reiniciar tiempo de animación al empezar
    }

    /**
     * Detiene la reproducción de la animación.
     * @param immediate Si es true, se detiene inmediatamente (STOPPED). Si es false, pasa por LERPOUT si está configurado.
     */
    public void stop(boolean immediate) {
        if (!isPlaying && currentPhase == Phase.STOPPED) return;

        if (immediate || this.lerpOutDuration <= 0.001f) {
            this.currentPhase = Phase.STOPPED;
            this.isPlaying = false;
        } else {
            if (this.currentPhase != Phase.LERPOUT) { // Evitar reiniciar LERPOUT si ya está en ello
                this.currentPhase = Phase.LERPOUT;
                this.currentLerpTime = 0.0f; // Iniciar lerpOut desde el principio
            }
        }
    }

    /**
     * Avanza el estado de la animación basado en el deltaTime.
     * @param deltaTime El tiempo transcurrido desde el último tick, en segundos.
     */
    public void tick(float deltaTime) {
        if (!isPlaying && currentPhase != Phase.LERPOUT) { // Si no se está reproduciendo y no está en LERPOUT, no hacer nada
             if(currentPhase == Phase.STOPPED) return; // Completamente detenida
        }

        float effectiveSpeed = this.speed; // Podría ser modificado por factores externos si es necesario

        switch (currentPhase) {
            case LERPIN:
                currentLerpTime += deltaTime;
                if (currentLerpTime >= lerpInDuration) {
                    currentPhase = Phase.PLAY;
                    currentLerpTime = 0.0f; // Reseteado para posible uso futuro o claridad
                }
                // La animación principal también avanza durante LERPIN
                advanceMainAnimationTime(deltaTime, effectiveSpeed);
                break;

            case PLAY:
                advanceMainAnimationTime(deltaTime, effectiveSpeed);
                break;

            case LERPOUT:
                currentLerpTime += deltaTime;
                 // La animación principal puede o no avanzar durante LERPOUT, depende del diseño.
                 // Por ahora, la detendremos o la dejaremos en el último frame.
                 // Si queremos que continúe: advanceMainAnimationTime(deltaTime, effectiveSpeed);
                
                if (currentLerpTime >= lerpOutDuration) {
                    currentPhase = Phase.STOPPED;
                    isPlaying = false; // Marcar como no reproduciendo activamente
                }
                break;

            case STOPPED:
                // No hacer nada
                return;
        }
    }

    private void advanceMainAnimationTime(float deltaTime, float effectiveSpeed) {
        currentTime += deltaTime * effectiveSpeed;
        float animationLength = runtimeAnimation.getLength();

        if (currentTime >= animationLength) {
            switch (this.loopType) {
                case ONCE:
                    currentTime = animationLength; // Clavar al final
                    stop(false); // Iniciar LERPOUT si existe, sino STOPPED
                    break;
                case LOOP:
                    currentTime = currentTime % animationLength; // Envolver
                    // Si hay LERPIN, podría reiniciarse aquí, pero usualmente no para LOOP.
                    break;
                case HOLD:
                    currentTime = animationLength; // Mantener en el último frame
                    // No se detiene, simplemente se queda en el último frame.
                    // Si se quisiera que 'stop()' tenga efecto aún en HOLD, se necesitaría lógica adicional.
                    break;
            }
        }
    }

    /**
     * @return True si la animación ha terminado su ciclo de vida (LERPOUT completado o detenida).
     */
    public boolean isFinished() {
        return currentPhase == Phase.STOPPED;
    }

    /**
     * Calcula el ratio de interpolación (0.0 a 1.0) basado en la fase actual.
     * 1.0 significa que la animación tiene influencia total.
     * @return El ratio de lerp.
     */
    public float getLerpWeight() {
        switch (currentPhase) {
            case LERPIN:
                return (lerpInDuration > 0.001f) ? Math.min(1.0f, currentLerpTime / lerpInDuration) : 1.0f;
            case PLAY:
                return 1.0f;
            case LERPOUT:
                return (lerpOutDuration > 0.001f) ? Math.max(0.0f, 1.0f - (currentLerpTime / lerpOutDuration)) : 0.0f;
            case STOPPED:
                return 0.0f;
        }
        return 0.0f; // Fallback
    }

    public RuntimeAnimation getRuntimeAnimation() {
        return runtimeAnimation;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public float getSpeed() {
        return speed;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public LoopType getLoopType() {
        return loopType;
    }

    public void setLoopType(LoopType loopType) {
        this.loopType = loopType;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }
    
    public boolean isPlaying() {
        return isPlaying || currentPhase == Phase.LERPOUT; // Considerar LERPOUT como aún "activo" en cierto sentido
    }
}