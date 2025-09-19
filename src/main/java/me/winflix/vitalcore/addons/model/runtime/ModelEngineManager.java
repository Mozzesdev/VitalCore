package me.winflix.vitalcore.addons.model.runtime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonObject;

import me.winflix.rsmanager.api.ResourcePackAPI;
import me.winflix.rsmanager.api.exceptions.ResourcePackApiException;
import me.winflix.rsmanager.interfaces.ResourceType;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.handlers.EntityModelHandler;
import me.winflix.vitalcore.addons.model.data.BbModel;
import me.winflix.vitalcore.addons.model.data.ModelContext;
import me.winflix.vitalcore.addons.model.data.ModelHandler;
import me.winflix.vitalcore.addons.model.data.ModelInstance;
import me.winflix.vitalcore.addons.model.data.ProcessedBbModel;
import me.winflix.vitalcore.addons.model.data.ProcessedPackData;
import me.winflix.vitalcore.addons.model.loader.BlockbenchLoader;
import me.winflix.vitalcore.addons.model.processor.ModelProcessor;

/**
 * Gestiona la carga de .bbmodel, el spawn/destroy/tick de instancias,
 * y registra los recursos del modelo usando los métodos específicos de
 * ResourcePackAPI.
 */
public class ModelEngineManager {

    private final Map<String, ProcessedBbModel> loadedRuntimeModels = new HashMap<>();
    private final File modelDir;
    private final List<ModelInstance> activeInstances = Collections.synchronizedList(new ArrayList<>());
    private final String METADATA_KEY = "addons_custom_model";
    private final Map<ModelContext.InstanceType, ModelHandler> registeredHandlers = new HashMap<>();

    // --- Referencias a la API y al plugin ---
    private final VitalCore owningPlugin;
    private final ResourcePackAPI resourcePackApi;
    private final String pluginNamespace;

    public ModelEngineManager(VitalCore owningPlugin, @Nullable ResourcePackAPI resourcePackApi,
            String pluginNamespace) {
        this.modelDir = new File(owningPlugin.getDataFolder(), "addons/models");
        this.owningPlugin = owningPlugin;
        this.resourcePackApi = resourcePackApi;
        this.pluginNamespace = pluginNamespace;

        if (!modelDir.exists())
            modelDir.mkdirs();
        registerHandlers();
        loadAllModels();
        startTickTask();
    }

    private void registerHandlers() {
        registerHandler(ModelContext.InstanceType.ENTITY, new EntityModelHandler());
    }

    public void registerHandler(ModelContext.InstanceType type, ModelHandler handler) {
        if (type == null || handler == null)
            return;
        registeredHandlers.put(type, handler);
    }

    @Nullable
    public ModelHandler getHandlerForType(ModelContext.InstanceType type) {
        return registeredHandlers.get(type);
    }

    /**
     * Carga todos los modelos desde el directorio y registra sus recursos
     * usando la ResourcePackAPI si está disponible.
     */
    public void loadAllModels() {
        synchronized (loadedRuntimeModels) {
            if (resourcePackApi != null) {
                for (ProcessedBbModel oldModel : loadedRuntimeModels.values()) {
                    removeModelResourcesFromApi(oldModel);
                }
            }
            loadedRuntimeModels.clear();
        }

        File[] files = modelDir.listFiles((d, n) -> n.toLowerCase().endsWith(".bbmodel"));
        if (files == null || files.length == 0) {
            VitalCore.Log.warning("[ModelEngine] No se encontraron archivos .bbmodel en: " + modelDir.getPath());
            return;
        }

        VitalCore.Log
                .info("[ModelEngine] Cargando, procesando y registrando recursos para " + files.length + " modelos...");
        boolean needsRegeneration = false;

        for (File f : files) {
            BbModel rawModel = BlockbenchLoader.load(f);
            if (rawModel != null) {
                ProcessedBbModel proBbModel = ModelProcessor.process(rawModel);
                if (proBbModel != null) {
                    synchronized (loadedRuntimeModels) {
                        loadedRuntimeModels.put(proBbModel.getName(), proBbModel);
                    }
                    VitalCore.Log.info(proBbModel.getName());
                    if (addModelResourcesToApi(proBbModel)) {
                        needsRegeneration = true;
                    }
                } else {
                    VitalCore.Log.warning("[ModelEngine] Error al procesar modelo desde archivo: " + f.getName());
                }

            } else {
                VitalCore.Log.warning("[ModelEngine] Error al cargar modelo crudo desde archivo: " + f.getName());
            }
        }

        if (needsRegeneration && resourcePackApi != null) {
            resourcePackApi.regeneratePack(false);
        } else if (needsRegeneration) {
            VitalCore.Log.warning(
                    "[ModelEngine] Se modificaron recursos, pero la API de CentralResourcePack no está disponible para solicitar regeneración.");
        }
    }

    public String getPluginNamespace() {
        return pluginNamespace;
    }

    public ResourcePackAPI getResourcePackApi() {
        return resourcePackApi;
    }

    /**
     * Intenta añadir los recursos de un modelo (texturas, JSON de geometría y JSON
     * de item)
     * a la API centralizada usando los métodos específicos.
     *
     * @param model El modelo procesado cuyos recursos se añadirán.
     * @return true si se añadió o modificó al menos un recurso, false en caso
     *         contrario o si la API no está disponible.
     */
    private boolean addModelResourcesToApi(ProcessedBbModel model) {
        if (resourcePackApi == null || model == null || model.getPackData() == null) {
            return false;
        }

        boolean resourceAddedOrModified = false;
        ProcessedPackData packData = model.getPackData();
        String modelNameForPath = model.getName().toLowerCase().replaceAll("[^a-z0-9/._-]", "_");

        // 1. Añadir texturas usando addTextureFile
        if (packData.getTexturesUsed() != null) {
            for (BbModel.ModelTexture texture : packData.getTexturesUsed()) {
                if (texture == null || texture.getData() == null || texture.getName() == null)
                    continue;
                String textureFileName = texture.getName().toLowerCase();
                if (!textureFileName.endsWith(".png"))
                    continue;
                String relativePath = modelNameForPath + "/" + textureFileName;
                try (InputStream textureStream = new ByteArrayInputStream(texture.getData())) {
                    // *** USAR MÉTODO ESPECÍFICO ***
                    resourcePackApi.addTextureFile(owningPlugin, relativePath, textureStream);
                    resourceAddedOrModified = true;
                } catch (ResourcePackApiException e) {
                    VitalCore.Log.log(Level.SEVERE,
                            "[ModelEngine] Error al añadir textura '" + relativePath + "' a la API: " + e.getMessage());
                } catch (IOException e) {
                    VitalCore.Log.log(Level.SEVERE, "[ModelEngine] Error de I/O inesperado con stream de textura para '"
                            + relativePath + "': " + e.getMessage());
                }
            }
        }

        // 2. Añadir JSON de geometría usando addModelFile
        if (packData.getGeometryJsons() != null) {
            for (Map.Entry<String, JsonObject> entry : packData.getGeometryJsons().entrySet()) {
                String namespacedId = entry.getKey();
                String[] parts = namespacedId.split(":", 2);
                if (parts.length == 2) {
                    String relativePath = parts[1] + ".json";
                    JsonObject geoJson = entry.getValue();
                    try {
                        resourcePackApi.addModelFile(owningPlugin, relativePath, geoJson);
                        resourceAddedOrModified = true;
                    } catch (ResourcePackApiException e) {
                        VitalCore.Log.log(Level.SEVERE, "[ModelEngine] Error al añadir geometría '" + relativePath
                                + "' a la API: " + e.getMessage());
                    }
                } else {
                    VitalCore.Log.warning("[ModelEngine] Formato de ID de geometría inesperado: " + namespacedId);
                }
            }
        }

        if (packData.getItemDefinitionJsons() != null) {
            for (Map.Entry<String, JsonObject> entry : packData.getItemDefinitionJsons().entrySet()) {
                String namespacedId = entry.getKey();
                String[] parts = namespacedId.split(":", 2);
                if (parts.length == 2) {
                    String relativePath = parts[1] + ".json";
                    JsonObject itemJson = entry.getValue();
                    try {
                        resourcePackApi.addItemFile(owningPlugin, relativePath, itemJson);
                        resourceAddedOrModified = true;
                    } catch (ResourcePackApiException e) {
                        VitalCore.Log.log(Level.SEVERE, "[ModelEngine] Error al añadir definición item '" + relativePath
                                + "' a la API: " + e.getMessage());
                    }
                } else {
                    VitalCore.Log
                            .warning("[ModelEngine] Formato de ID de definición de item inesperado: " + namespacedId);
                }
            }
        }

        return resourceAddedOrModified;
    }

    /**
     * Intenta eliminar los recursos de un modelo de la API centralizada usando
     * removeResource.
     *
     * @param model El modelo cuyos recursos se eliminarán.
     * @return true si se intentó eliminar al menos un recurso (no garantiza éxito),
     *         false si la API no está disponible o el modelo es inválido.
     */
    private boolean removeModelResourcesFromApi(ProcessedBbModel model) {
        if (resourcePackApi == null || model == null || model.getPackData() == null) {
            return false;
        }

        boolean removalAttempted = false;
        ProcessedPackData packData = model.getPackData();
        String modelNameForPath = model.getName().toLowerCase().replaceAll("[^a-z0-9/._-]", "_");

        // 1. Eliminar texturas (necesita ResourceType.TEXTURE)
        if (packData.getTexturesUsed() != null) {
            for (BbModel.ModelTexture texture : packData.getTexturesUsed()) {
                if (texture == null || texture.getName() == null)
                    continue;
                String textureFileName = texture.getName().toLowerCase();
                if (!textureFileName.endsWith(".png"))
                    continue;
                String relativePath = modelNameForPath + "/" + textureFileName;
                try {
                    resourcePackApi.removeResource(owningPlugin, ResourceType.TEXTURE, relativePath);
                    removalAttempted = true;
                } catch (Exception e) {
                    VitalCore.Log.log(Level.WARNING, "[ModelEngine] Error al solicitar eliminación de textura '"
                            + relativePath + "' de la API: " + e.getMessage());
                }
            }
        }

        // 2. Eliminar JSON de geometría (necesita ResourceType.MODEL_JSON)
        if (packData.getGeometryJsons() != null) {
            for (String namespacedId : packData.getGeometryJsons().keySet()) {
                String[] parts = namespacedId.split(":", 2);
                if (parts.length == 2) {
                    String relativePath = parts[1] + ".json";
                    try {
                        resourcePackApi.removeResource(owningPlugin, ResourceType.MODEL_JSON, relativePath);
                        removalAttempted = true;
                    } catch (Exception e) {
                        VitalCore.Log.log(Level.WARNING, "[ModelEngine] Error al solicitar eliminación de geometría '"
                                + relativePath + "' de la API: " + e.getMessage());
                    }
                }
            }
        }

        // 3. Eliminar JSON de definición de item (necesita
        // ResourceType.ITEM_DEFINITION_JSON)
        if (packData.getItemDefinitionJsons() != null) {
            for (String namespacedId : packData.getItemDefinitionJsons().keySet()) {
                String[] parts = namespacedId.split(":", 2);
                if (parts.length == 2) {
                    String relativePath = parts[1] + ".json";
                    try {
                        // *** USAR EL ResourceType CORRECTO ***
                        resourcePackApi.removeResource(owningPlugin, ResourceType.ITEMS, relativePath);
                        removalAttempted = true;
                    } catch (Exception e) {
                        VitalCore.Log.log(Level.WARNING,
                                "[ModelEngine] Error al solicitar eliminación de definición item '" + relativePath
                                        + "' de la API: " + e.getMessage());
                    }
                }
            }
        }

        // Añadir lógica para eliminar otros tipos de recursos si es necesario

        return removalAttempted;
    }

    // --- Métodos existentes (sin cambios relevantes) ---
    public Set<String> getModelNames() {
        synchronized (loadedRuntimeModels) {
            return Collections.unmodifiableSet(loadedRuntimeModels.keySet());
        }
    }

    public ProcessedBbModel getModel(String name) {
        synchronized (loadedRuntimeModels) {
            return loadedRuntimeModels.get(name);
        }
    }

    public Map<String, ProcessedBbModel> getLoadedModels() {
        synchronized (loadedRuntimeModels) {
            return Collections.unmodifiableMap(new HashMap<>(loadedRuntimeModels));
        }
    }

    public void reload() {
        VitalCore.Log.info("[ModelEngine] Iniciando recarga de modelos y actualización de recursos en API...");
        loadAllModels();
        VitalCore.Log.info("[ModelEngine] Recarga completa.");
    }

    @Nullable
    public ModelInstance createInstance(String modelName, ModelContext context) {
        ProcessedBbModel model = getModel(modelName);
        if (model == null) {
            VitalCore.Log.warning("[ModelEngine] Modelo no encontrado: " + modelName);
            return null;
        }
        ModelHandler handler = getHandlerForType(context.getInstanceType());
        if (handler == null) {
            VitalCore.Log.severe("[ModelEngine] No handler for type: " + context.getInstanceType());
            return null;
        }
        ModelInstance instance = null;
        try {
            instance = handler.createInstance(model, context);
        } catch (Exception e) {
            VitalCore.Log.log(Level.SEVERE, "[ModelEngine] Error creating instance for " + modelName, e);
            return null;
        }
        if (instance != null) {
            registerInstance(instance);
        } else {
            VitalCore.Log.warning("[ModelEngine] Handler devolvió null al crear instancia para: " + modelName);
        }
        return instance;
    }

    private void registerInstance(ModelInstance instance) {
        if (instance != null)
            synchronized (activeInstances) {
                activeInstances.add(instance);
            }
    }

    public Optional<ModelInstance> getInstanceOptional(UUID instanceUuid) {
        if (instanceUuid == null)
            return Optional.empty();
        synchronized (activeInstances) {
            for (ModelInstance inst : activeInstances) {
                if (inst != null && instanceUuid.equals(inst.getInstanceUuid())) {
                    return Optional.of(inst);
                }
            }
        }
        return Optional.empty();
    }

    public Collection<ModelInstance> getActiveInstancesView() {
        synchronized (activeInstances) {
            return new ArrayList<>(activeInstances);
        }
    }

    public boolean isModelEntity(Entity entity) {
        return entity != null && entity.hasMetadata(METADATA_KEY);
    }

    public void destroy(ModelInstance inst) {
        if (inst == null)
            return;
        boolean removed;
        synchronized (activeInstances) {
            removed = this.activeInstances.remove(inst);
        }
        if (removed) {
            ModelHandler handler = inst.getHandler();
            String modelName = inst.getModel() != null ? inst.getModel().getName() : "unknown";
            if (handler != null) {
                try {
                    handler.destroy(inst);
                } catch (Exception e) {
                    VitalCore.Log.log(Level.SEVERE, "[ModelEngine] Error delegando destroy a handler para " + modelName,
                            e);
                }
            } else {
                VitalCore.Log.severe("[ModelEngine] Instancia removida (UUID: " + inst.getInstanceUuid()
                        + ") no tenía handler. Modelo: " + modelName);
                cleanupOrphanedVisuals(inst);
            }
            VitalCore.Log.info("[ModelEngine] Instancia destruida (UUID: " + inst.getInstanceUuid() + ")");
        }
    }

    public boolean destroy(Entity entity) {
        if (entity == null)
            return false;
        ModelInstance instanceToRemove = findInstanceContainingEntity(entity.getUniqueId());
        if (instanceToRemove != null) {
            destroy(instanceToRemove);
            return true;
        } else {
            if (entity.isValid() && entity.hasMetadata(METADATA_KEY)) {
                VitalCore.Log.warning("[ModelEngine] Entidad " + entity.getUniqueId()
                        + " tenía metadata pero no instancia activa. Limpiando.");
                entity.removeMetadata(METADATA_KEY, owningPlugin);
            }
            return false;
        }
    }

    @Nullable
    private ModelInstance findInstanceContainingEntity(UUID entityUuid) {
        if (entityUuid == null)
            return null;
        synchronized (activeInstances) {
            for (ModelInstance inst : activeInstances) {
                if (inst == null)
                    continue;
                if (inst.getBaseEntity().isPresent()
                        && inst.getBaseEntity().get().getUniqueId().equals(entityUuid)) {
                    return inst;
                }
                if (inst.getBoneEntities() != null) {
                    for (ItemDisplay display : inst.getBoneEntities().values()) {
                        if (display != null && display.getUniqueId().equals(entityUuid)) {
                            return inst;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void cleanupOrphanedVisuals(ModelInstance instance) {
        VitalCore.Log.warning(
                "[ModelEngine] Limpieza de emergencia para instancia sin handler: " + instance.getInstanceUuid());
        if (instance.getBoneEntities() != null)
            for (ItemDisplay display : instance.getBoneEntities().values()) {
                if (display != null && display.isValid()) {
                    display.remove();
                }
            }
        instance.getBaseEntity().ifPresent(base -> {
            if (base.isValid()) {
                base.remove();
            }
        });
    }

    private void startTickTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<ModelInstance> instancesCopy;
                synchronized (activeInstances) {
                    instancesCopy = new ArrayList<>(activeInstances);
                }
                for (ModelInstance inst : instancesCopy) {
                    if (inst == null)
                        continue;
                    ModelHandler handler = inst.getHandler();
                    String modelName = inst.getModel() != null ? inst.getModel().getName() : "unknown_model";
                    UUID instanceUUID = inst.getInstanceUuid();
                    if (handler == null) {
                        VitalCore.Log.warning("[ModelEngine Tick] Instancia (UUID: " + instanceUUID + ", Modelo: "
                                + modelName + ") encontrada sin handler!");
                        continue;
                    }
                    boolean shouldTick = true;
                    if (handler instanceof EntityModelHandler) {
                        Entity baseOpt = inst.getBaseEntity().get();
                        if (baseOpt.isEmpty() || !baseOpt.isValid()) {
                            VitalCore.Log.info(baseOpt.toString());
                            VitalCore.Log.warning("[ModelEngine Tick] Entidad base para instancia ENTITY (UUID: "
                                    + instanceUUID + ", Modelo: " + modelName + ") es inválida. Destruyendo.");
                            destroy(inst);
                            shouldTick = false;
                        }
                    }
                    if (shouldTick) {
                        try {
                            handler.tick(inst);
                        } catch (Exception ex) {
                            VitalCore.Log.log(Level.SEVERE,
                                    "[ModelEngine Tick] Error en tick (Handler: " + handler.getClass().getSimpleName()
                                            + ") para " + modelName + " (UUID: " + instanceUUID + ")",
                                    ex);
                        }
                    }
                }
            }
        }.runTaskTimer(owningPlugin, 1L, 1L);
    }

    public void shutdown() {
        VitalCore.Log.info("[ModelEngineManager] Deteniendo gestor de modelos...");
        synchronized (activeInstances) {
            VitalCore.Log.info("[ModelEngineManager] Destruyendo " + activeInstances.size() + " instancias activas...");
            Iterator<ModelInstance> iterator = activeInstances.iterator();
            while (iterator.hasNext()) {
                destroy(iterator.next());
            }
        }
        VitalCore.Log.info("[ModelEngineManager] Instancias activas destruidas.");
        if (resourcePackApi != null) {
            VitalCore.Log.info("[ModelEngineManager] Solicitando eliminación de recursos de la API...");
            boolean removalAttempted = false;
            synchronized (loadedRuntimeModels) {
                for (ProcessedBbModel model : loadedRuntimeModels.values()) {
                    if (removeModelResourcesFromApi(model)) {
                        removalAttempted = true;
                    }
                }
            }
            if (removalAttempted) {
                VitalCore.Log.info("[ModelEngineManager] Solicitud de eliminación de recursos enviada.");
            }
        }
        VitalCore.Log.info("[ModelEngineManager] Detención completada.");
    }
}