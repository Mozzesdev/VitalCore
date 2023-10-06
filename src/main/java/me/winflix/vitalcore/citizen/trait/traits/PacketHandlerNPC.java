package me.winflix.vitalcore.citizen.utils.trait.traits;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.citizen.enums.DespawnReason;
import me.winflix.vitalcore.citizen.enums.RemoveReason;
import me.winflix.vitalcore.citizen.enums.SpawnReason;
import me.winflix.vitalcore.citizen.interfaces.EntityController;
import me.winflix.vitalcore.citizen.interfaces.Trait;
import me.winflix.vitalcore.citizen.interfaces.EntityPacketTracker;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.citizen.tasks.PlayerUpdateTask;
import me.winflix.vitalcore.citizen.utils.LocationLookup;
import me.winflix.vitalcore.core.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@TraitName("packet")
public class PacketHandlerNPC extends Trait {
    private EntityPacketTracker packetTracker;
    private boolean spawned = false;

    public PacketHandlerNPC() {
        super("packet");
    }

    public EntityPacketTracker getPacketTracker() {
        return packetTracker;
    }

    @Override
    public void onRemove(RemoveReason reason) {
        if (reason == RemoveReason.REMOVAL) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            Bukkit.getScheduler().scheduleSyncDelayedTask(VitalCore.getPlugin(), () -> {
                if (npc.getLocation() != null) {
                    npc.spawn(npc.getLocation(), SpawnReason.RESPAWN);
                }
            });
        }
    }

    @Override
    public void onSpawn() {
        packetTracker = NMS.createPacketTracker(npc.getEntity());
        spawned = true;
    }

    @Override
    public void run() {
        if (!spawned)
            return;
        LocationLookup.PerPlayerMetadata<Boolean>
                ppm = Citizen.getLocationLookup().registerMetadata("packetnpc", null);
        for (Player nearby : Citizen.getLocationLookup().getNearbyPlayers(npc)) {
            if (!ppm.has(nearby.getUniqueId(), npc.getProfile().getId().toString())) {
                packetTracker.link(nearby);
                ppm.set(nearby.getUniqueId(), npc.getProfile().getId().toString(), true);
            }
        }
        packetTracker.run();
    }

    public EntityController wrap(EntityController controller) {
        if (!(controller instanceof PacketController)) {
            return new PacketController(controller);
        }
        return controller;
    }

    private class PacketController implements EntityController {
        private final EntityController base;

        public PacketController(EntityController controller) {
            this.base = controller;
        }

        @Override
        public void create(Location at, NPC npc) {
            base.create(at, npc);
        }

        @Override
        public void die() {
            base.die();
            if (!spawned)
                return;
            PlayerUpdateTask.deregisterPlayer(getBukkitEntity());
            LocationLookup.PerPlayerMetadata<Boolean>
                    ppm = Citizen.getLocationLookup().registerMetadata("packetnpc", null);
            packetTracker.unlinkAll(player -> ppm.remove(player.getUniqueId(), npc.getProfile().getId().toString()));
            spawned = false;
        }

        @Override
        public Entity getBukkitEntity() {
            return base.getBukkitEntity();
        }

        @Override
        public void remove() {
            if (!spawned)
                return;
            PlayerUpdateTask.deregisterPlayer(getBukkitEntity());
            LocationLookup.PerPlayerMetadata<Boolean>
                    ppm = Citizen.getLocationLookup().registerMetadata("packetnpc", null);
            packetTracker.unlinkAll(player -> ppm.remove(player.getUniqueId(),  npc.getProfile().getId().toString()));
            base.remove();
            spawned = false;
        }

        @Override
        public boolean spawn(Location at) {
            NMS.setLocationDirectly(base.getBukkitEntity(), at);
            PlayerUpdateTask.registerPlayer(getBukkitEntity());
            return true;
        }
    }
}