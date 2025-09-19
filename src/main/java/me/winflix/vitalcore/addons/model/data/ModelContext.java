package me.winflix.vitalcore.addons.model.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Contiene la información necesaria para crear una instancia de un modelo.
 * Define dónde y cómo se debe crear la instancia (ubicación, tipo,
 * entidad/bloque asociado, etc.).
 * Utiliza el patrón Builder para una construcción más legible.
 */
public class ModelContext {

    private final Location location; // Ubicación principal (obligatoria)
    private final InstanceType instanceType; // Tipo de instancia a crear (obligatorio)
    @Nullable
    private final Entity targetEntity; // Entidad a la que vincularse (si aplica)
    @Nullable
    private final Player owner; // Jugador propietario (si aplica)
    @Nullable
    private final Block targetBlock; // Bloque al que vincularse (si aplica)
    private final Map<String, Object> customData; // Datos adicionales para handlers específicos

    // Constructor privado, usar el Builder
    private ModelContext(Builder builder) {
        this.location = Objects.requireNonNull(builder.location, "Location cannot be null in ModelContext");
        this.instanceType = Objects.requireNonNull(builder.instanceType, "InstanceType cannot be null in ModelContext");
        this.targetEntity = builder.targetEntity;
        this.owner = builder.owner;
        this.targetBlock = builder.targetBlock;
        this.customData = Collections.unmodifiableMap(new HashMap<>(builder.customData));
    }

    /**
     * Obtiene una copia de la ubicación principal para esta instancia.
     * Usado por ModelInstance como la ubicación inicial.
     * 
     * @return Location clonada.
     */
    public Location getInitialLocation() {
        return location.clone();
    }

    /**
     * Alias para getInitialLocation() por consistencia si se usa en otros lados.
     * 
     * @return Location clonada.
     */
    public Location getLocation() {
        return location.clone();
    }

    /**
     * Obtiene el tipo de instancia que se debe crear.
     * 
     * @return El InstanceType.
     */
    public InstanceType getInstanceType() {
        return instanceType;
    }

    /**
     * Obtiene la entidad objetivo opcional a la que se vinculará el modelo.
     * Relevante principalmente para InstanceType.ENTITY.
     * 
     * @return Optional<Entity>.
     */
    public Optional<Entity> getTargetEntity() {
        return Optional.ofNullable(targetEntity);
    }

    /**
     * Obtiene el jugador propietario opcional de la instancia.
     * 
     * @return Optional<Player>.
     */
    public Optional<Player> getOwner() {
        return Optional.ofNullable(owner);
    }

    /**
     * Obtiene el bloque objetivo opcional al que se vinculará el modelo.
     * Relevante principalmente para InstanceType.STATIC_BLOCK.
     * 
     * @return Optional<Block>.
     */
    public Optional<Block> getTargetBlock() {
        return Optional.ofNullable(targetBlock);
    }

    /**
     * Obtiene una vista inmutable del mapa de datos personalizados.
     * 
     * @return Map<String, Object> inmutable.
     */
    public Map<String, Object> getCustomDataView() {
        return customData; // Ya es inmutable desde el constructor
    }

    /**
     * Obtiene un dato personalizado específico por su clave.
     * 
     * @param key La clave del dato.
     * @return El valor asociado o null si no existe.
     */
    @Nullable
    public Object getCustomData(String key) {
        return customData.get(key);
    }

    /**
     * Obtiene un dato personalizado específico, intentando castearlo al tipo
     * deseado.
     * 
     * @param key  La clave del dato.
     * @param type La clase esperada del valor.
     * @return Un Optional conteniendo el valor casteado si existe y es del tipo
     *         correcto, o Optional.empty().
     */
    public <T> Optional<T> getCustomData(String key, Class<T> type) {
        Object value = customData.get(key);
        if (type != null && type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    // --- Builder ---
    public static class Builder {
        private Location location;
        private InstanceType instanceType = InstanceType.STATIC_LOCATION; // Default
        private Entity targetEntity;
        private Player owner;
        private Block targetBlock;
        private final Map<String, Object> customData = new HashMap<>();

        /**
         * Inicia la construcción de un ModelContext con la ubicación principal.
         * 
         * @param location La ubicación donde se creará o a la que se vinculará la
         *                 instancia. No puede ser null.
         */
        public Builder(@Nonnull Location location) {
            this.location = Objects.requireNonNull(location,
                    "Initial location cannot be null for ModelContext.Builder");
        }

        /**
         * Establece el tipo de instancia a crear.
         * 
         * @param type El InstanceType (e.g., ENTITY, STATIC_LOCATION). No puede ser
         *             null.
         * @return Este Builder.
         */
        public Builder type(@Nonnull InstanceType type) {
            this.instanceType = Objects.requireNonNull(type, "InstanceType cannot be null");
            return this;
        }

        /**
         * Establece la entidad objetivo a la que se vinculará el modelo.
         * Si se establece, el InstanceType se cambiará implícitamente a ENTITY si no lo
         * era ya.
         * 
         * @param entity La entidad objetivo (puede ser null).
         * @return Este Builder.
         */
        public Builder targetEntity(@Nullable Entity entity) {
            this.targetEntity = entity;
            if (entity != null && this.instanceType != InstanceType.ENTITY) {
                this.instanceType = InstanceType.ENTITY;
            }
            return this;
        }

        /**
         * Establece el bloque objetivo al que se vinculará el modelo.
         * Si se establece, el InstanceType se cambiará implícitamente a STATIC_BLOCK si
         * no lo era ya.
         * 
         * @param block El bloque objetivo (puede ser null).
         * @return Este Builder.
         */
        public Builder targetBlock(@Nullable Block block) {
            this.targetBlock = block;
            if (block != null && this.instanceType != InstanceType.STATIC_BLOCK) {
                this.instanceType = InstanceType.STATIC_BLOCK;
            }
            return this;
        }

        /**
         * Establece el jugador propietario de la instancia.
         * 
         * @param player El jugador propietario (puede ser null).
         * @return Este Builder.
         */
        public Builder owner(@Nullable Player player) {
            this.owner = player;
            return this;
        }

        /**
         * Añade un dato personalizado al contexto. Útil para pasar información
         * específica a handlers concretos (e.g., duración para GESTURE).
         * 
         * @param key   La clave del dato.
         * @param value El valor del dato.
         * @return Este Builder.
         */
        public Builder customData(String key, Object value) {
            if (key != null && value != null) {
                this.customData.put(key, value);
            }
            return this;
        }

        /**
         * Construye el objeto ModelContext final.
         * 
         * @return Un ModelContext inmutable.
         */
        public ModelContext build() {
            if (instanceType == InstanceType.ENTITY && targetEntity == null) {
                System.out.println(
                        "Warning: Building ModelContext with type ENTITY but no targetEntity provided. EntityModelHandler might create a default one or fail.");
            }
            if (instanceType == InstanceType.STATIC_BLOCK && targetBlock == null) {
                throw new IllegalStateException(
                        "Cannot build ModelContext with type STATIC_BLOCK without a targetBlock.");
            }
            return new ModelContext(this);
        }
    }

    // --- Enum para Tipos de Instancia ---
    public enum InstanceType {
        ENTITY, // Vinculado a una entidad existente o nueva (invisible).
        STATIC_LOCATION, // Modelo estático en una coordenada mundial fija.
        STATIC_BLOCK, // Modelo estático vinculado a un bloque específico.
        GESTURE, // Efecto visual temporal, no necesariamente vinculado.
        ITEM_PLACED, // Representa un item colocado en el mundo (similar a BLOCK o LOCATION).
        CUSTOM // Para tipos definidos externamente por desarrolladores.
    }
}
