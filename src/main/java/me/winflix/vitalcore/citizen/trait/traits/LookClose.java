package me.winflix.vitalcore.citizen.utils.trait.traits;

import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.citizen.events.NPCLookCloseChangeTargetEvent;
import me.winflix.vitalcore.citizen.interfaces.Toggleable;
import me.winflix.vitalcore.citizen.interfaces.Trait;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@TraitName("lookclose")
public class LookClose extends Trait implements Toggleable {

    private double range = 10.0;
    private Player lookingAt;
    private boolean enabled = false;

    public LookClose() {
        super("lookclose");
    }

    private boolean canSee(Player player) {
        if (player == null || !player.isValid())
            return false;
        return !(npc.getEntity() instanceof LivingEntity) ||
                ((LivingEntity) npc.getEntity()).hasLineOfSight(player);
    }

    public boolean canSeeTarget() {
        return canSee(lookingAt);
    }

    public void findNewTarget() {
        if (lookingAt != null && !isValid(lookingAt)) {
            NPCLookCloseChangeTargetEvent event = new NPCLookCloseChangeTargetEvent(npc, lookingAt, null);
            Bukkit.getPluginManager().callEvent(event);
            if (event.getNewTarget() != null && isValid(event.getNewTarget())) {
                lookingAt = event.getNewTarget();
            } else {
                lookingAt = null;
            }
        }

        Player old = lookingAt;

        double min = Double.MAX_VALUE;
        Location npcLoc = npc.getLocation();
        for (Player player : getNearbyPlayers()) {
            double dist = player.getLocation().distance(npcLoc);
            if (dist > min)
                continue;
            min = dist;
            lookingAt = player;
        }
        NPCLookCloseChangeTargetEvent event = new NPCLookCloseChangeTargetEvent(npc, old, lookingAt);
        Bukkit.getPluginManager().callEvent(event);
        if (lookingAt != event.getNewTarget() && event.getNewTarget() != null && !isValid(event.getNewTarget())) {
            return;
        }
        lookingAt = event.getNewTarget();
    }

    private List<Player> getNearbyPlayers() {
        List<Player> options = new ArrayList<>();
        Location npcLoc = npc.getLocation();
        Iterable<Player> nearby = Citizen.getLocationLookup().getNearbyPlayers(npcLoc, range);
        for (Player player : nearby) {
            if (player == lookingAt)
                continue;
            if (player.getLocation().getWorld() != npcLoc.getWorld() || isInvisible(player))
                continue;

            options.add(player);
        }
        return options;
    }

    private boolean isInvisible(Player player) {
        return player.getGameMode() == GameMode.SPECTATOR || player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                || isPluginVanished(player) || !canSee(player);
    }

    private boolean isPluginVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    private boolean isValid(Player entity) {
        return entity.isOnline() && entity.isValid() && entity.getWorld() == npc.getEntity().getWorld()
                && entity.getLocation().distance(npc.getLocation()) <= range && !isInvisible(entity);
    }

    public double getRange() {
        return range;
    }

    public Player getTarget() {
        return lookingAt;
    }

    public void setRange(double range) {
        this.range = range;
    }

    @Override
    public void onDespawn() {
        NPCLookCloseChangeTargetEvent event = new NPCLookCloseChangeTargetEvent(npc, lookingAt, null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getNewTarget() != null && isValid(event.getNewTarget())) {
            lookingAt = event.getNewTarget();
        } else {
            lookingAt = null;
        }
    }

    @Override
    public void run() {
        if (!npc.isSpawned()) {
            lookingAt = null;
            return;
        }

        if (!enabled) {
            lookingAt = null;
            return;
        }

        findNewTarget();

        if (lookingAt == null)
            return;

        RotationTrait rot = npc.getOrAddTrait(RotationTrait.class);
        rot.getPhysicalSession().rotateToFace(lookingAt);

    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    @Override
    public String toString() {
        return "LookClose{" + enabled + "}";
    }

}
