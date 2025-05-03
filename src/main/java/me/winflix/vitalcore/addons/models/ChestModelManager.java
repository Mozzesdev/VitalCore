package me.winflix.vitalcore.addons.models;

// import java.util.List;
// import java.util.concurrent.atomic.AtomicReference;

// import org.bukkit.Bukkit;
// import org.bukkit.Location;
// import org.bukkit.entity.Item;
// import org.bukkit.entity.Player;
// import org.bukkit.inventory.ItemStack;
// import org.bukkit.util.Vector;

// import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
// import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
// import com.ticxo.modelengine.api.model.ActiveModel;
// import com.ticxo.modelengine.api.model.ModeledEntity;

// import me.winflix.vitalcore.VitalCore;
// import me.winflix.vitalcore.addons.files.ChestConfig;
// import me.winflix.vitalcore.addons.files.ChestConfig.ChestSettings;
// import me.winflix.vitalcore.addons.managers.GenericModelManager;
// import me.winflix.vitalcore.general.utils.Utils;

// public class ChestModelManager extends GenericModelManager {
//     private final VitalCore plugin;
//     private final ChestSettings settings;
//     private final List<ItemStack> rewards;

//     public ChestModelManager(VitalCore plugin,
//             ChestConfig config,
//             String chestId,
//             List<ItemStack> rewards) {
//         super(plugin, config.getSettings(chestId).getModelId());
//         this.plugin = plugin;
//         this.settings = config.getSettings(chestId);
//         this.rewards = rewards;
//     }

//     @Override
//     protected void onSpawn(ModeledEntity me, ActiveModel am) {
//         String idleAnim = settings.getAnimations().getOrDefault("idle", "");
//         am.getAnimationHandler().playAnimation(idleAnim, 0, 0, 1, true);
//     }

//     @Override
//     protected void onInteract(ModeledEntity me, Player player) {
//         // 1) Parar animaciones y reproducir “open”
//         AnimationHandler handler = me.getModel(settings.getModelId())
//                 .orElseThrow()
//                 .getAnimationHandler();
//         handler.forceStopAllAnimations();
//         String openAnim = settings.getAnimations().getOrDefault("open", "");
//         IAnimationProperty prop = handler.playAnimation(openAnim, 0.1f, 0.1f, 1, false);
//         long duration = (long) ((prop.getBlueprintAnimation().getLength() * 20L) - 10L);

//         // 2) Contenedor para el Item
//         final AtomicReference<Item> visualRef = new AtomicReference<>();

//         // 3) Tras acabar “open” → spawn + schedule remove
//         Bukkit.getScheduler().runTaskLater(plugin, () -> {
//             // spawn
//             Location baseLoc = me.getBase().getLocation().clone();
//             Item spawned = Utils.spawnFloatingItem(baseLoc, rewards.get(0));
//             visualRef.set(spawned);

//             spawned.setVelocity(new Vector(0, 0.05, 0));

//             // cancelamos el ascenso exactamente a los 20 ticks
//             Bukkit.getScheduler().runTaskLater(plugin, () -> {
//                 // dejarlo “quieto” otra vez
//                 if (!spawned.isDead()) {
//                     spawned.setVelocity(new Vector(0, 0, 0));
//                 }
//             }, 20L);

//             // schedule remove y entregar recompensa 2s después
//             Bukkit.getScheduler().runTaskLater(plugin, () -> {
//                 giveRewards(player);
//                 Item toRemove = visualRef.get();
//                 if (toRemove != null && !toRemove.isDead()) {
//                     Utils.removeFloatingItem(toRemove);
//                 }
//                 handler.stopAnimation(openAnim);
//             }, 60L);

//         }, duration);
//     }

//     private void giveRewards(Player p) {
//         p.getInventory().addItem(rewards.get(0));
//     }

// }