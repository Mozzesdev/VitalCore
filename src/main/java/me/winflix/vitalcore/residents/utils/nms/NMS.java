package me.winflix.vitalcore.residents.utils.nms;

import java.lang.invoke.MethodHandle;
import java.net.SocketAddress;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.google.common.base.Preconditions;

import me.winflix.vitalcore.residents.utils.Utils;
import me.winflix.vitalcore.residents.utils.network.EmptyChannel;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;

public class NMS {

    public static boolean isEntityValid(Entity entity) {
        net.minecraft.world.entity.Entity handle = getHandle(entity);
        return handle.valid && handle.isAlive();
    }

    public static net.minecraft.world.entity.Entity getHandle(Entity entity) {
        if (!(entity instanceof CraftEntity))
            return null;
        return ((CraftEntity) entity).getHandle();
    }

    public static void setStepHeight(Entity entity, float height) {
        getHandle(entity).setMaxUpStep(height);
    };

    public static void initNetworkManager(Connection network) {
        network.channel = new EmptyChannel(null);
        SocketAddress socketAddress = new SocketAddress() {
            private static final long serialVersionUID = 8207338859896320185L;
        };
        network.address = socketAddress;
    }

    @SuppressWarnings("resource")
    public static boolean addEntityToWorld(Entity entity, SpawnReason custom) {
        int viewDistance = -1;
        ChunkMap chunkMap = null;

        try {
            if (entity instanceof Player) {
                chunkMap = ((ServerChunkCache) getHandle(entity).level().getChunkSource()).chunkMap;
                viewDistance = (int) Reflection.getFirstGetter(ChunkMap.class, int.class).invoke(chunkMap);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        boolean success = getHandle(entity).level().addFreshEntity(getHandle(entity), custom);
        try {
            if (chunkMap != null) {
                Reflection.getFirstSetter(ChunkMap.class, int.class).invoke(chunkMap, viewDistance);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return success;
    };

    @SuppressWarnings("all")
    public static void addOrRemoveFromPlayerList(org.bukkit.entity.Entity entity, boolean remove) {
        if (entity == null)
            return;
        ServerPlayer handle = (ServerPlayer) getHandle(entity);
        if (handle.level() == null)
            return;
        if (remove) {
            handle.level().players().remove(handle);
        } else if (!handle.level().players().contains(handle)) {
            ((List) handle.level().players()).add(handle);
        }
        try {
            MethodHandle chunkUpdate = Reflection.getMethodHandle(ChunkMap.class, "a", ServerPlayer.class,
                    boolean.class);
            chunkUpdate.invoke(((ServerLevel) handle.level()).getChunkSource().chunkMap, handle, !remove);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void removeFromWorld(Entity entity) {
        Preconditions.checkNotNull(entity);
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        ((ServerLevel) nmsEntity.level()).getChunkSource().removeEntity(nmsEntity);
    }

    public static void remove(Entity entity) {
        getHandle(entity).remove(RemovalReason.KILLED);
    }

    public static void setBukkitEntity(net.minecraft.world.entity.Entity entity, CraftEntity bukkitEntity) {
        try {
            Reflection.getSetter(net.minecraft.world.entity.Entity.class, "bukkitEntity").invoke(entity, bukkitEntity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setExactLocation(org.bukkit.entity.Entity entity, Location location) {
        getHandle(entity).setPos(location.getX(), location.getY(), location.getZ());
        getHandle(entity).moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                location.getPitch());
    }

    public static void setBodyYaw(org.bukkit.entity.Entity entity, float yaw) {
        getHandle(entity).setYRot(yaw);
    }

    public static void setHeadYaw(org.bukkit.entity.Entity entity, float yaw) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity))
            return;
        LivingEntity handle = (LivingEntity) getHandle(entity);
        yaw = Utils.clamp(yaw);
        handle.yBodyRotO = yaw;
        if (!(handle instanceof net.minecraft.world.entity.player.Player)) {
            handle.setYBodyRot(yaw);
        }
        handle.setYHeadRot(yaw);
    }

}
