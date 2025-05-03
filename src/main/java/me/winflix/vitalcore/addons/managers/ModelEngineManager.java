package me.winflix.vitalcore.addons.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.handlers.EntityModelHandler;
import me.winflix.vitalcore.addons.interfaces.BbModel;
import me.winflix.vitalcore.addons.interfaces.ModelHandler;
import me.winflix.vitalcore.addons.interfaces.ModelInstance;
import me.winflix.vitalcore.addons.interfaces.ProcessedBbModel;
import me.winflix.vitalcore.addons.interfaces.ProcessedPackData;

/**
 * Gestiona la carga de .bbmodel, el spawn/destroy/tick de instancias,
 * y delega en el ModelHandler configurado en cada BbModel.
 */
public class ModelEngineManager {

    private final Map<String, ProcessedBbModel> loadedRuntimeModels = new HashMap<>();
    private final File modelDir;
    private final List<ModelInstance> activeInstances = Collections.synchronizedList(new ArrayList<>());
    private final String METADATA_KEY = "addons_custom_model";
    private final Map<String, ModelHandler> availableHandlers = new HashMap<>();
    private final List<ProcessedPackData> loadedPackData = new ArrayList<>();

    public ModelEngineManager(File dataFolder) {
        this.modelDir = new File(dataFolder, "addons/models");
        if (!modelDir.exists())
            modelDir.mkdirs();
        registerHandlers();
        loadAllModels();
        startTickTask();
    }

    private void registerHandlers() {
        availableHandlers.put("ENTITY", new EntityModelHandler());
        VitalCore.Log.info("[ModelEngine] Registrados " + availableHandlers.size() + " manejadores de modelos.");
    }

    private ModelHandler getHandlerForModel(ProcessedBbModel model) {
        return availableHandlers.getOrDefault("ENTITY", new EntityModelHandler());
    }

    public void loadAllModels() {
        synchronized (loadedRuntimeModels) {
            loadedRuntimeModels.clear();
        }
        synchronized (loadedPackData) {
            loadedPackData.clear();
        }

        File[] files = modelDir.listFiles((d, n) -> n.toLowerCase().endsWith(".bbmodel"));
        if (files == null || files.length == 0) {
            VitalCore.Log.warning("[ModelEngine] No se encontraron archivos .bbmodel en: " + modelDir.getPath());
            return;
        }

        VitalCore.Log.info("[ModelEngine] Cargando y procesando " + files.length + " modelos...");
        for (File f : files) {
            BbModel rawModel = BlockbenchLoader.load(f);
            if (rawModel != null) {
                ProcessedBbModel proBbModel = ModelProcessor.process(rawModel);

                if (proBbModel != null) {
                    synchronized (loadedRuntimeModels) {
                        loadedRuntimeModels.put(proBbModel.getName(), proBbModel);
                    }
                    synchronized (loadedPackData) {
                        loadedPackData.add(proBbModel.getPackData());
                    }

                    VitalCore.Log.info(proBbModel.toString());
                } else {
                    VitalCore.Log
                            .warning("[ModelEngine] Error al crear ProcessedBbModel para: " + rawModel.getName());
                }

            } else {
                VitalCore.Log.warning("[ModelEngine] Error al cargar el modelo crudo desde el archivo: " + f.getName());
            }
        }
        VitalCore.Log.info("[ModelEngine] Carga y procesamiento completado. " + loadedRuntimeModels.size()
                + " modelos listos para runtime.");
    }

    public List<ProcessedPackData> getLoadedPackData() {
        return loadedPackData;
    }

    public Collection<ProcessedPackData> getAllProcessedPackData() {
        synchronized (loadedPackData) {
            return new ArrayList<>(loadedPackData);
        }
    }

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
        VitalCore.Log.info("[ModelEngine] Iniciando recarga de modelos...");
        loadAllModels();
        VitalCore.Log.info("[ModelEngine] Recarga completa.");
    }

    public ModelInstance spawn(String modelName, Location loc, Player owner) {
        ProcessedBbModel model = getModel(modelName);
        if (model == null) {
            VitalCore.Log.warning("[ModelEngine] Modelo procesado no encontrado para spawn: " + modelName);
            return null;
        }

        ModelHandler handler = getHandlerForModel(model);
        if (handler == null) {
            VitalCore.Log.severe("[ModelEngine] ¡No se pudo determinar un handler para el modelo: " + modelName + "!");
            return null;
        }

        ModelInstance inst = null;
        try {
            inst = handler.spawn(model, loc, owner);
        } catch (Exception e) {
            VitalCore.Log.log(Level.SEVERE, "[ModelEngine] Error durante handler.spawn() para modelo " + modelName, e);
            return null;
        }

        if (inst != null) {
            synchronized (activeInstances) {
                activeInstances.add(inst);
            }
        } else {
            VitalCore.Log.warning("[ModelEngine] Handler devolvió null al spawnear: " + modelName);
        }
        return inst;
    }

    /**
     * Comprueba si una entidad tiene la metadata que indica que es
     * la base de un modelo gestionado por este engine.
     *
     * @param entity La entidad a comprobar.
     * @return true si tiene la metadata de modelo, false en caso contrario.
     */
    public boolean isModelEntity(Entity entity) {
        return entity != null && entity.hasMetadata(METADATA_KEY);
    }

    /**
     * Destruye una instancia específica, delegando al handler y removiéndola
     * de la lista interna de gestión.
     */
    public void destroy(ModelInstance inst) {
        if (inst == null)
            return;

        boolean removed = false;
        synchronized (activeInstances) {
            removed = this.activeInstances.remove(inst);
        }

        if (removed) {
            ModelHandler handler = inst.getHandler(); // <-- Obtener handler desde la instancia
            String modelName = inst.getModel() != null ? inst.getModel().getName() : "unknown";

            if (handler != null) {
                try {
                    handler.destroy(inst);
                } catch (Exception e) {
                    VitalCore.Log.log(Level.SEVERE, "[ModelEngine] Error al delegar destrucción al handler "
                            + handler.getClass().getSimpleName() + " para " + modelName, e);
                }
            } else {
                VitalCore.Log
                        .severe("[ModelEngine] ¡La instancia removida no tenía handler asignado! Modelo: " + modelName);
            }
        } else {
            String modelName = inst.getModel() != null ? inst.getModel().getName() : "unknown";
            VitalCore.Log.warning(
                    "[ModelEngine] Se intentó destruir una instancia que no estaba en la lista activa: " + modelName);
        }
    }

    /**
     * Intenta destruir una instancia de modelo completa basándose en una de sus
     * entidades visuales (ItemDisplay) que fue clickeada o apuntada.
     *
     * @param entity La entidad (presumiblemente un ItemDisplay con metadata) que se
     *               quiere usar
     *               para identificar y eliminar el modelo.
     * @return true si se encontró y eliminó un modelo asociado a la entidad,
     *         false en caso contrario.
     */
    public boolean destroy(Entity entity) {
        if (entity == null) {
            return false;
        }

        if (!isModelEntity(entity)) {
            return false;
        }

        // 2. Buscar la ModelInstance que contiene esta entidad específica
        ModelInstance instanceToRemove = null;
        UUID targetEntityUUID = entity.getUniqueId();

        synchronized (activeInstances) {
            for (ModelInstance inst : this.activeInstances) {
                if (inst == null || inst.getBoneEntities() == null)
                    continue;

                // Buscar si algún ItemDisplay DENTRO de esta instancia coincide
                for (ItemDisplay display : inst.getBoneEntities().values()) {
                    // Comprobar que display no sea null antes de llamar a getUniqueId
                    if (display != null && display.getUniqueId().equals(targetEntityUUID)) {
                        instanceToRemove = inst;
                        break; // Salir del bucle interno
                    }
                }
                if (instanceToRemove != null) {
                    break; // Salir del bucle externo
                }
            }
        }

        // 3. Si se encontró la instancia, destruirla (lo cual la removerá de la lista)
        if (instanceToRemove != null) {
            VitalCore.Log.info("[ModelEngineManager] Encontrada instancia '"
                    + instanceToRemove.getModel().getName() + "' para destruir via entidad " + targetEntityUUID);
            destroy(instanceToRemove); // Llama al otro método destroy que la remueve y limpia
            return true; // Éxito
        } else {
            // Tenía metadata pero no se encontró en ninguna instancia activa.
            VitalCore.Log.warning("[ModelEngineManager] La entidad " + targetEntityUUID
                    + " tenía metadata pero no se encontró en instancias activas.");
            // Remover metadata huérfana
            if (entity.isValid() && entity.hasMetadata(METADATA_KEY)) {
                entity.removeMetadata(METADATA_KEY, VitalCore.getPlugin());
                VitalCore.Log.info("[ModelEngineManager] Removida metadata huérfana de entidad " + targetEntityUUID);
            }
            return false;
        }
    }

    /** Scheduler que cada tick invoca tick() en todas las instancias activas */
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
                    String modelName = inst.getModel() != null ? inst.getModel().getName() : "unknown";

                    if (handler == null) {
                        VitalCore.Log.warning("[ModelEngine] Instancia encontrada sin handler durante el tick! Modelo: "
                                + modelName + ", UUID: " + inst.getInstanceUuid());
                        continue;
                    }

                    if (inst.getModel() == null) {
                        VitalCore.Log.warning("[ModelEngine] Instancia encontrada sin modelo durante el tick! Handler: "
                                + handler.getClass().getSimpleName() + ", UUID: " + inst.getInstanceUuid());
                        continue;
                    }

                    try {
                        handler.tick(inst);
                    } catch (Exception ex) {
                        VitalCore.Log.log(Level.SEVERE, "[ModelEngine] Error en tick (Handler: "
                                + handler.getClass().getSimpleName() + ") para " + modelName, ex);
                    }
                }
            }
        }.runTaskTimer(VitalCore.getPlugin(), 1L, 1L);
        VitalCore.Log.info("[ModelEngine] Tarea de Tick iniciada.");
    }

    public void shutdown() {
        VitalCore.Log.info("[ModelEngine] Deteniendo gestor de modelos...");
        synchronized (activeInstances) {
            VitalCore.Log.info("[ModelEngine] Destruyendo " + activeInstances.size() + " instancias activas...");
            for (ModelInstance inst : new ArrayList<>(activeInstances)) {
                destroy(inst);
            }
            activeInstances.clear();
        }
        VitalCore.Log.info("[ModelEngine] Todas las instancias destruidas.");
    }

}