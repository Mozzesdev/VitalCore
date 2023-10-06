package me.winflix.vitalcore.citizen.trait.traits;

import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.citizen.events.NPCLookCloseChangeTargetEvent;
import me.winflix.vitalcore.citizen.interfaces.Toggleable;
import me.winflix.vitalcore.citizen.interfaces.Trait;
import me.winflix.vitalcore.citizen.trait.traits.RotationTrait.PacketRotationSession;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@TraitName("lookclose")
public class LookClose extends Trait implements Toggleable {

    private double range = 10;
    private Player lookingAt;
    private boolean enabled = true;
    private final Map<UUID, PacketRotationSession> sessions = Maps.newHashMapWithExpectedSize(4);
    private boolean realisticLooking = false;

    public LookClose() {
        super("lookclose");
    }

    private boolean canSee(Player player) {
        if (player == null || !player.isValid())
            return false;
        return realisticLooking && npc.getEntity() instanceof LivingEntity
                ? ((LivingEntity) npc.getEntity()).hasLineOfSight(player)
                : true;
    }

    public boolean canSeeTarget() {
        return canSee(lookingAt);
    }

    public void findNewTarget() {
        if (sessions.size() > 0) {
            for (PacketRotationSession session : sessions.values()) {
                session.end();
            }
            sessions.clear();
        }

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
            double distance = player.getLocation().distanceSquared(npcLoc);

            if (distance < min) {
                min = distance;
                lookingAt = player;
            }
        }

        if (old != lookingAt) {
            NPCLookCloseChangeTargetEvent event = new NPCLookCloseChangeTargetEvent(npc, old, lookingAt);
            Bukkit.getPluginManager().callEvent(event);
            if (lookingAt != event.getNewTarget() && event.getNewTarget() != null && !isValid(event.getNewTarget())) {
                return;
            }
            lookingAt = event.getNewTarget();
        }
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
