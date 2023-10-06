package me.winflix.vitalcore.core.nms;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.citizen.events.NPCKnockbackEvent;
import me.winflix.vitalcore.citizen.interfaces.EntityPacketTracker;
import me.winflix.vitalcore.citizen.interfaces.NPCHolder;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.citizen.utils.EntityTracker;
import me.winflix.vitalcore.citizen.utils.Util;
import me.winflix.vitalcore.citizen.trait.EmptyChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.*;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scoreboard.Team;

import java.io.Serial;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.SocketAddress;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NMS {

    public static float getYaw(org.bukkit.entity.Entity entity) {
        return getHandle(entity).getYRot();
    }

    public static float getHeadYaw(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity)) {
            return entity.getLocation().getYaw();
        }
        return getHandle((org.bukkit.entity.LivingEntity) entity).getYHeadRot();
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

    public static void setTeamNameTagVisible(Team team, boolean visible) {
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, visible ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);
    }

    public static boolean sendTabListAdd(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);
        ServerPlayer from = ((CraftPlayer) listPlayer).getHandle();
        ClientboundPlayerInfoUpdatePacket packet = ClientboundPlayerInfoUpdatePacket
                .createPlayerInitializing(Arrays.asList(from));
        boolean list = from instanceof NPCHolder
                ? !((NPCHolder) from).getNPC().getMetaData().get("removefromtablist", true)
                : false;
        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(from.getUUID(),
                from.getGameProfile(), list, from.latency, from.gameMode.getGameModeForPlayer(),
                list ? from.getTabListDisplayName() : Component.empty(),
                from.getChatSession() == null ? null : from.getChatSession().asData());
        try {
            PLAYERINFO_ENTRIES.invoke(packet, Lists.newArrayList(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        sendPacket(recipient, packet);
        return true;
    }

    public static void setAggressive(org.bukkit.entity.Entity entity, boolean aggro) {
        Entity handle = getHandle(entity);
        if (!(handle instanceof Mob))
            return;
        ((Mob) handle).setAggressive(aggro);
    }

    public static void sendTabListRemove(Player recipient, Collection<? extends ServerPlayer> npcs) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(npcs);
        ServerPlayer[] entities = new ServerPlayer[npcs.size()];
        int i = 0;
        for (ServerPlayer npc : npcs) {
            entities[i] = (ServerPlayer) npc;
            i++;
        }
        sendPacket(recipient, new ClientboundPlayerInfoRemovePacket(
                npcs.stream().map(e -> ((ServerPlayer) e).getUUID()).collect(Collectors.toList())));
    }

    public static void sendTeamPacket(Player recipient, Team team, int mode) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(team);
        if (TEAM_FIELD == null) {
            TEAM_FIELD = NMS.getGetter(team.getClass(), "team");
        }
        try {
            PlayerTeam nmsTeam = (PlayerTeam) TEAM_FIELD.invoke(team);
            if (mode == 1) {
                sendPacket(recipient, ClientboundSetPlayerTeamPacket.createRemovePacket(nmsTeam));
            } else {
                sendPacket(recipient, ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, mode == 0));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void sendTabListRemove(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);
        sendPacket(recipient, new ClientboundPlayerInfoRemovePacket(Arrays.asList(getHandle(listPlayer).getUUID())));
    }

    public static MethodHandle getGetter(Class<?> clazz, String name) {
        return getGetter(clazz, name, true);
    }

    public static MethodHandle getGetter(Class<?> clazz, String name, boolean log) {
        try {
            return LOOKUP.unreflectGetter(getField(clazz, name, log));
        } catch (Exception e) {
            if (log) {
                Bukkit.getLogger().info("Could not fetch NMS field: " + name + ": " + e.getLocalizedMessage());
            }
        }
        return null;
    }

    public static void setHeadYaw(org.bukkit.entity.Entity entity, float yaw) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity))
            return;
        LivingEntity handle = (LivingEntity) getHandle(entity);
        yaw = Util.clamp(yaw);
        handle.yBodyRotO = yaw;
        if (!(handle instanceof net.minecraft.world.entity.player.Player)) {
            handle.setYBodyRot(yaw);
        }
        handle.setYHeadRot(yaw);
    }

    public static void setPitch(org.bukkit.entity.Entity entity, float pitch) {
        getHandle(entity).setXRot(pitch);
    }

    public static void setBodyYaw(org.bukkit.entity.Entity entity, float yaw) {
        getHandle(entity).setYRot(yaw);
    }

    public static void sendPositionUpdate(org.bukkit.entity.Entity from, boolean position, Float bodyYaw, Float pitch,
            Float headYaw) {
        List<Packet<?>> toSend = getPositionUpdate(from, position, bodyYaw, pitch, headYaw);
        sendPacketsNearby(null, from.getLocation(), toSend, 64);
    }

    public static void setAdvancement(Player entity, PlayerAdvancements instance) {
        try {
            ADVANCEMENTS_PLAYER_SETTER.invoke(getHandle(entity), instance);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void sendPacketsNearby(Player from, Location location, Collection<Packet<?>> packets, double radius) {
        radius *= radius;
        final org.bukkit.World world = location.getWorld();
        for (Player player : Citizen.getLocationLookup().getNearbyPlayers(location, radius)) {
            if (world != player.getWorld() || (from != null && !player.canSee(from))
                    || (location.distanceSquared(player.getLocation(PACKET_CACHE_LOCATION)) > radius)) {
                continue;
            }
            for (Packet<?> packet : packets) {
                sendPacket(player, packet);
            }
        }
    }

    public static boolean isSolid(org.bukkit.block.Block in) {
        BlockState data = ((CraftBlock) in).getNMS();
        return data.isSuffocating(((CraftWorld) in.getWorld()).getHandle(),
                new BlockPos(in.getX(), in.getY(), in.getZ()));
    }

    public static void callKnockbackEvent(NPC npc, float strength, double dx, double dz,
            Consumer<NPCKnockbackEvent> cb) {
        if (SUPPORT_KNOCKBACK_RESISTANCE && npc.getEntity() instanceof LivingEntity) {
            try {
                org.bukkit.attribute.AttributeInstance attribute = ((org.bukkit.entity.LivingEntity) npc.getEntity())
                        .getAttribute(
                                org.bukkit.attribute.Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                if (attribute != null) {
                    strength *= (float) (1 - attribute.getValue());
                }
            } catch (Throwable t) {
                SUPPORT_KNOCKBACK_RESISTANCE = false;
            }
        }
        org.bukkit.util.Vector vector = npc.getEntity().getVelocity();
        org.bukkit.util.Vector impulse = new org.bukkit.util.Vector(dx, 0, dz).normalize().multiply(strength);
        org.bukkit.util.Vector delta = new org.bukkit.util.Vector(vector.getX() / 2 - impulse.getX() - vector.getX(),
                -vector.getY()
                        + (npc.getEntity().isOnGround() ? Math.min(0.4, vector.getY() / 2 + strength) : vector.getY()),
                vector.getZ() / 2 - impulse.getZ() - vector.getZ());
        NPCKnockbackEvent event = new NPCKnockbackEvent(npc, strength, delta, null);
        Bukkit.getPluginManager().callEvent(event);
        if (!PAPER_KNOCKBACK_EVENT_EXISTS) {
            event.getKnockbackVector().multiply(new org.bukkit.util.Vector(-1, 0, -1));
        }
        if (!event.isCancelled()) {
            cb.accept(event);
        }
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        if (packet == null)
            return;
        ((ServerPlayer) getHandle(player)).connection.send(packet);
    }

    public static void initNetworkManager(Connection network) {
        network.channel = new EmptyChannel(null);
        network.address = new SocketAddress() {
            @Serial
            private static final long serialVersionUID = 8207338859896320185L;
        };
    }

    public static void setBukkitEntity(Entity entity, CraftEntity bukkitEntity) {
        try {
            BUKKITENTITY_FIELD_SETTER.invoke(entity, bukkitEntity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        getHandle(entity).setMaxUpStep(height);
    }

    public static List<MethodHandle> getFieldsOfType(Class<?> clazz, Class<?> type) {
        List<Field> found = getFieldsMatchingType(clazz, type, false);
        if (found.isEmpty())
            return null;
        return found.stream().map(f -> {
            try {
                return LOOKUP.unreflectGetter(f);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Packet<?>> getPositionUpdate(org.bukkit.entity.Entity from, boolean position, Float bodyYaw,
            Float pitch, Float headYaw) {
        Entity handle = getHandle(from);
        if (bodyYaw == null) {
            bodyYaw = handle.getYRot();
        }
        if (pitch == null) {
            pitch = handle.getXRot();
        }
        List<Packet<?>> toSend = new ArrayList<>();
        if (position) {
            ChunkMap.TrackedEntity entry = ((ServerLevel) handle.level()).getChunkSource().chunkMap.entityMap
                    .get(handle.getId());
            VecDeltaCodec vdc = null;
            try {
                vdc = (VecDeltaCodec) POSITION_CODEC_GETTER.invoke((ServerEntity) SERVER_ENTITY_GETTER.invoke(entry));
            } catch (Throwable e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
            Vec3 pos = handle.trackingPosition();
            toSend.add(new ClientboundMoveEntityPacket.PosRot(handle.getId(), (short) vdc.encodeX(pos),
                    (short) vdc.encodeY(pos), (short) vdc.encodeZ(pos), (byte) (bodyYaw * 256.0F / 360.0F),
                    (byte) (pitch * 256.0F / 360.0F), handle.onGround));
        } else {
            toSend.add(new ClientboundMoveEntityPacket.Rot(handle.getId(), (byte) (bodyYaw * 256.0F / 360.0F),
                    (byte) (pitch * 256.0F / 360.0F), handle.onGround));
        }
        if (headYaw != null) {
            toSend.add(new ClientboundRotateHeadPacket(handle, (byte) (headYaw * 256.0F / 360.0F)));
        }
        return toSend;
    }

    @SuppressWarnings("rawtypes")
    public static Iterable<Object> createBundlePacket(List packets) {
        return packets.isEmpty() ? ImmutableList.of() : ImmutableList.of(new ClientboundBundlePacket(packets));
    }

    public static EntityPacketTracker createPacketTracker(org.bukkit.entity.Entity entity) {
        return createPacketTracker(entity, new EntityPacketTracker.PacketAggregator());
    }

    public static EntityPacketTracker createPacketTracker(org.bukkit.entity.Entity entity,
            EntityPacketTracker.PacketAggregator agg) {
        Entity handle = getHandle(entity);
        Set<ServerPlayerConnection> linked = Sets.newIdentityHashSet();
        ServerEntity tracker = new ServerEntity((ServerLevel) handle.level(), handle, handle.getType().updateInterval(),
                handle.getType().trackDeltas(), agg::send, linked);
        Map<EquipmentSlot, ItemStack> equipment = Maps.newEnumMap(EquipmentSlot.class);
        return new EntityPacketTracker() {
            @Override
            public void link(Player player) {
                ServerPlayer p = (ServerPlayer) getHandle(player);
                handle.unsetRemoved();
                tracker.addPairing(p);
                linked.add(p.connection);
                agg.add(p.getUUID(), packet -> p.connection.send((Packet<?>) packet));
            }

            @Override
            public void run() {
                if (handle instanceof LivingEntity entity) {
                    boolean changed = false;
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        ItemStack old = equipment.getOrDefault(slot, ItemStack.EMPTY);
                        ItemStack curr = entity.getItemBySlot(slot);
                        if (!changed && entity.equipmentHasChanged(old, curr)) {
                            changed = true;
                        }
                        equipment.put(slot, curr);
                    }
                    if (changed) {
                        List<com.mojang.datafixers.util.Pair<EquipmentSlot, ItemStack>> vals = Lists.newArrayList();
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            vals.add(com.mojang.datafixers.util.Pair.of(slot, equipment.get(slot)));
                        }
                        agg.send(new ClientboundSetEquipmentPacket(handle.getId(), vals));
                    }
                }
                tracker.sendChanges();
            }

            @Override
            public void unlink(Player player) {
                ServerPlayer p = (ServerPlayer) getHandle(player);
                tracker.removePairing(p);
                linked.remove(p.connection);
                agg.removeConnection(p.getUUID());
            }

            @Override
            public void unlinkAll(Consumer<Player> callback) {
                handle.remove(Entity.RemovalReason.KILLED);
                for (ServerPlayerConnection link : Lists.newArrayList(linked)) {
                    Player entity = link.getPlayer().getBukkitEntity();
                    unlink(entity);
                    if (callback != null) {
                        callback.accept(entity);
                    }
                }
                linked.clear();
            }
        };
    }

    public static void setLocationDirectly(org.bukkit.entity.Entity entity, Location location) {
        getHandle(entity).setPos(location.getX(), location.getY(), location.getZ());
        getHandle(entity).moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                location.getPitch());
    }

    public static void removeFromWorld(org.bukkit.entity.Entity entity) {
        Preconditions.checkNotNull(entity);
        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        ((ServerLevel) nmsEntity.level()).getChunkSource().removeEntity(nmsEntity);
    }

    public static void remove(org.bukkit.entity.Entity entity) {
        getHandle(entity).remove(Entity.RemovalReason.KILLED);
    }

    public static boolean addEntityToWorld(org.bukkit.entity.Entity entity, CreatureSpawnEvent.SpawnReason custom) {
        int viewDistance = -1;
        ChunkMap chunkMap = null;

        try {
            if (entity instanceof Player) {
                chunkMap = ((ServerChunkCache) getHandle(entity).level().getChunkSource()).chunkMap;
                viewDistance = (int) PLAYER_CHUNK_MAP_VIEW_DISTANCE_GETTER.invoke(chunkMap);
                PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER.invoke(chunkMap, -1);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        boolean success = getHandle(entity).level().addFreshEntity(getHandle(entity), custom);
        try {
            if (chunkMap != null) {
                PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER.invoke(chunkMap, viewDistance);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return success;
    }

    public static float getStepHeight(org.bukkit.entity.Entity entity) {
        return getHandle(entity).maxUpStep();
    }

    public static net.minecraft.world.entity.Entity getHandle(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity))
            return null;
        return ((CraftEntity) entity).getHandle();
    }

    private static LivingEntity getHandle(org.bukkit.entity.LivingEntity entity) {
        return (LivingEntity) getHandle((org.bukkit.entity.Entity) entity);
    }

    public static boolean isValid(org.bukkit.entity.Entity entity) {
        net.minecraft.world.entity.Entity handle = getHandle(entity);
        return handle.valid && handle.isAlive();
    }

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
            CHUNKMAP_UPDATE_PLAYER_STATUS.invoke(((ServerLevel) handle.level()).getChunkSource().chunkMap, handle,
                    !remove);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // PlayerUpdateTask.addOrRemove(entity, remove);
    }

    public static Field getField(Class<?> clazz, String field) {
        return getField(clazz, field, true);
    }

    public static Field getField(Class<?> clazz, String field, boolean log) {
        if (clazz == null)
            return null;
        Field f = null;
        try {
            f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            if (log) {
                Bukkit.getLogger()
                        .info("Could not fetch NMS field " + field + ": " + e.getLocalizedMessage());
            }
            return null;
        }
    }

    public static MethodHandle getFinalSetter(Field field, boolean log) {
        if (field == null)
            return null;
        if (MODIFIERS_FIELD == null) {
            if (UNSAFE == null) {
                try {
                    UNSAFE = NMS.getField(Class.forName("sun.misc.Unsafe"), "theUnsafe").get(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (log) {
                        Bukkit.getLogger()
                                .info("Could not fetch NMS field " + field.getName() + ": " + e.getLocalizedMessage());
                    }
                    return null;
                }
                UNSAFE_STATIC_FIELD_OFFSET = getMethodHandle(UNSAFE.getClass(), "staticFieldOffset", true, Field.class)
                        .bindTo(UNSAFE);
                UNSAFE_FIELD_OFFSET = getMethodHandle(UNSAFE.getClass(), "objectFieldOffset", true, Field.class)
                        .bindTo(UNSAFE);
                UNSAFE_PUT_OBJECT = getMethodHandle(UNSAFE.getClass(), "putObject", true, Object.class, long.class,
                        Object.class).bindTo(UNSAFE);
                UNSAFE_PUT_INT = getMethodHandle(UNSAFE.getClass(), "putInt", true, Object.class, long.class, int.class)
                        .bindTo(UNSAFE);
                UNSAFE_PUT_FLOAT = getMethodHandle(UNSAFE.getClass(), "putFloat", true, Object.class, long.class,
                        float.class).bindTo(UNSAFE);
                UNSAFE_PUT_DOUBLE = getMethodHandle(UNSAFE.getClass(), "putDouble", true, Object.class, long.class,
                        double.class).bindTo(UNSAFE);
                UNSAFE_PUT_BOOLEAN = getMethodHandle(UNSAFE.getClass(), "putBoolean", true, Object.class, long.class,
                        boolean.class).bindTo(UNSAFE);
                UNSAFE_PUT_LONG = getMethodHandle(UNSAFE.getClass(), "putLong", true, Object.class, long.class,
                        long.class).bindTo(UNSAFE);
            }
            try {
                boolean isStatic = Modifier.isStatic(field.getModifiers());
                long offset = (long) (isStatic ? UNSAFE_STATIC_FIELD_OFFSET.invoke(field)
                        : UNSAFE_FIELD_OFFSET.invoke(field));
                MethodHandle mh = field.getType() == int.class ? UNSAFE_PUT_INT
                        : field.getType() == boolean.class ? UNSAFE_PUT_BOOLEAN
                                : field.getType() == double.class ? UNSAFE_PUT_DOUBLE
                                        : field.getType() == float.class ? UNSAFE_PUT_FLOAT
                                                : field.getType() == long.class ? UNSAFE_PUT_LONG : UNSAFE_PUT_OBJECT;
                return isStatic ? MethodHandles.insertArguments(mh, 0, field.getDeclaringClass(), offset)
                        : MethodHandles.insertArguments(mh, 1, offset);
            } catch (Throwable t) {
                t.printStackTrace();
                if (log) {
                    Bukkit.getLogger()
                            .info("Could not fetch NMS field " + field.getName() + ": " + t.getLocalizedMessage());
                }
                return null;
            }
        }
        try {
            MODIFIERS_FIELD.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            if (log) {
                Bukkit.getLogger()
                        .info("Could not fetch NMS field " + field.getName() + ": " + e.getLocalizedMessage());
            }
            return null;
        }
        try {
            return LOOKUP.unreflectSetter(field);
        } catch (Exception e) {
            if (log) {
                Bukkit.getLogger()
                        .info("Could not fetch NMS field " + field.getName() + ": " + e.getLocalizedMessage());
            }
        }
        return null;
    }

    public static MethodHandle getMethodHandle(Class<?> clazz, String method, boolean log, Class<?>... params) {
        if (clazz == null)
            return null;
        try {
            return LOOKUP.unreflect(getMethod(clazz, method, log, params));
        } catch (Exception e) {
            if (log) {
                Bukkit.getLogger().info("Could not fetch NMS method " + method + ": " + e.getLocalizedMessage());
            }
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String method, boolean log, Class<?>... params) {
        if (clazz == null)
            return null;
        Method f = null;
        try {
            f = clazz.getDeclaredMethod(method, params);
            f.setAccessible(true);
        } catch (Exception e) {
            if (log) {
                Bukkit.getLogger().info("Could not fetch NMS method " + method + ": " + e.getLocalizedMessage());
            }
        }
        return f;
    }

    public static MethodHandle getFirstGetter(Class<?> clazz, Class<?> type) {
        try {
            List<Field> found = getFieldsMatchingType(clazz, type, false);
            if (found.isEmpty())
                return null;
            return LOOKUP.unreflectGetter(found.get(0));
        } catch (Exception ignored) {
            Bukkit.getLogger().info("Could not fetch NMS field " + type + ": " + ignored.getLocalizedMessage());
        }
        return null;
    }

    public static void setNoGravity(org.bukkit.entity.Entity entity, boolean nogravity) {
        Entity handle = getHandle(entity);
        handle.setNoGravity(nogravity);
        if (!(handle instanceof Mob) || !(entity instanceof NPCHolder))
            return;
        Mob mob = (Mob) handle;
        NPC npc = ((NPCHolder) entity).getNPC();
        if (!(mob.getMoveControl() instanceof FlyingMoveControl) || npc.isFlyable())
            return;
        try {
            if (nogravity) {
                boolean old = (boolean) FLYING_MOVECONTROL_FLOAT_GETTER.invoke(mob.getMoveControl());
                FLYING_MOVECONTROL_FLOAT_SETTER.invoke(mob.getMoveControl(), true);
                npc.getMetaData().set("flying-nogravity-float", old);
            } else if (npc.isFlyable()) {
                FLYING_MOVECONTROL_FLOAT_SETTER.invoke(mob.getMoveControl(),
                        npc.getMetaData().get("flying-nogravity-float"));
                npc.getMetaData().remove("flying-nogravity-float");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static List<Field> getFieldsMatchingType(Class<?> clazz, Class<?> type, boolean allowStatic) {
        List<Field> found = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (allowStatic ^ Modifier.isStatic(field.getModifiers()))
                continue;
            if (field.getType() == type) {
                found.add(field);
                field.setAccessible(true);
            }
        }
        return found;
    }

    public static MethodHandle getFirstMethodHandle(Class<?> clazz, boolean log, Class<?>... params) {
        return getFirstMethodHandleWithReturnType(clazz, log, null, params);
    }

    public static MethodHandle getFirstMethodHandleWithReturnType(Class<?> clazz, boolean log, Class<?> returnType,
            Class<?>... params) {
        if (clazz == null)
            return null;
        try {
            Method first = null;
            for (Method method : clazz.getDeclaredMethods()) {
                if (returnType != null && !returnType.equals(method.getReturnType()))
                    continue;
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == params.length) {
                    first = method;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (paramTypes[i] != params[i]) {
                            first = null;
                        }
                    }
                    if (first != null) {
                        break;
                    }
                }
            }
            if (first == null)
                return null;
            first.setAccessible(true);
            return LOOKUP.unreflect(first);
        } catch (Exception e) {
            if (log) {
                Bukkit.getLogger().info("Could not fetch NMS method " + e.getLocalizedMessage());
            }
        }
        return null;
    }

    public static Runnable playerTicker(Player entity) {
        Runnable tick = ((ServerPlayer) getHandle(entity))::doTick;
        return () -> {
            if (entity.isValid()) {
                tick.run();
            }
        };
    }

    public static MethodHandle getFirstFinalSetter(Class<?> clazz, Class<?> type) {
        try {
            List<Field> found = getFieldsMatchingType(clazz, type, false);
            if (found.isEmpty())
                return null;
            return getFinalSetter(found.get(0), true);
        } catch (Exception e) {
            Bukkit.getLogger().info("Could not fetch NMS field " + type + ": " + e.getLocalizedMessage());
        }
        return null;
    }

    public static void setAttribute(LivingEntity entity, Attribute attribute, double value) {
        AttributeInstance attr = entity.getAttribute(attribute);
        if (attr == null) {
            try {
                AttributeSupplier provider = (AttributeSupplier) ATTRIBUTE_SUPPLIER.invoke(entity.getAttributes());
                Map<Attribute, AttributeInstance> all = Maps
                        .newHashMap((Map<Attribute, AttributeInstance>) ATTRIBUTE_PROVIDER_MAP.invoke(provider));
                all.put(attribute, new AttributeInstance(attribute, att -> {
                    throw new UnsupportedOperationException(
                            "Tried to change value for default attribute instance FOLLOW_RANGE");
                }));
                ATTRIBUTE_PROVIDER_MAP_SETTER.invoke(provider, ImmutableMap.copyOf(all));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            attr = entity.getAttribute(attribute);
        }
        attr.setBaseValue(value);
    }

    public static void flyingMoveLogic(LivingEntity entity, Vec3 vec3d) {
        if (entity.isEffectiveAi() || entity.isControlledByLocalInstance()) {
            double d0 = 0.08D;
            boolean flag = ((entity.getDeltaMovement()).y <= 0.0D);
            if (flag && entity.hasEffect(MobEffects.SLOW_FALLING)) {
                d0 = 0.01D;
                entity.fallDistance = 0.0F;
            }
            FluidState fluid = entity.level().getFluidState(entity.blockPosition());
            if (entity.isInWater() && !entity.canStandOnFluid(fluid)) {
                double d1 = entity.getY();
                float f = entity.isSprinting() ? 0.9F : 0.8F;
                float f1 = 0.02F;
                float f2 = EnchantmentHelper.getDepthStrider(entity);
                if (f2 > 3.0F)
                    f2 = 3.0F;
                if (!entity.onGround())
                    f2 *= 0.5F;
                if (f2 > 0.0F) {
                    f += (0.546F - f) * f2 / 3.0F;
                    f1 += (entity.getSpeed() - f1) * f2 / 3.0F;
                }
                if (entity.hasEffect(MobEffects.DOLPHINS_GRACE))
                    f = 0.96F;
                entity.moveRelative(f1, vec3d);
                entity.move(MoverType.SELF, entity.getDeltaMovement());
                Vec3 vec3d1 = entity.getDeltaMovement();
                if (entity.horizontalCollision && entity.onClimbable())
                    vec3d1 = new Vec3(vec3d1.x, 0.2D, vec3d1.z);
                entity.setDeltaMovement(vec3d1.multiply(f, 0.8D, f));
                Vec3 vec3d2 = entity.getFluidFallingAdjustedMovement(d0, flag, entity.getDeltaMovement());
                entity.setDeltaMovement(vec3d2);
                if (entity.horizontalCollision
                        && entity.isFree(vec3d2.x, vec3d2.y + 0.6D - entity.getY() + d1, vec3d2.z))
                    entity.setDeltaMovement(vec3d2.x, 0.3D, vec3d2.z);
            } else if (entity.isInLava() && !entity.canStandOnFluid(fluid)) {
                double d1 = entity.getY();
                entity.moveRelative(0.02F, vec3d);
                entity.move(MoverType.SELF, entity.getDeltaMovement());
                if (entity.getFluidHeight(FluidTags.LAVA) <= entity.getFluidJumpThreshold()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, 0.8D, 0.5D));
                    Vec3 vec3 = entity.getFluidFallingAdjustedMovement(d0, flag, entity.getDeltaMovement());
                    entity.setDeltaMovement(vec3);
                } else {
                    entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5D));
                }
                if (!entity.isNoGravity())
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -d0 / 4.0D, 0.0D));
                Vec3 vec3d3 = entity.getDeltaMovement();
                if (entity.horizontalCollision
                        && entity.isFree(vec3d3.x, vec3d3.y + 0.6D - entity.getY() + d1, vec3d3.z))
                    entity.setDeltaMovement(vec3d3.x, 0.3D, vec3d3.z);
            } else if (entity.isFallFlying()) {
                Vec3 vec3d4 = entity.getDeltaMovement();
                if (vec3d4.y > -0.5D)
                    entity.fallDistance = 1.0F;
                Vec3 vec3d5 = entity.getLookAngle();
                float f = entity.getXRot() * 0.017453292F;
                double d2 = Math.sqrt(vec3d5.x * vec3d5.x + vec3d5.z * vec3d5.z);
                double d3 = vec3d4.horizontalDistance();
                double d4 = vec3d5.length();
                float f3 = Mth.cos(f);
                f3 = (float) (f3 * f3 * Math.min(1.0D, d4 / 0.4D));
                vec3d4 = entity.getDeltaMovement().add(0.0D, d0 * (-1.0D + f3 * 0.75D), 0.0D);
                if (vec3d4.y < 0.0D && d2 > 0.0D) {
                    double d5 = vec3d4.y * -0.1D * f3;
                    vec3d4 = vec3d4.add(vec3d5.x * d5 / d2, d5, vec3d5.z * d5 / d2);
                }
                if (f < 0.0F && d2 > 0.0D) {
                    double d5 = d3 * -Mth.sin(f) * 0.04D;
                    vec3d4 = vec3d4.add(-vec3d5.x * d5 / d2, d5 * 3.2D, -vec3d5.z * d5 / d2);
                }
                if (d2 > 0.0D)
                    vec3d4 = vec3d4.add((vec3d5.x / d2 * d3 - vec3d4.x) * 0.1D, 0.0D,
                            (vec3d5.z / d2 * d3 - vec3d4.z) * 0.1D);
                entity.setDeltaMovement(vec3d4.multiply(0.99D, 0.98D, 0.99D));
                entity.move(MoverType.SELF, entity.getDeltaMovement());
                if (entity.horizontalCollision && !entity.level().isClientSide) {
                    double d5 = entity.getDeltaMovement().horizontalDistance();
                    double d6 = d3 - d5;
                    float f4 = (float) (d6 * 10.0D - 3.0D);
                    if (f4 > 0.0F) {
                        entity.playSound(entity.getFallDamageSound0((int) f4), 1.0F, 1.0F);
                        entity.hurt(entity.damageSources().flyIntoWall(), f4);
                    }
                }
                if (entity.onGround() && !entity.level().isClientSide && entity.getSharedFlag(7)
                        && !CraftEventFactory.callToggleGlideEvent(entity, false).isCancelled())
                    entity.setSharedFlag(7, false);
            } else {
                BlockPos blockposition = BlockPos.containing(entity.getX(), (entity.getBoundingBox()).minY - 0.5D,
                        entity.getZ());
                float f5 = entity.level().getBlockState(blockposition).getBlock().getFriction();
                float f = entity.onGround() ? (f5 * 0.91F) : 0.91F;
                Vec3 vec3d6 = entity.handleRelativeFrictionAndCalculateMovement(vec3d, f5);
                double d7 = vec3d6.y;
                if (entity.hasEffect(MobEffects.LEVITATION)) {
                    d7 += (0.05D * (entity.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec3d6.y) * 0.2D;
                    entity.fallDistance = 0.0F;
                } else if (entity.level().isClientSide && !entity.level().hasChunkAt(blockposition)) {
                    if (entity.getY() > entity.level().getMinBuildHeight()) {
                        d7 = -0.1D;
                    } else {
                        d7 = 0.0D;
                    }
                } else if (!entity.isNoGravity()) {
                    d7 -= d0;
                }
                if (entity.shouldDiscardFriction()) {
                    entity.setDeltaMovement(vec3d6.x, d7, vec3d6.z);
                } else {
                    entity.setDeltaMovement(vec3d6.x * f, d7 * 0.98D, vec3d6.z * f);
                }
            }
        }
        entity.calculateEntityAnimation(entity instanceof net.minecraft.world.entity.animal.FlyingAnimal);
    }

    public static void activate(org.bukkit.entity.Entity entity) {
        getHandle(entity).activatedTick = MinecraftServer.currentTick;
    }

    public static MethodHandle getSetter(Class<?> clazz, String name) {
        return getSetter(clazz, name, true);
    }

    public static MethodHandle getSetter(Class<?> clazz, String name, boolean log) {
        try {
            return LOOKUP.unreflectSetter(getField(clazz, name, log));
        } catch (Exception e) {
            if (log) {
                Bukkit.getLogger().info("Could not fetch NMS field " + name + ": " + e.getLocalizedMessage());

            }
        }
        return null;
    }

    public static SoundEvent getSoundEffect(NPC npc, SoundEvent sound, NPC.Metadata metadata) {
        return npc == null || !npc.getMetaData().has(metadata) ? sound
                : BuiltInRegistries.SOUND_EVENT.get(
                        new ResourceLocation(npc.getMetaData().get(metadata, sound == null ? "" : sound.toString())));
    }

    public static void setKnockbackResistance(org.bukkit.entity.LivingEntity entity, double d) {
        LivingEntity handle = getHandle(entity);
        handle.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(d);
    }

    public static MethodHandle getFirstSetter(Class<?> clazz, Class<?> type) {
        try {
            List<Field> found = getFieldsMatchingType(clazz, type, false);
            if (found.isEmpty())
                return null;
            return LOOKUP.unreflectSetter(found.get(0));
        } catch (Exception e) {
            Bukkit.getLogger().info("Could not fetch NMS field " + type + ": " + e.getLocalizedMessage());
        }
        return null;
    }

    public static void giveReflectiveAccess(Class<?> from, Class<?> to) {
        try {
            if (GET_MODULE == null) {
                Class<?> module = Class.forName("java.lang.Module");
                GET_MODULE = Class.class.getMethod("getModule");
                ADD_OPENS = module.getMethod("addOpens", String.class, module);
            }
            ADD_OPENS.invoke(GET_MODULE.invoke(from), from.getPackage().getName(), GET_MODULE.invoke(to));
        } catch (Exception ignored) {
        }
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle FLYING_MOVECONTROL_FLOAT_GETTER = getFirstGetter(FlyingMoveControl.class,
            boolean.class);
    private static final MethodHandle FLYING_MOVECONTROL_FLOAT_SETTER = getFirstSetter(FlyingMoveControl.class,
            boolean.class);
    private static final MethodHandle ADVANCEMENTS_PLAYER_SETTER = getFirstFinalSetter(ServerPlayer.class,
            PlayerAdvancements.class);
    private static final MethodHandle ATTRIBUTE_SUPPLIER = getFirstGetter(AttributeMap.class,
            AttributeSupplier.class);
    private static final MethodHandle ATTRIBUTE_PROVIDER_MAP = getFirstGetter(AttributeSupplier.class, Map.class);
    private static final MethodHandle ATTRIBUTE_PROVIDER_MAP_SETTER = getFirstFinalSetter(AttributeSupplier.class,
            Map.class);
    private static final MethodHandle CHUNKMAP_UPDATE_PLAYER_STATUS = getMethodHandle(ChunkMap.class, "a", true,
            ServerPlayer.class, boolean.class);

    private static final MethodHandle BUKKITENTITY_FIELD_SETTER = getSetter(Entity.class, "bukkitEntity");

    private static final MethodHandle POSITION_CODEC_GETTER = getFirstGetter(ServerEntity.class,
            VecDeltaCodec.class);
    private static final MethodHandle SERVER_ENTITY_GETTER = getFirstGetter(ChunkMap.TrackedEntity.class,
            ServerEntity.class);
    private static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_GETTER = getFirstGetter(ChunkMap.class,
            int.class);
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER = getFirstSetter(ChunkMap.class,
            int.class);
    private static final MethodHandle PLAYER_INFO_ENTRIES_LIST = NMS
            .getFirstFinalSetter(ClientboundPlayerInfoUpdatePacket.class, List.class);
    private static final MethodHandle PLAYERINFO_ENTRIES = PLAYER_INFO_ENTRIES_LIST;
    private static boolean SUPPORT_KNOCKBACK_RESISTANCE = true;
    private static boolean PAPER_KNOCKBACK_EVENT_EXISTS = true;
    private static Method ADD_OPENS;
    private static Method GET_MODULE;
    private static Object UNSAFE;
    private static MethodHandle UNSAFE_FIELD_OFFSET;
    private static MethodHandle UNSAFE_PUT_BOOLEAN;
    private static Field MODIFIERS_FIELD;
    private static MethodHandle UNSAFE_PUT_DOUBLE;
    private static MethodHandle UNSAFE_PUT_FLOAT;
    private static MethodHandle UNSAFE_PUT_INT;
    private static MethodHandle UNSAFE_PUT_LONG;
    private static MethodHandle UNSAFE_PUT_OBJECT;
    private static MethodHandle UNSAFE_STATIC_FIELD_OFFSET;
    private static MethodHandle TEAM_FIELD;


    static {
        giveReflectiveAccess(Field.class, NMS.class);
        MODIFIERS_FIELD = getField(Field.class, "modifiers", false);
    }

}
