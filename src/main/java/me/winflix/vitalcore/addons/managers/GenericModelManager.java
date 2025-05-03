package me.winflix.vitalcore.addons.managers;

// import java.util.Map;
// import java.util.UUID;
// import java.util.concurrent.ConcurrentHashMap;

// import org.bukkit.Location;
// import org.bukkit.entity.Entity;
// import org.bukkit.entity.EntityType;
// import org.bukkit.entity.Player;
// import org.bukkit.event.EventHandler;
// import org.bukkit.event.Listener;
// import org.bukkit.event.player.PlayerInteractAtEntityEvent;

// import com.ticxo.modelengine.api.ModelEngineAPI;
// import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
// import com.ticxo.modelengine.api.events.AnimationPlayEvent;
// import com.ticxo.modelengine.api.model.ActiveModel;
// import com.ticxo.modelengine.api.model.ModeledEntity;

// import me.winflix.vitalcore.VitalCore;

// /**
//  * Manejador genérico de entidades con modelos de ModelEngine.
//  * Refactorizado para usar UUID como clave y acceso O(1) a ModeledEntity por
//  * base entity.
//  */
// public abstract class GenericModelManager implements Listener {
//     protected final VitalCore plugin;
//     protected final String modelId;

//     protected final Map<UUID, ModeledEntity> entities = new ConcurrentHashMap<>();
//     protected final Map<Location, UUID> locationIndex = new ConcurrentHashMap<>();
//     protected final Map<Entity, ModeledEntity> byBaseEntity = new ConcurrentHashMap<>();

//     public GenericModelManager(VitalCore plugin, String modelId) {
//         this.plugin = plugin;
//         this.modelId = modelId;
//         plugin.getServer().getPluginManager().registerEvents(this, plugin);
//     }

//     /**
//      * Crea y configura la entidad base (interactive) en la ubicación.
//      * Puedes sobreescribir para otro tipo de entidad.
//      */
//     protected Entity createBaseEntity(Location location) {
//         Location centered = location.clone();
//         Entity interact = centered.getWorld().spawnEntity(centered, EntityType.INTERACTION);
//         interact.setInvulnerable(true);
//         interact.setSilent(true);
//         interact.setCustomNameVisible(false);
//         interact.setGravity(false);
//         interact.setPersistent(false);
//         interact.setFireTicks(0);

//         return interact;
//     }

//     /**
//      * Spawnea la entidad modelada en la ubicación.
//      */
//     public void spawnEntity(Location location) {
//         Entity base = createBaseEntity(location);
//         ModeledEntity me = ModelEngineAPI.createModeledEntity(base);
//         ActiveModel am = ModelEngineAPI.createActiveModel(modelId);
//         me.addModel(am, true);

//         UUID id = base.getUniqueId();
//         entities.put(id, me);
//         byBaseEntity.put(base, me);

//         onSpawn(me, am);
//     }

//     /**
//      * Destruye y elimina la entidad modelada.
//      */
//     public void removeEntity(Entity entity) {
//         if (entity != null) {
//             ModeledEntity me = byBaseEntity.get(entity);
//             if (me != null) {
//                 me.removeModel(modelId);
//             }
//         }
//     }

//     @EventHandler
//     public void onEntityInteract(PlayerInteractAtEntityEvent event) {
//         ModeledEntity me = byBaseEntity.get(event.getRightClicked());
//         if (me != null) {
//             event.setCancelled(true);
//             onInteract(me, event.getPlayer());
//         }
//     }

//     @EventHandler
//     public void onAnimationPlay(AnimationPlayEvent event) {
//         if ("".equalsIgnoreCase(modelId)) return;
//         // effectHandler.handleAnimationPlay(event.getProperty().getName(), event.getModel());
//     }
//     /**
//      * Llamado tras spawnEntity: ideal para iniciar animaciones (idle, etc.).
//      */
//     protected abstract void onSpawn(ModeledEntity me, ActiveModel am);

//     /**
//      * Llamado al interactuar con la entidad modelada.
//      */
//     protected abstract void onInteract(ModeledEntity me, Player player);

//     protected void scheduleIdle(AnimationHandler handler, long delayTicks) {
//         plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
//             handler.forceStopAllAnimations();
//             handler.playAnimation("idle", 0, 0, 0.5, true);
//         }, delayTicks);
//     }
// }