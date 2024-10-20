package me.winflix.vitalcore.residents.models;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftParrot;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;

public class AvoidNPC {
    private static AtomicInteger atomicInteger;
    private final int entityID;
    private final Location location;
    private GameProfile profile;
    // private MetaData metadata = new MetaData();

    // private final CitizenNMS nms;

    static {
        try {
            Field field = Entity.class.getDeclaredField("d");
            field.setAccessible(true);
            atomicInteger = (AtomicInteger) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Error when accessing the field 'd' of the Entity class: " + e.getMessage());
        }
    }

    public AvoidNPC(UUID uuid, Location location, String displayName) {
        this.entityID = atomicInteger.incrementAndGet();
        this.profile = new GameProfile(uuid, displayName);
        this.location = location;
        // this.nms = new CitizenNMS(this);
    }

    public AvoidNPC(Location location, String displayName) {
        this(UUID.randomUUID(), location, displayName);
    }

    public void spawnNPC(Collection<? extends Player> receivers) {
        receivers.forEach(this::spawnNPC);
    }

    public void spawnNPC(Player receiver) {
        this.addToTabList(receiver);
        // this.sendPacket(receiver, this.nms.getEntitySpawnPacket());
        this.updateMetadata(receiver);
    }

    public void destroyNPC(Collection<? extends Player> receivers) {
        receivers.forEach(this::destroyNPC);
    }

    public void destroyNPC(Player receiver) {
        // this.sendPacket(receiver, this.nms.getEntityDestroyPacket());
    }

    public void reloadNPC(Collection<? extends Player> receivers) {
        receivers.forEach(this::reloadNPC);
    }

    public void reloadNPC(Player receiver) {
        this.destroyNPC(receiver);
        this.spawnNPC(receiver);
    }

    public void teleportNPC(Collection<? extends Player> receivers, Location location, boolean onGround) {
        receivers.forEach(receiver -> this.teleportNPC(receiver, location, onGround));
    }

    public void teleportNPC(Player receiver, Location location, boolean onGround) {
        this.location.setX(location.getX());
        this.location.setY(location.getY());
        this.location.setZ(location.getZ());
        this.location.setPitch(location.getPitch());
        this.location.setYaw(location.getYaw());
        this.rotateHead(receiver, location.getPitch(), location.getYaw(), true);
        // this.sendPacket(receiver, this.nms.getEntityTeleportPacket(onGround));
    }

    public void updateMetadata(Collection<? extends Player> receivers) {
        receivers.forEach(this::updateMetadata);
    }

    public void updateMetadata(Player receiver) {
        // this.sendPacket(receiver, this.nms.getEntityMetadataPacket());
    }

    public void updateGameMode(Collection<? extends Player> receivers) {
        receivers.forEach(this::updateGameMode);
    }

    public void updateGameMode(Player receiver) {
        // this.sendPacket(receiver,
        // this.nms.getUpdatePlayerInfoPacket(PlayerInfo.UPDATE_GAME_MODE, null));
    }

    // public void setPing(Collection<? extends Player> receivers, Ping ping) {
    // receivers.forEach(receiver -> this.setPing(receiver, ping));
    // }

    // public void setPing(Player receiver, Ping ping) {
    // this.sendPacket(receiver,
    // this.nms.getUpdatePlayerInfoPacket(PlayerInfo.UPDATE_LATENCY, ping));
    // }

    public void setGameMode(Collection<? extends Player> receivers, GameMode gameMode) {
        receivers.forEach(receiver -> this.setGameMode(receiver, gameMode));
    }

    public void setGameMode(Player receiver, GameMode gameMode) {
    // this.sendPacket(receiver,
    // this.nms.getUpdatePlayerInfoPacket(PlayerInfo.UPDATE_GAME_MODE, gameMode));
    }

    public void setTabListName(Collection<? extends Player> receivers, String displayName) {
        receivers.forEach(receiver -> this.setTabListName(receiver, displayName));
    }

    public void setTabListName(Player receiver, String displayName) {
        // this.sendPacket(receiver,
        // this.nms.getUpdatePlayerInfoPacket(PlayerInfo.UPDATE_DISPLAY_NAME,
        // displayName));
    }

    public void addToTabList(Collection<? extends Player> receivers) {
        receivers.forEach(this::addToTabList);
    }

    public void addToTabList(Player receiver) {
        // this.sendPacket(receiver,
        // this.nms.getUpdatePlayerInfoPacket(PlayerInfo.ADD_PLAYER, null));
    }

    // public void playAnimation(Collection<? extends Player> receivers, Animation
    // animation) {
    // receivers.forEach(receiver -> this.playAnimation(receiver, animation));
    // }

    // public void playAnimation(Player receiver, Animation animation) {
    // this.sendPacket(receiver, this.nms.getEntityAnimationPacket(animation));
    // }

    public void lookAtPlayer(Collection<? extends Player> receivers, Player target) {
        receivers.forEach(receiver -> this.lookAtPlayer(receiver, target));
    }

    public void lookAtPlayer(Player receiver, Player target) {
        this.lookAtPoint(receiver, target.getEyeLocation());
    }

    public void lookAtPoint(Collection<? extends Player> receivers, Location location) {
        receivers.forEach(receiver -> this.lookAtPoint(receiver, location));
    }

    public void lookAtPoint(Player receiver, Location location) {
        Location eyeLocation = getEyeLocation();
        double deltaX = location.getX() - eyeLocation.getX();
        double deltaY = location.getY() - eyeLocation.getY();
        double deltaZ = location.getZ() - eyeLocation.getZ();

        double yaw = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        yaw = (yaw + 360) % 360;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double pitch = Math.toDegrees(Math.atan2(-deltaY, horizontalDistance)) - 90;
        pitch = (pitch + 360) % 360;

        rotateHead(receiver, (float) pitch, (float) yaw, true);
    }

    public void rotateHead(Collection<? extends Player> receivers, float pitch, float yaw, boolean body) {
        receivers.forEach(receiver -> this.rotateHead(receiver, pitch, yaw, body));
    }

    public void rotateHead(Player receiver, float pitch, float yaw, boolean body) {
        this.location.setPitch(pitch);
        this.location.setYaw(yaw);

        // if (body) this.sendPacket(receiver, this.nms.getEntityLookPacket());

        // this.sendPacket(receiver, this.nms.getEntityHeadRotatePacket());
    }

    // public void setEquipment(Collection<? extends Player> receivers, ItemSlot slot,
    //         org.bukkit.inventory.ItemStack itemStack) {
    //     receivers.forEach(receiver -> this.setEquipment(receiver, slot, itemStack));
    // }

    // public void setEquipment(Player receiver, ItemSlot slot, ItemStack itemStack) {
    //     this.sendPacket(receiver,
    //             this.nms.getEntityEquipmentPacket(slot.getNMSItemSlot(), itemStack));
    // }

    public void setPassenger(Collection<? extends Player> receivers, int... entityIDs) {
        receivers.forEach(receiver -> this.setPassenger(receiver, entityIDs));
    }

    public void setPassenger(Player receiver, int... entityIDs) {
        // this.sendPacket(receiver, this.nms.getEntityAttachPacket(entityIDs));
    }

    public void setLeash(Player receiver, org.bukkit.entity.Entity entity) {
        // this.sendPacket(receiver, this.nms.getAttachEntityPacket(entity));
    }

    public void setLeash(Collection<? extends Player> receivers, org.bukkit.entity.Entity entity) {
        receivers.forEach(p -> this.setLeash(p, entity));
    }

    public void moveRelative(Player receiver, double relX, double relY, double relZ, boolean onGround) {
        // this.sendPacket(receiver, this.nms.getEntityMovePacket(relX, relY, relZ, onGround));
    }

    public void moveRelative(Collection<? extends Player> receivers, double relX, double relY, double relZ,
            boolean onGround) {
        receivers.forEach(p -> this.moveRelative(p, relX, relY, relZ, onGround));
    }

    public void setSkinByUsername(String username) {
        // setSkin(SkinGrabber.fetchSkinByName(username));
    }

    public void setSkinByUUID(UUID uuid) {
        // setSkin(SkinGrabber.fetchSkinByUUID(uuid));
    }

    private void sendPacket(Player receiver, Packet<?> packet) {
        ((CraftPlayer) (receiver)).getHandle().connection.send(packet);
    }

    // public void setTeamFeatures(Player receiver, TeamFeatures teamFeatures) {
    //     String teamName = this.profile.getName();
    //     Scoreboard scoreboard = new Scoreboard();
    //     PlayerTeam team = new PlayerTeam(scoreboard, teamName);

    //     // team.setNameTagVisibility(teamFeatures.nameTagVisible() ? Team.Visibility.ALWAYS : Team.Visibility.NEVER);
    //     // team.setColor(teamFeatures.glowColor().getNMSColor());
    //     // team.setCollisionRule(teamFeatures.collision() ? Team.CollisionRule.ALWAYS : Team.CollisionRule.NEVER);

    //     ClientboundSetPlayerTeamPacket createPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team,
    //             true);
    //     ClientboundSetPlayerTeamPacket joinPacket = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, teamName,
    //             ClientboundSetPlayerTeamPacket.Action.ADD);

    //     sendPacket(receiver, createPacket);
    //     sendPacket(receiver, joinPacket);
    // }

    public CompoundTag createParrot(Consumer<CraftParrot> callback, World world) {
        Parrot entityParrot = new Parrot(EntityType.PARROT, ((CraftWorld) world).getHandle());
        CraftParrot parrot = new CraftParrot((CraftServer) Bukkit.getServer(), entityParrot);

        callback.accept(parrot);

        CompoundTag compoundTag = new CompoundTag();
        entityParrot.addAdditionalSaveData(compoundTag);

        return compoundTag;
    }

    // public void setParrotLeftShoulder(Consumer<CraftParrot> callback, World world) {
    //     this.metadata.setLeftShoulder(this.createParrot(callback, world));
    // }

    // public void setParrotRightShoulder(Consumer<CraftParrot> callback, World world) {
    //     this.metadata.setRightShoulder(this.createParrot(callback, world));
    // }

    public int getEntityID() {
        return entityID;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public Location getLocation() {
        return location;
    }

    public Location getEyeLocation() {
        return this.location.clone().add(0, EntityType.PLAYER.getDimensions().width() * 0.85F, 0);
    }

    // public MetaData getMetaData() {
    //     return this.metadata;
    // }

    // public void setMetaData(MetaData metaData) {
    //     this.metadata = metaData;
    // }

    public void setSkin(Property property) {
        this.profile.getProperties().put("textures", property);
    }

    public void setDisplayName(String displayName) {
        GameProfile swapProfile = new GameProfile(this.profile.getId(), displayName);
        swapProfile.getProperties().putAll(this.profile.getProperties());
        this.profile = swapProfile;
    }

    // public void registerListener(NPCListener listener) {
    //     if (EventManager.isInitialized()) {
    //         EventManager.getINSTANCE().listenNPC(this, listener);
    //     }
    // }

    // public void unregisterListener() {
    //     if (EventManager.isInitialized()) {
    //         EventManager.getINSTANCE().unlistenNPC(this);
    //     }
    // }

    // public static void initEventHandler(Plugin plugin) {
    //     EventManager.init(plugin);
    // }

    // public record TeamFeatures(boolean nameTagVisible, boolean collision, GlowColor glowColor) {
    // }

    // public record NPCInteractEvent(Player player, InteractType interactType, Hand hand, boolean sneaking) {
    // }

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    // @FunctionalInterface
    // public interface NPCListener extends Consumer<NPC.NPCInteractEvent> {
    // }

    @FunctionalInterface
    public interface UnsafeRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface UnsafeFunction<K, T> {
        T apply(K k) throws Exception;
    }

}
