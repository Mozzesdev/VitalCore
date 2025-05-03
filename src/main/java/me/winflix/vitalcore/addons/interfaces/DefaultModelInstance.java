package me.winflix.vitalcore.addons.interfaces;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;

/**
 * Implementación por defecto de ModelInstance.
 * Almacena la referencia al modelo procesado, ubicación, dueño, UUID
 * y las entidades ItemDisplay que representan los huesos.
 */
public class DefaultModelInstance implements ModelInstance {

    private final ProcessedBbModel model; // Almacena el modelo PROCESADO
    private final Location baseLocation;
    private final Player owner; // Puede ser null
    private final UUID instanceUuid;
    private final Map<String, ItemDisplay> boneEntities = new HashMap<>(); // Mapa Hueso UUID -> Entidad Display
    private final ModelHandler handler; 

     /**
     * Constructor para DefaultModelInstance.
     * @param model El modelo PROCESADO (ProcessedBbModel) a instanciar.
     * @param location La ubicación base para la instancia.
     * @param owner El jugador dueño (puede ser null).
     * @param handler El ModelHandler que gestionará esta instancia. <-- NUEVO PARÁMETRO
     */
    public DefaultModelInstance(ProcessedBbModel model, Location location, Player owner, ModelHandler handler) {
        this.model = Objects.requireNonNull(model, "ProcessedBbModel no puede ser nulo");
        this.baseLocation = Objects.requireNonNull(location, "Location no puede ser nula").clone();
        this.owner = owner;
        this.handler = Objects.requireNonNull(handler, "ModelHandler no puede ser nulo");
        this.instanceUuid = UUID.randomUUID();
    }

    @Override
    public ProcessedBbModel getModel() {
        return model;
    }

    @Override
    public Location getBaseLocation() {
        return baseLocation.clone(); // Devolver copia para evitar modificación externa
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public UUID getInstanceUuid() {
        return instanceUuid;
    }

    /**
     * Devuelve una vista no modificable del mapa de entidades.
     * Para añadir/quitar entidades, usar los métodos específicos.
     */
    @Override
    public Map<String, ItemDisplay> getBoneEntities() {
        return Collections.unmodifiableMap(boneEntities);
    }

    @Override
    public ModelHandler getHandler() {
        return this.handler;
    }

    // --- Métodos para uso interno (ej. por EntityModelHandler) ---

    /**
     * Añade o actualiza la entidad ItemDisplay asociada a un UUID de hueso.
     * Usado por el ModelHandler durante el spawn.
     * 
     * @param boneUuid El UUID del hueso.
     * @param display  La entidad ItemDisplay correspondiente.
     */
    public void addBoneEntity(String boneUuid, ItemDisplay display) {
        if (boneUuid != null && display != null) {
            // Podrías añadir lógica aquí para remover/reemplazar una entidad antigua si ya
            // existe
            // ItemDisplay oldDisplay = this.boneEntities.put(boneUuid, display);
            // if (oldDisplay != null && oldDisplay != display && oldDisplay.isValid()) {
            // oldDisplay.remove(); // Limpiar entidad antigua si se reemplaza
            // }
            this.boneEntities.put(boneUuid, display);
        }
    }

    /**
     * Limpia el mapa de entidades de huesos.
     * Usado por el ModelHandler durante la destrucción de la instancia.
     * (Nota: Esto solo limpia el mapa, no remueve las entidades del mundo,
     * eso debe hacerlo el ModelHandler antes de llamar a esto).
     */
    public void clearBoneEntities() {
        this.boneEntities.clear();
    }

    /**
     * Obtiene una entidad específica por el UUID de su hueso.
     * 
     * @param boneUuid El UUID del hueso.
     * @return El ItemDisplay asociado, o null si no existe.
     */
    public ItemDisplay getEntityForBone(String boneUuid) {
        return this.boneEntities.get(boneUuid);
    }

    // --- Sobrescritura de equals y hashCode basada en el UUID de la instancia ---

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DefaultModelInstance that = (DefaultModelInstance) o;
        return instanceUuid.equals(that.instanceUuid); // La igualdad se basa en el UUID de la INSTANCIA
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceUuid); // El hashCode se basa en el UUID de la INSTANCIA
    }
}