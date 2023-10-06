package me.winflix.vitalcore.citizen.models;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.citizen.enums.*;
import me.winflix.vitalcore.citizen.events.NPCAddTraitEvent;
import me.winflix.vitalcore.citizen.interfaces.CurrentLocation;
import me.winflix.vitalcore.citizen.interfaces.EntityController;
import me.winflix.vitalcore.citizen.interfaces.Spawned;
import me.winflix.vitalcore.citizen.interfaces.Trait;
import me.winflix.vitalcore.citizen.tasks.PlayerUpdateTask;
import me.winflix.vitalcore.citizen.trait.traits.Gravity;
import me.winflix.vitalcore.citizen.utils.Util;
import me.winflix.vitalcore.citizen.utils.controller.EntityControllers;
import me.winflix.vitalcore.citizen.utils.metadata.MetadataStore;
import me.winflix.vitalcore.citizen.trait.traits.MobTrait;
import me.winflix.vitalcore.citizen.trait.traits.PacketHandlerNPC;
import me.winflix.vitalcore.citizen.trait.traits.ScoreboardTrait;
import me.winflix.vitalcore.citizen.utils.metadata.SimpleMetadataStore;
import me.winflix.vitalcore.core.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.util.BoundingBox;

public class NPC {
    private static AtomicInteger atomicInteger;
    private final int entityID;
    private final Location location;
    private final UUID uuid;
    private GameProfile profile;
    private String name;
    private MetadataStore metadata = new SimpleMetadataStore() {
        @Override
        public void remove(String key) {
            super.remove(key);
            if (getEntity() != null) {
                getEntity().removeMetadata(key, VitalCore.getPlugin());
            }
        }

        @Override
        public void set(String key, Object data) {
            super.set(key, data);
            if (getEntity() != null) {
                getEntity().setMetadata(key, new FixedMetadataValue(VitalCore.getPlugin(), data));
            }
        }

        @Override
        public void setPersistent(String key, Object data) {
            super.setPersistent(key, data);
            if (getEntity() != null) {
                getEntity().setMetadata(key, new FixedMetadataValue(VitalCore.getPlugin(), data));
            }
        }
    };

    protected final Map<Class<? extends Trait>, Trait> traits = new HashMap<>();
    private final List<Runnable> runnables = new ArrayList<>();
    private EntityController entityController;
    private int updateCounter = 0;

    static {
        try {
            Field field = Entity.class.getDeclaredField("d");
            field.setAccessible(true);
            atomicInteger = (AtomicInteger) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Error when accessing the field 'd' of the Entity class: " + e.getMessage());
        }
    }

    public NPC(UUID uuid, Location location, String displayName, EntityType type) {
        EntityController controller = EntityControllers.createForType(type);
        this.entityID = atomicInteger.incrementAndGet();
        this.name = displayName;
        this.uuid = uuid;
        this.location = location;
        this.profile = new GameProfile(uuid, displayName);

        Citizen.getTraitFactory().addDefaultTraits(this);
        Citizen.npcs.put(this.uuid, this);

        setEntityController(controller);
    }

    public NPC(Location location, String displayName, EntityType type) {
        this(UUID.randomUUID(), location, displayName, type);
    }

    public boolean spawn(Location at, SpawnReason reason) {
        Preconditions.checkNotNull(at, "location cannot be null");
        Preconditions.checkNotNull(reason, "reason cannot be null");
        if (getEntity() != null) {
            Bukkit.getLogger().info("Tried to spawn: " + this + " while already spawned. SpawnReason." + reason);
            return false;
        }
        if (at.getWorld() == null) {
            Bukkit.getLogger().info("Tried to spawn: " + this + "but the world was null. SpawnReason." + reason);
            return false;
        }
        at = at.clone();

        if (reason == SpawnReason.CHUNK_LOAD || reason == SpawnReason.COMMAND) {
            at.getChunk().load();
        }

        getOrAddTrait(CurrentLocation.class).setLocation(at);
        entityController.create(at.clone(), this);
        getEntity().setMetadata("NPC", new FixedMetadataValue(VitalCore.getPlugin(), true));
        getEntity().setMetadata("NPC-ID", new FixedMetadataValue(VitalCore.getPlugin(), getEntityID()));

        Collection<Trait> onPreSpawn = traits.values();
        for (Trait trait : onPreSpawn.toArray(new Trait[onPreSpawn.size()])) {
            try {
                trait.onPreSpawn();
            } catch (Throwable ex) {
                Bukkit.getLogger().info("An exception occurred while the trait " + trait.getName() + getEntityID());
                ex.printStackTrace();
            }
        }

        boolean couldSpawn = entityController.spawn(at);

        if (!couldSpawn) {
            entityController.remove();
            return false;
        }

        NMS.setLocationDirectly(getEntity(), at);
        NMS.setHeadYaw(getEntity(), at.getYaw());
        NMS.setBodyYaw(getEntity(), at.getYaw());

        final Location to = at;
        Consumer<Runnable> postSpawn = new Consumer<Runnable>() {
            private int timer;

            @Override
            public void accept(Runnable cancel) {
                if (getEntity() == null || (!hasTrait(PacketHandlerNPC.class) && !getEntity().isValid())) {
                    if (timer++ > Util.toTicks(Util.parseDuration("1s".toString(), null))) {
                        Bukkit.getLogger().info("Couldn't spawn " + this + " waited " + timer + " ticks but entity not added to world");
                        entityController.remove();
                        cancel.run();
                    }

                    return;
                }

                // Set the spawned state
                getOrAddTrait(CurrentLocation.class).setLocation(to);
                getOrAddTrait(Spawned.class).setSpawned(true);

                for (Trait trait : Iterables.toArray(traits.values(), Trait.class)) {
                    try {
                        trait.onSpawn();
                    } catch (Throwable ex) {
                        Bukkit.getLogger().info("An exception occurred while the trait " + trait.getName() + getEntityID());
                        ex.printStackTrace();
                    }
                }

                EntityType type = getEntity().getType();
                NMS.replaceTrackerEntry(getEntity());

                if (type.isAlive()) {
                    LivingEntity entity = (LivingEntity) getEntity();
                    entity.setRemoveWhenFarAway(false);

                    if (NMS.getStepHeight(entity) < 1) {
                        NMS.setStepHeight(entity, 1);
                    }

                    if (type == EntityType.PLAYER) {
                        PlayerUpdateTask.registerPlayer(getEntity());
                    } else if (getMetaData().has(NPC.Metadata.AGGRESSIVE)) {
                        NMS.setAggressive(entity, getMetaData().<Boolean>get(NPC.Metadata.AGGRESSIVE));
                    }

                    if (SUPPORT_NODAMAGE_TICKS && (Util.toTicks(Util.parseDuration("1s", null)) != 20
                            || getMetaData().has(NPC.Metadata.SPAWN_NODAMAGE_TICKS))) {
                        try {
                            entity.setNoDamageTicks(getMetaData().get(NPC.Metadata.SPAWN_NODAMAGE_TICKS,
                                    Util.toTicks(Util.parseDuration("1s", null))));
                        } catch (NoSuchMethodError err) {
                            SUPPORT_NODAMAGE_TICKS = false;
                        }
                    }
                }

                updateFlyableState();
                updateCustomNameVisibility();
                updateScoreboard();

                Bukkit.getLogger().info("Spawned " + this + " SpawnReason." + reason);
                cancel.run();
            }
        };
        if (getEntity() != null && getEntity().isValid()) {
            postSpawn.accept(() -> {
            });
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    postSpawn.accept(() -> cancel());
                }
            }.runTaskTimer(VitalCore.getPlugin(), 0, 1);
        }

        return true;
    }

    public void setEntityController(EntityController newController) {
        Preconditions.checkNotNull(newController);
        boolean wasSpawned = entityController == null ? false : isSpawned();
        Location prev = null;
        if (wasSpawned) {
            prev = getEntity().getLocation();
            despawn(DespawnReason.PENDING_RESPAWN);
        }
        PacketHandlerNPC packet = getTraitNullable(PacketHandlerNPC.class);
        if (packet != null) {
            newController = packet.wrap(newController);
        }
        entityController = newController;
        if (wasSpawned) {
            spawn(prev, SpawnReason.RESPAWN);
        }
    }

    private void updateCustomNameVisibility() {
        String nameplateVisible = getMetaData().<Object> get(NPC.Metadata.NAMEPLATE_VISIBLE, true).toString();
        if (requiresNameHologram()) {
            nameplateVisible = "false";
        }
        getEntity().setCustomNameVisible(Boolean.parseBoolean(nameplateVisible));
    }

    private void updateScoreboard() {
        if (getMetaData().has(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME)) {
            getOrAddTrait(ScoreboardTrait.class).update();
        }
    }

    public String getName() {
        return name;
    }

    public boolean despawn(DespawnReason reason) {
        if (getEntity() == null && reason != DespawnReason.DEATH) {
            Bukkit.getLogger().info("Tried to despawn " + this + " while already despawned, DespawnReason." + reason);
            if (reason == DespawnReason.RELOAD) {
                unloadEvents();
            }
            return true;
        }
        if (getEntity() instanceof Player) {
            PlayerUpdateTask.deregisterPlayer(getEntity());
        }
        if (reason == DespawnReason.RELOAD) {
            unloadEvents();
        }
        for (Trait trait : new ArrayList<Trait>(traits.values())) {
            trait.onDespawn(reason);
        }
        Bukkit.getLogger().info("Despawned " + this + " DespawnReason." + reason);

        if (reason == DespawnReason.DEATH) {
            entityController.die();
        } else {
            entityController.remove();
        }
        return true;
    }

    public org.bukkit.entity.Entity getEntity() {
        return entityController == null ? null : entityController.getBukkitEntity();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    protected void unloadEvents() {
        runnables.clear();
        for (Trait trait : traits.values()) {
            HandlerList.unregisterAll(trait);
        }
        traits.clear();
    }

    public boolean isFlyable() {
        updateFlyableState();
        return getMetaData().get(Metadata.FLYABLE, false);
    }

    public boolean isProtected() {
        return getMetaData().get(Metadata.DEFAULT_PROTECTED, true);
    }

    private void updateFlyableState() {
        org.bukkit.entity.EntityType type = isSpawned() ? getEntity().getType()
                : getOrAddTrait(MobTrait.class).getType();
        if (type == null)
            return;
        if (!Util.isAlwaysFlyable(type))
            return;
        if (!getMetaData().has(Metadata.FLYABLE)) {
            getMetaData().setPersistent(Metadata.FLYABLE, true);
        }
        if (!hasTrait(Gravity.class)) {
            getOrAddTrait(Gravity.class).setEnabled(true);
        }
    }

    public boolean isUpdating(NPCUpdate update) {
        return update == NPCUpdate.PACKET && updateCounter > Util.toTicks(Duration.ofSeconds(30));
    }

    public boolean isSpawned() {
        return getEntity() != null && (hasTrait(PacketHandlerNPC.class) || NMS.isValid(getEntity()));
    }

    public void addRunnable(Runnable runnable) {
        this.runnables.add(runnable);
    }

    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
    }

    protected Trait getTraitFor(Class<? extends Trait> clazz) {
        return Citizen.getTraitFactory().getTrait(clazz);
    }

    public void addTrait(Class<? extends Trait> clazz) {
        addTrait(getTraitFor(clazz));
    }

    public void addTrait(Trait trait) {
        if (trait == null) {
            return;
        }

        if (trait.getNPC() == null) {
            trait.linkToNPC(this);
        }

        Class<? extends Trait> clazz = trait.getClass();
        Trait replaced = traits.get(clazz);

        Bukkit.getPluginManager().registerEvents(trait, VitalCore.getPlugin());
        traits.put(clazz, trait);
        if (isSpawned()) {
            trait.onSpawn();
        }

        if (trait.isRunImplemented()) {
            if (replaced != null) {
                runnables.remove(replaced);
            }
            runnables.add(trait);
        }

        Bukkit.getPluginManager().callEvent(new NPCAddTraitEvent(this, trait));
    }

    public <T extends Trait> T getTraitNullable(Class<T> clazz) {
        return clazz.cast(traits.get(clazz));
    }

    public <T extends Trait> T getOrAddTrait(Class<T> clazz) {
        Trait trait = traits.get(clazz);
        if (trait == null) {
            trait = getTraitFor(clazz);
            addTrait(trait);
        }
        return clazz.cast(trait);
    }

    public boolean requiresNameHologram() {
        return getEntityType() != org.bukkit.entity.EntityType.ARMOR_STAND &&
                !getEntityType().name().equals("TEXT_DISPLAY")
                && ((getProfile().getName().length() > 16 && getEntityType() == org.bukkit.entity.EntityType.PLAYER)
                        || getMetaData().get(Metadata.ALWAYS_USE_NAME_HOLOGRAM, false));
    }

    protected org.bukkit.entity.EntityType getEntityType() {
        return isSpawned() ? getEntity().getType() : getOrAddTrait(MobTrait.class).getType();
    }

    public void update() {
        try {
            for (Runnable runnable : runnables) {
                runnable.run();
            }

            if (getMetaData().has(Metadata.ACTIVATION_RANGE)) {
                int range = getMetaData().get(Metadata.ACTIVATION_RANGE);
                if (range == -1 || Citizen.getLocationLookup().getNearbyPlayers(getLocation(), range)
                        .iterator().hasNext()) {
                    NMS.activate(getEntity());
                }
            }

            boolean isLiving = getEntity() instanceof LivingEntity;

            if (isUpdating(NPCUpdate.PACKET)) {
                updateCounter = 0;
            }

            if (isLiving) {
                NMS.setKnockbackResistance((LivingEntity) getEntity(), isProtected() ? 1D : 0D);
                if (SUPPORT_PICKUP_ITEMS) {
                    try {
                        ((LivingEntity) getEntity()).setCanPickupItems(getMetaData().get(Metadata.PICKUP_ITEMS, false));
                    } catch (Throwable t) {
                        SUPPORT_PICKUP_ITEMS = false;
                    }
                }

            }

            updateCounter++;
        } catch (Exception ex) {
            Throwable error = Throwables.getRootCause(ex);
            // Messaging.logTr(Messages.EXCEPTION_UPDATING_NPC, getId(),
            // error.getMessage());
            error.printStackTrace();
        }
    }

    public boolean isPushableByFluids() {
        return getMetaData().get(Metadata.FLUID_PUSHABLE, isProtected());
    }

    private void sendPacket(Player receiver, Packet<?> packet) {
        ((CraftPlayer) (receiver)).getHandle().connection.send(packet);
    }

    public void setTeamFeatures(Player receiver, TeamFeatures teamFeatures) {
        String teamName = this.profile.getName();
        Scoreboard scoreboard = new Scoreboard();
        PlayerTeam team = new PlayerTeam(scoreboard, teamName);

        team.setNameTagVisibility(teamFeatures.nameTagVisible() ? Team.Visibility.ALWAYS : Team.Visibility.NEVER);
        team.setColor(teamFeatures.glowColor().getNMSColor());
        team.setCollisionRule(teamFeatures.collision() ? Team.CollisionRule.ALWAYS : Team.CollisionRule.NEVER);

        ClientboundSetPlayerTeamPacket createPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team,
                true);
        ClientboundSetPlayerTeamPacket joinPacket = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, teamName,
                ClientboundSetPlayerTeamPacket.Action.ADD);

        sendPacket(receiver, createPacket);
        sendPacket(receiver, joinPacket);
    }

    public int getEntityID() {
        return entityID;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public Location getLocation() {
        return location;
    }

    public MetadataStore getMetaData() {
        return this.metadata;
    }

    public void setMetaData(MetadataStore metaData) {
        this.metadata = metaData;
    }

    public void setSkin(Property property) {
        this.profile.getProperties().put("textures", property);
    }

    public record TeamFeatures(boolean nameTagVisible, boolean collision, GlowColor glowColor) {
    }

    public enum NPCUpdate {
        PACKET;
    }

    private static boolean SUPPORT_PICKUP_ITEMS = true;
    private static boolean SUPPORT_NODAMAGE_TICKS = true;


    public enum Metadata {
        ACTIVATION_RANGE("activation-range", Integer.class),
        AGGRESSIVE("entity-aggressive", Boolean.class),
        ALWAYS_USE_NAME_HOLOGRAM("always-use-name-hologram", Boolean.class),
        AMBIENT_SOUND("ambient-sound", String.class),
        BOUNDING_BOX_FUNCTION("bounding-box-function", new TypeToken<Supplier<BoundingBox>>() {
        }),
        COLLIDABLE("collidable", Boolean.class),
        DAMAGE_OTHERS("damage-others", Boolean.class),
        DEATH_SOUND("death-sound", String.class),
        DEFAULT_PROTECTED("protected", Boolean.class),
        DISABLE_DEFAULT_STUCK_ACTION("disable-default-stuck-action", Boolean.class),
        DROPS_ITEMS("drops-items", Boolean.class),
        FLUID_PUSHABLE("fluid-pushable", Boolean.class),
        FLYABLE("flyable", Boolean.class),
        FORCE_PACKET_UPDATE("force-packet-update", Boolean.class),
        GLOWING("glowing", Boolean.class),
        HOLOGRAM_LINE_SUPPLIER("hologram-line-supplier", new TypeToken<Function<Player, String>>() {
        }),
        HURT_SOUND("hurt-sound", String.class),
        ITEM_AMOUNT("item-type-amount", Integer.class),
        ITEM_DATA("item-type-data", Byte.class),
        ITEM_ID("item-type-id", String.class),
        KEEP_CHUNK_LOADED("keep-chunk-loaded", Boolean.class),
        KNOCKBACK("knockback", Boolean.class),
        LEASH_PROTECTED("protected-leash", Boolean.class),
        MINECART_ITEM("minecart-item-name", String.class),
        MINECART_ITEM_DATA("minecart-item-data", Byte.class),
        MINECART_OFFSET("minecart-item-offset", Integer.class),
        NAMEPLATE_VISIBLE("nameplate-visible", Boolean.class),
        PACKET_UPDATE_DELAY("packet-update-delay", Integer.class),
        PATHFINDER_FALL_DISTANCE("pathfinder-fall-distance", Double.class),
        PATHFINDER_OPEN_DOORS("pathfinder-open-doors", Boolean.class),
        PICKUP_ITEMS("pickup-items", Boolean.class),
        REMOVE_FROM_PLAYERLIST("removefromplayerlist", Boolean.class),
        RESET_PITCH_ON_TICK("reset-pitch-on-tick", Boolean.class),
        RESET_YAW_ON_SPAWN("reset-yaw-on-spawn", Boolean.class),
        RESPAWN_DELAY("respawn-delay", Integer.class),
        SCOREBOARD_FAKE_TEAM_NAME("fake-scoreboard-team-name", String.class),
        SHOULD_SAVE("should-save", Boolean.class),
        SILENT("silent-sounds", Boolean.class),
        SNEAKING("citizens-sneaking", Boolean.class),
        SPAWN_NODAMAGE_TICKS("spawn-nodamage-ticks", Integer.class),
        SWIMMING("swim", Boolean.class),
        TARGETABLE("protected-target", Boolean.class),
        TRACKING_RANGE("tracking-distance", Integer.class),
        USE_MINECRAFT_AI("minecraft-ai", Boolean.class),
        USING_HELD_ITEM("using-held-item", Boolean.class),
        USING_OFFHAND_ITEM("using-offhand-item", Boolean.class),
        VILLAGER_BLOCK_TRADES("villager-trades", Boolean.class),
        WATER_SPEED_MODIFIER("water-speed-modifier", Double.class);

        private final String key;
        private final TypeToken<?> type;

        Metadata(String key, Class<?> type) {
            this(key, TypeToken.of(type));
        }

        Metadata(String key, TypeToken<?> type) {
            this.key = key;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public TypeToken<?> getType() {
            return type;
        }

        public static NPC.Metadata byKey(String name) {
            for (NPC.Metadata v : NPC.Metadata.values()) {
                if (v.key.equals(name))
                    return v;
            }
            return null;
        }

        public static NPC.Metadata byName(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }
    }

}
