package me.winflix.vitalcore.addons.managers;

// import java.util.List;
// import java.util.Map;

// import org.bukkit.Bukkit;
// import org.bukkit.Location;
// import org.bukkit.Particle;
// import org.bukkit.SoundCategory;
// import org.bukkit.entity.Entity;

// // import com.ticxo.modelengine.api.model.ActiveModel;

// import me.winflix.vitalcore.VitalCore;
// import me.winflix.vitalcore.addons.interfaces.EffectKeyframe;

// public class ModelEffectHandler {
//     // private final VitalCore plugin;
//     // private final Map<String, List<EffectKeyframe>> effectsByAnim;

//     // public ModelEffectHandler(VitalCore plugin, Map<String, List<EffectKeyframe>> effectsByAnim) {
//     //     this.plugin = plugin;
//     //     this.effectsByAnim = effectsByAnim;
//     // }

//     // public void handleAnimationPlay(String animationName, ActiveModel model) {
//     //     List<EffectKeyframe> list = effectsByAnim.get(animationName);
//     //     if (list == null || list.isEmpty())
//     //         return;

//     //     Entity entity = (Entity) model.getModeledEntity().getBase();
//     //     if (entity == null)
//     //         return;

//     //     Location loc = entity.getLocation();

//     //     for (EffectKeyframe ef : list) {
//     //         long delay = (long) (ef.getTime() * 20);
//     //         Bukkit.getScheduler().runTaskLater(plugin, () -> playEffect(loc, ef), delay);
//     //     }
//     // }

//     // private void playEffect(Location loc, EffectKeyframe ef) {
//     //     if ("sound".equals(ef.getChannel())) {
//     //         String key = ef.getEffect().replaceFirst("\\.[^.]+$", "").toLowerCase();
//     //         loc.getWorld().playSound(loc, key, SoundCategory.MASTER, 1.0f, 1.0f);
//     //     } else if ("particle".equals(ef.getChannel())) {
//     //         try {
//     //             Particle p = Particle.valueOf(ef.getEffect().toUpperCase());
//     //             loc.getWorld().spawnParticle(p, loc, 1);
//     //         } catch (IllegalArgumentException ex) {
//     //             Bukkit.getLogger().warning("Invalid particle: " + ef.getEffect());
//     //         }
//     //     }
//     // }
// }