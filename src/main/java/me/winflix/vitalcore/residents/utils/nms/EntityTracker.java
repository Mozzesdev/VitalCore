package me.winflix.vitalcore.residents.utils.nms;

import java.lang.invoke.MethodHandle;
import java.util.Set;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.ForwardingSet;

import me.winflix.vitalcore.residents.Residents;
import me.winflix.vitalcore.residents.entities.PlayerEntityNPC;
import me.winflix.vitalcore.residents.interfaces.NPC;
import me.winflix.vitalcore.residents.interfaces.NPCHolder;
import me.winflix.vitalcore.residents.utils.Utils;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public class EntityTracker extends ChunkMap.TrackedEntity {

    private final Entity tracker;

    public EntityTracker(ChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        this.tracker = entity;
        try {
            Set<ServerPlayerConnection> set = (Set<ServerPlayerConnection>) TRACKING_SET_GETTER.invoke(this);
            TRACKING_SET_SETTER.invoke(this, new ForwardingSet<ServerPlayerConnection>() {
                @Override
                public boolean add(ServerPlayerConnection conn) {
                    boolean res = super.add(conn);
                    if (res) {
                        updateLastPlayer(conn.getPlayer());
                    }
                    return res;
                }

                @Override
                protected Set<ServerPlayerConnection> delegate() {
                    return set;
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public EntityTracker(ChunkMap map, TrackedEntity entry) {
        this(map, getTracker(entry), getTrackingDistance(entry), getE(entry), getF(entry));
    }

    public void updateLastPlayer(ServerPlayer lastUpdatedPlayer) {
        if (tracker.isRemoved() || tracker.getBukkitEntity().getType() != EntityType.PLAYER)
            return;
        final ServerPlayer entityplayer = lastUpdatedPlayer;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Residents.getPlugin(), () -> {
            if (tracker.isRemoved() || entityplayer.isRemoved())
                return;
            NMS.sendPacket(entityplayer.getBukkitEntity(),
                    new ClientboundRotateHeadPacket(tracker, (byte) (tracker.getYHeadRot() * 256.0F / 360.0F)));
        }, 20L);
        boolean sendTabRemove = NMS.sendTabListAdd(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        if (!sendTabRemove) {
            Bukkit.getScheduler()
                    .scheduleSyncDelayedTask(Residents.getPlugin(), () -> NMS
                            .sendPacket(entityplayer.getBukkitEntity(), new ClientboundAnimatePacket(tracker, 0)),
                            1);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Residents.getPlugin(), () -> {
            if (tracker.isRemoved() || entityplayer.isRemoved())
                return;
            NMS.sendTabListRemove(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
            NMS.sendPacket(entityplayer.getBukkitEntity(), new ClientboundAnimatePacket(tracker, 0));
        }, 20L);
    }

    @Override
    public void updatePlayer(final ServerPlayer entityplayer) {
        if (entityplayer instanceof PlayerEntityNPC)
            return;

        if (!tracker.isRemoved() && !seenBy.contains(entityplayer.connection)) {
            NPC npc = ((NPCHolder) tracker).getNPC();
            if (REQUIRES_SYNC == null) {
                REQUIRES_SYNC = !Bukkit.isPrimaryThread();
            }
            boolean cancelled = Utils.callPossiblySync(() -> {
                Integer trackingRange = npc.getMetadata().<Integer>get(NPC.Metadata.TRACKING_RANGE);
                if (TRACKING_RANGE_SETTER != null && trackingRange != null
                        && npc.getMetadata().get("last-tracking-range", -1) != trackingRange.intValue()) {
                    try {
                        TRACKING_RANGE_SETTER.invoke(this, trackingRange);
                        npc.getMetadata().set("last-tracking-range", trackingRange);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }, REQUIRES_SYNC);

            if (cancelled)
                return;
        }

        super.updatePlayer(entityplayer);
    }

    private static int getE(TrackedEntity entry) {
        try {
            return (int) E.invoke(TRACKER_ENTRY.invoke(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean getF(TrackedEntity entry) {
        try {
            return (boolean) F.invoke(TRACKER_ENTRY.invoke(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Entity getTracker(TrackedEntity entry) {
        try {
            return (Entity) TRACKER.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getTrackingDistance(TrackedEntity entry) {
        try {
            return (Integer) TRACKING_RANGE.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static final MethodHandle E = Reflection.getGetter(ServerEntity.class, "e");
    private static final MethodHandle F = Reflection.getGetter(ServerEntity.class, "f");
    private static volatile Boolean REQUIRES_SYNC;
    private static final MethodHandle TRACKER = Reflection.getFirstGetter(TrackedEntity.class, Entity.class);
    private static final MethodHandle TRACKER_ENTRY = Reflection.getFirstGetter(TrackedEntity.class, ServerEntity.class);
    private static final MethodHandle TRACKING_RANGE = Reflection.getFirstGetter(TrackedEntity.class, int.class);
    private static final MethodHandle TRACKING_RANGE_SETTER = Reflection.getFirstFinalSetter(TrackedEntity.class, int.class);
    private static final MethodHandle TRACKING_SET_GETTER = Reflection.getFirstGetter(TrackedEntity.class, Set.class);
    private static final MethodHandle TRACKING_SET_SETTER = Reflection.getFirstFinalSetter(TrackedEntity.class, Set.class);

}
