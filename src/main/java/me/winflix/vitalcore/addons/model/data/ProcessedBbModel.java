package me.winflix.vitalcore.addons.model.data;

import me.winflix.vitalcore.addons.model.data.animation.RuntimeAnimation; // Importar RuntimeAnimation

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representa la estructura procesada y lista para usar en runtime de un modelo
 * BbModel.
 * Separa los datos crudos de los datos necesarios para la lógica del juego,
 * incluyendo las animaciones procesadas.
 */
public class ProcessedBbModel {
    private final String name;
    private final Map<String, ProcessedBone> bones; // Mapa de UUID de hueso a ProcessedBone
    private final ProcessedPackData packData;
    private final Map<String, RuntimeAnimation> runtimeAnimations; // Mapa de nombre de animación a RuntimeAnimation

    /**
     * Constructor para ProcessedBbModel.
     * @param name El nombre del modelo.
     * @param bones Un mapa de todos los huesos procesados en el modelo (UUID -> ProcessedBone).
     * @param packData Los datos del paquete de recursos asociados con este modelo.
     */
    public ProcessedBbModel(String name, Map<String, ProcessedBone> bones, ProcessedPackData packData) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.bones = Objects.requireNonNull(bones, "Bones map cannot be null");
        this.packData = packData; // packData puede ser null si no hay recursos que generar
        this.runtimeAnimations = new HashMap<>(); // Inicializar el mapa de animaciones
    }

    public String getName() {
        return name;
    }

    public ProcessedPackData getPackData() {
        return packData;
    }

    /**
     * Obtiene un mapa inmutable de todos los huesos procesados en este modelo.
     * La clave es el UUID del hueso.
     * @return Un mapa de UUID de hueso a ProcessedBone.
     */
    public Map<String, ProcessedBone> getBones() {
        return Collections.unmodifiableMap(bones);
    }

    /**
     * Obtiene un mapa inmutable de las animaciones procesadas para este modelo.
     * La clave es el nombre de la animación.
     * @return Un mapa de nombre de animación a RuntimeAnimation.
     */
    public Map<String, RuntimeAnimation> getRuntimeAnimations() {
        return Collections.unmodifiableMap(runtimeAnimations);
    }

    /**
     * Añade una animación procesada al modelo.
     * Este método es típicamente llamado por ModelProcessor.
     * @param animation La RuntimeAnimation a añadir.
     */
    public void addRuntimeAnimation(RuntimeAnimation animation) {
        if (animation != null && animation.getName() != null) {
            this.runtimeAnimations.put(animation.getName(), animation);
        }
    }


    @Override
    public String toString() {
        return "ProcessedBbModel{" +
               "name='" + name + '\'' +
               ", bonesCount=" + (bones != null ? bones.size() : 0) +
               ", packDataExists=" + (packData != null) +
               ", runtimeAnimationsCount=" + runtimeAnimations.size() +
               '}';
    }
}