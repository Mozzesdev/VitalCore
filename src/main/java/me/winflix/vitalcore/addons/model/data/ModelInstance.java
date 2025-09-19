// Path: me/winflix/vitalcore/addons/model/data/ModelInstance.java
// Adaptado para integrarse con ModelEngineManager (vc_model_engine_manager_fused)
// y para permitir que ModelHandler añada entidades de hueso.
package me.winflix.vitalcore.addons.model.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional; 
import java.util.UUID;

import javax.annotation.Nullable; 

import org.bukkit.Location;
import org.bukkit.entity.Entity; 
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.model.data.BbModel.Animation.LoopType;
import me.winflix.vitalcore.addons.model.data.animation.AnimationController;
import me.winflix.vitalcore.addons.model.data.animation.AnimationPlayInstance;

public class ModelInstance {
    private final UUID instanceUUID;
    private final String modelId; 
    private ProcessedBbModel processedModel; 
    
    private Entity baseEntity; 
    private Location currentLocation; 
    
    // Cambiado a no final para permitir que el handler lo pueble si es necesario
    private final Map<String, ItemDisplay> boneEntities = new HashMap<>(); 
    private final Map<String, Interaction> boneInteractions = new HashMap<>(); 

    private boolean visible = true;
    private float scale = 1.0f; 

    private final AnimationController animationController; 
    private final ModelContext context; 
    private final ModelHandler handler; 

    public ModelInstance(ProcessedBbModel model, ModelContext context, ModelHandler handler) {
        this.instanceUUID = UUID.randomUUID();
        this.processedModel = Objects.requireNonNull(model, "ProcessedBbModel no puede ser nulo");
        this.modelId = model.getName(); 
        this.context = Objects.requireNonNull(context, "ModelContext no puede ser nulo");
        this.handler = Objects.requireNonNull(handler, "ModelHandler no puede ser nulo");

        this.baseEntity = context.getTargetEntity().get(); 
        this.currentLocation = Objects.requireNonNull(context.getInitialLocation(), "InitialLocation en ModelContext no puede ser nula");
        
        this.animationController = new AnimationController(this);

        // No llamar a spawnModelParts() aquí si el handler es responsable de ello.
        // Si ModelInstance DEBE spawnear sus propias partes en algunos casos,
        // esta lógica necesitaría ser condicional o invocada por el handler.
        // Por ahora, se asume que EntityModelHandler se encargará del spawn.
    }

    /**
     * Método para que los ModelHandlers registren las entidades ItemDisplay de los huesos.
     * Debe ser llamado por el ModelHandler durante la creación de la instancia.
     * @param boneUuid El UUID del hueso.
     * @param display La entidad ItemDisplay asociada.
     */
    public void addBoneEntity(String boneUuid, ItemDisplay display) {
        if (boneUuid != null && display != null) {
            this.boneEntities.put(boneUuid, display);
        }
    }
    
    /**
     * Método para que los ModelHandlers registren las entidades Interaction de los huesos.
     * @param boneUuid El UUID del hueso.
     * @param interaction La entidad Interaction asociada.
     */
    public void addBoneInteraction(String boneUuid, Interaction interaction) {
        if (boneUuid != null && interaction != null) {
            this.boneInteractions.put(boneUuid, interaction);
        }
    }


    // Este método es conceptual. La actualización real la hace el handler.
    public void updateModelPose() {
        // La lógica de transformación está en EntityModelHandler.updateBoneRecursive
    }

    public void setLocation(Location location) {
        this.currentLocation = Objects.requireNonNull(location, "Location no puede ser nula.");
    }

    public Location getCurrentLocation() {
        if (baseEntity != null && baseEntity.isValid()) {
            return baseEntity.getLocation();
        }
        return currentLocation;
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) return;
        this.visible = visible;
        // El ModelHandler aplicará esta visibilidad.
    }

    public boolean isVisible() {
        return visible;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public void remove() {
        synchronized (boneEntities) {
            boneEntities.values().forEach(display -> {
                if (display != null && display.isValid()) {
                    display.remove();
                }
            });
            boneEntities.clear();
        }
        synchronized (boneInteractions) {
            boneInteractions.values().forEach(interaction -> {
                if (interaction != null && interaction.isValid()) {
                    interaction.remove();
                }
            });
            boneInteractions.clear();
        }
    }

    public UUID getInstanceUuid() {
        return instanceUUID;
    }

    public String getModelId() {
        return modelId;
    }
    
    public ProcessedBbModel getModel() { 
        return processedModel;
    }
    
    public void setProcessedModel(ProcessedBbModel newModel) {
        if (this.processedModel == newModel && newModel != null) return;
        if (this.processedModel == null && newModel == null) return;
        
        if (this.animationController != null) {
            this.animationController.stopAllAnimations(true);
        }
        remove(); 
        this.processedModel = newModel; 
        
        if (this.processedModel != null) { 
            if (this.currentLocation == null && this.baseEntity != null && this.baseEntity.isValid()) {
                this.currentLocation = this.baseEntity.getLocation();
            }
            // El handler será responsable de re-spawnear las partes si es necesario
            // llamando a su propia lógica de spawn y usando addBoneEntity.
            if (this.handler != null) {
                 // Potencialmente, el handler podría necesitar un método para "reconstruir" la instancia.
                 // Por ahora, la responsabilidad de re-spawnear recae en cómo se usa setProcessedModel externamente.
                 // O, ModelInstance podría tener un método spawnParts() que el handler llama.
            } else if (this.currentLocation == null) {
                 VitalCore.Log.warning("[ModelInstance] No se pudieron re-spawnear partes del modelo para " + instanceUUID + " debido a ubicación nula y sin entidad base válida y sin handler.");
            }
        }
        setVisible(this.visible); 
    }

    public Optional<Entity> getBaseEntity() {
        return Optional.ofNullable(baseEntity);
    }

    public void setBaseEntity(@Nullable Entity entity) {
        this.baseEntity = entity;
        if (entity != null && entity.isValid()) { 
            this.currentLocation = entity.getLocation(); 
        }
    }
    
    public Map<String, ItemDisplay> getBoneEntities() { 
        return Collections.unmodifiableMap(boneEntities);
    }
    
    public Map<String, Interaction> getBoneInteractions() {
        return Collections.unmodifiableMap(boneInteractions);
    }

    public ModelContext getContext() {
        return context;
    }

    public ModelHandler getHandler() {
        return handler;
    }

    public AnimationController getAnimationController() {
        return animationController;
    }

    public AnimationPlayInstance playAnimation(String animationName, float speed, float lerpIn, float lerpOut, @Nullable LoopType loopOverride, boolean force) {
        if (animationController == null) {
            VitalCore.Log.warning("[ModelInstance] AnimationController es nulo para la instancia " + instanceUUID);
            return null;
        }
        return animationController.playAnimation(animationName, speed, lerpIn, lerpOut, loopOverride, force);
    }

    public void stopAnimation(String animationName, boolean immediate) {
        if (animationController != null) {
            animationController.stopAnimation(animationName, immediate);
        }
    }

    public void stopAllAnimations(boolean immediate) {
        if (animationController != null) {
            animationController.stopAllAnimations(immediate);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelInstance that = (ModelInstance) o;
        return instanceUUID.equals(that.instanceUUID);
    }

    @Override
    public int hashCode() {
        return instanceUUID.hashCode();
    }

    @Override
    public String toString() {
        return "ModelInstance{" +
               "instanceUUID=" + instanceUUID +
               ", modelId='" + modelId + '\'' +
               ", baseEntity=" + (baseEntity != null ? baseEntity.getType() : "null") +
               ", visible=" + visible +
               ", scale=" + scale +
               '}';
    }
}
