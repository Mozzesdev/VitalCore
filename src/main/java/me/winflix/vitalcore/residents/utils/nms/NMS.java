package me.winflix.vitalcore.residents.utils.nms;

import java.lang.invoke.MethodHandle;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import me.winflix.vitalcore.residents.interfaces.NPC;
import me.winflix.vitalcore.residents.interfaces.NPCHolder;
import me.winflix.vitalcore.residents.utils.Utils;
import me.winflix.vitalcore.residents.utils.network.EmptyChannel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
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

    public static SoundEvent getSoundEffect(NPC npc, SoundEvent snd, NPC.Metadata meta) {
        if (npc == null || !npc.getMetadata().has(meta)) {
            return snd;
        }

        String metaValue = npc.getMetadata().get(meta, snd == null ? "" : snd.toString());
        ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace(metaValue);

        Optional<Holder.Reference<SoundEvent>> optionalHolder = BuiltInRegistries.SOUND_EVENT.get(resourceLocation);

        return optionalHolder.map(holder -> holder.value())
                .orElse(snd);
    }

    public static void setStepHeight(Entity entity, float height) {
        getHandle(entity);
    };

    public static float getStepHeight(Entity entity) {
        return getHandle(entity).maxUpStep();
    }

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

    public static void sendPacket(Player player, Packet<?> packet) {
        if (packet == null)
            return;
        ((ServerPlayer) getHandle(player)).connection.send(packet);
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

    public static boolean sendTabListAdd(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);
        ServerPlayer from = ((CraftPlayer) listPlayer).getHandle();
        ClientboundPlayerInfoUpdatePacket packet = ClientboundPlayerInfoUpdatePacket
                .createPlayerInitializing(Arrays.asList(from));
        boolean list = false;
        RemoteChatSession chatSession = from.getChatSession();
        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(
                from.getUUID(),
                from.getGameProfile(),
                list,
                from.connection.latency(),
                from.gameMode.getGameModeForPlayer(),
                list ? from.getTabListDisplayName() : Component.empty(),
                true,
                0,
                chatSession != null ? chatSession.asData() : null);

        try {
            PLAYER_INFO_ENTRIES_LIST.invoke(packet, Lists.newArrayList(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        sendPacket(recipient, packet);
        return true;
    }

    public static void sendTabListRemove(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);
        sendPacket(recipient, new ClientboundPlayerInfoRemovePacket(Arrays.asList(getHandle(listPlayer).getUUID())));
    }

    public static void replaceTrackerEntry(org.bukkit.entity.Entity entity) {
        ServerLevel server = (ServerLevel) getHandle(entity).level();
        TrackedEntity entry = server.getChunkSource().chunkMap.entityMap.get(entity.getEntityId());
        if (entry == null)
            return;
        entry.broadcastRemoved();
        EntityTracker replace = new EntityTracker(server.getChunkSource().chunkMap, entry);
        server.getChunkSource().chunkMap.entityMap.put(entity.getEntityId(), replace);
    }

    public static void rotateHead(Player receiver, NPC npc, boolean body) {
        ServerPlayer serverPlayer = ((CraftPlayer) npc.getEntity()).getHandle();

        byte yaw = (byte) (npc.getLocation().getYaw() * 256.0F / 360.0F);
        byte pitch = (byte) (npc.getLocation().getPitch() * 256.0F / 360.0F);

        ClientboundMoveEntityPacket.Rot bodyPacket = new ClientboundMoveEntityPacket.Rot(serverPlayer.getId(), yaw,
                pitch, true);
        ClientboundRotateHeadPacket headPacket = new ClientboundRotateHeadPacket(serverPlayer, yaw);

        if (body)
            sendPacket(receiver, bodyPacket);

        sendPacket(receiver, headPacket);
    }

    public static void setNoGravity(Entity entity, boolean nogravity) {
        net.minecraft.world.entity.Entity handle = getHandle(entity);
        handle.setNoGravity(nogravity);
        if (!(handle instanceof Mob) || !(entity instanceof NPCHolder))
            return;
        Mob mob = (Mob) handle;
        NPC npc = ((NPCHolder) entity).getNPC();
        if (!(mob.getMoveControl() instanceof FlyingMoveControl) || npc.getMetadata().has("flying-nogravity-float"))
            return;
        try {
            if (nogravity) {
                boolean old = (boolean) FLYING_MOVECONTROL_FLOAT_GETTER.invoke(mob.getMoveControl());
                FLYING_MOVECONTROL_FLOAT_SETTER.invoke(mob.getMoveControl(), true);
                npc.getMetadata().set("flying-nogravity-float", old);
            } else if (npc.getMetadata().has("flying-nogravity-float")) {
                FLYING_MOVECONTROL_FLOAT_SETTER.invoke(mob.getMoveControl(),
                        npc.getMetadata().get("flying-nogravity-float"));
                npc.getMetadata().remove("flying-nogravity-float");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static final MethodHandle PLAYER_INFO_ENTRIES_LIST = Reflection
            .getFirstFinalSetter(ClientboundPlayerInfoUpdatePacket.class, List.class);
    private static final MethodHandle FLYING_MOVECONTROL_FLOAT_GETTER = Reflection.getFirstGetter(
            FlyingMoveControl.class,
            boolean.class);
    private static final MethodHandle FLYING_MOVECONTROL_FLOAT_SETTER = Reflection.getFirstSetter(
            FlyingMoveControl.class,
            boolean.class);
}
