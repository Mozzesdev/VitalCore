package me.winflix.vitalcore.citizen.entities;

import com.mojang.authlib.GameProfile;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.citizen.interfaces.MobAI;
import me.winflix.vitalcore.citizen.interfaces.NPCHolder;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.citizen.trait.traits.Gravity;
import me.winflix.vitalcore.citizen.trait.traits.Inventory;
import me.winflix.vitalcore.citizen.utils.EmptyAdvancementDataPlayer;
import me.winflix.vitalcore.citizen.utils.EmptyServerStatsCounter;
import me.winflix.vitalcore.citizen.utils.Util;
import me.winflix.vitalcore.citizen.utils.network.EmptyNetworkManager;
import me.winflix.vitalcore.citizen.utils.network.EmptySocket;
import me.winflix.vitalcore.citizen.utils.network.EmptyNetHandler;
import me.winflix.vitalcore.core.nms.NMS;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class EntityPlayerNPC extends ServerPlayer implements NPCHolder, MobAI.ForwardingMobAI {
    private MobAI ai;
    private int jumpTicks = 0;
    private final NPC npc;
    private boolean setBukkitEntity;
    private EmptyServerStatsCounter statsCache;

    public EntityPlayerNPC(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile, NPC npc) {
        super(minecraftserver, worldserver, gameprofile);
        this.npc = npc;
        if (npc != null) {
            this.ai = new BasicMobAI(this);
            try {
                GAMEMODE_SETTING.invoke(gameMode, GameType.SURVIVAL, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            initialise(minecraftserver);
        }
    }

    @Override
    public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
        if (npc == null || !npc.isFlyable()) {
            return super.causeFallDamage(f, f1, damagesource);
        }
        return false;
    }

    @Override
    protected void checkFallDamage(double d0, boolean flag, BlockState blockData, BlockPos blockPosition) {
        if (npc == null || !npc.isFlyable()) {
            super.checkFallDamage(d0, flag, blockData, blockPosition);
        }
    }

    @Override
    public void die(DamageSource damagesource) {
        if (dead) {
            return;
        }
        super.die(damagesource);
        Bukkit.getScheduler().runTaskLater(VitalCore.getPlugin(), () -> {
            ((ServerLevel) level()).removePlayerImmediately(this, RemovalReason.KILLED);
            ((ServerLevel) level()).getChunkSource().removeEntity(this);
        }, 15);
    }

    @Override
    public void doTick() {
        if (npc == null) {
            super.doTick();
            return;
        }
        super.baseTick();
        boolean navigating = ai.getMoveControl().hasWanted();
        if (!navigating && getBukkitEntity() != null
                && (!npc.hasTrait(Gravity.class) || npc.getOrAddTrait(Gravity.class).hasGravity())
                && Util.isLoaded(getBukkitEntity().getLocation())
                && Util.checkYSafe(getY(), getBukkitEntity().getWorld())) {
            moveWithFallDamage(Vec3.ZERO);
        }
        Vec3 mot = getDeltaMovement();
        if (Math.abs(mot.x) < EPSILON && Math.abs(mot.y) < EPSILON && Math.abs(mot.z) < EPSILON) {
            setDeltaMovement(Vec3.ZERO);
        }
        if (navigating) {
            if (!ai.getNavigation().isDone()) {
                ai.getNavigation().tick();
            }
            moveOnCurrentHeading();
        }
        tickAI();
        detectEquipmentUpdates();
        noPhysics = isSpectator();
        if (isSpectator()) {
            this.onGround = false;
        }

        pushEntities();

        if (npc.getMetaData().has(NPC.Metadata.PICKUP_ITEMS)) {
            AABB axisalignedbb;
            if (this.isPassenger() && !this.getVehicle().isRemoved()) {
                axisalignedbb = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0, 0.0, 1.0);
            } else {
                axisalignedbb = this.getBoundingBox().inflate(1.0, 0.5, 1.0);
            }
            for (Entity entity : level().getEntities(this, axisalignedbb)) {
                entity.playerTouch(this);
            }
        }
    }

    @Override
    public MobAI getAI() {
        return null;
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        if (npc != null && !setBukkitEntity) {
            NMS.setBukkitEntity(this, new PlayerNPC(this));
            setBukkitEntity = true;
        }
        return super.getBukkitEntity();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NMS.getSoundEffect(npc, super.getDeathSound(), NPC.Metadata.DEATH_SOUND);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damagesource) {
        return NMS.getSoundEffect(npc, super.getHurtSound(damagesource), NPC.Metadata.HURT_SOUND);
    }

    @Override
    public NPC getNPC() {
        return npc;
    }

    @Override
    public ServerStatsCounter getStats() {
        return this.statsCache == null ? statsCache = new EmptyServerStatsCounter() : statsCache;
    }

    @Override
    public Component getTabListDisplayName() {
//        if (Setting.DISABLE_TABLIST.asBoolean()) {
//            return MutableComponent.create(new LiteralContents(""));
//        }
        return super.getTabListDisplayName();
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        // knock back velocity is cancelled and sent to client for handling when
        // the entity is a player. there is no client so make this happen
        // manually.
        boolean damaged = super.hurt(damagesource, f);
        if (damaged && hurtMarked) {
            hurtMarked = false;
            Bukkit.getScheduler().runTask(VitalCore.getPlugin(), () -> hurtMarked = true);
        }
        return damaged;
    }

    @Override
    public boolean isInWall() {
        if (npc == null || noPhysics || isSleeping()) {
            return super.isInWall();
        }
        return Util.inBlock(getBukkitEntity());
    }

    @Override
    public boolean isPushable() {
        return npc == null ? super.isPushable() : npc.getMetaData().has(NPC.Metadata.COLLIDABLE);
    }

    @Override
    public void knockback(double strength, double dx, double dz) {
        NMS.callKnockbackEvent(npc, (float) strength, dx, dz, (evt) -> super.knockback((float) evt.getStrength(),
                evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
    }

    private void moveWithFallDamage(Vec3 vec) {
        double x = getX();
        double y = getY();
        double z = getZ();
        travel(vec);
        if (!npc.isProtected()) {
            doCheckFallDamage(getX() - x, getY() - y, getZ() - z, onGround);
        }
    }

    private void moveOnCurrentHeading() {
        if (jumping) {
            if (onGround && jumpTicks == 0) {
                jumpFromGround();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }
        xxa *= 0.98F;
        zza *= 0.98F;
        moveWithFallDamage(new Vec3(this.xxa, this.yya, this.zza));
        NMS.setHeadYaw(getBukkitEntity(), getYRot());
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    @Override
    public boolean onClimbable() {
        if (npc == null || !npc.isFlyable()) {
            return super.onClimbable();
        } else {
            return false;
        }
    }

    @Override
    public void push(double x, double y, double z) {
        Vector vector = Util.callPushEvent(npc, x, y, z);
        if (vector != null) {
            super.push(vector.getX(), vector.getY(), vector.getZ());
        }
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
        if (npc != null) {
            Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        getAdvancements().save();
    }

    @Override
    public void tick() {
        super.tick();
        if (npc == null)
            return;
        Bukkit.getServer().getPluginManager().unsubscribeFromPermission("bukkit.broadcast.user", getBukkitEntity());
//        updatePackets(npc.getNavigator().isNavigating());
        npc.update();
    }

    private void updatePackets(boolean navigating) {
        if (!npc.isUpdating(NPC.NPCUpdate.PACKET))
            return;

        effectsDirty = true;
    }

    @Override
    public void travel(Vec3 vec3d) {
        if (npc == null || !npc.isFlyable()) {
            super.travel(vec3d);
        } else {
            NMS.flyingMoveLogic(this, vec3d);
        }
    }

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagkey, double d0) {
        Vec3 old = getDeltaMovement().add(0, 0, 0);
        boolean res = super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
        if (!npc.isPushableByFluids()) {
            setDeltaMovement(old);
        }
        return res;
    }

    private void initialise(MinecraftServer minecraftServer) {
        Socket socket = new EmptySocket();
        EmptyNetworkManager conn = null;
        try {
            conn = new EmptyNetworkManager(PacketFlow.CLIENTBOUND);
            connection = new EmptyNetHandler(minecraftServer, conn, this);
            conn.setListener(connection);
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.invulnerableTime = 0;
        NMS.setStepHeight(getBukkitEntity(), 1); // the default (0) breaks step climbing
        setSkinFlags((byte) 0xFF);
        EmptyAdvancementDataPlayer.clear(this.getAdvancements());
        NMS.setAdvancement(this.getBukkitEntity(),
                new EmptyAdvancementDataPlayer(minecraftServer.getFixerUpper(), minecraftServer.getPlayerList(),
                        minecraftServer.getAdvancements(), VitalCore.getPlugin().getDataFolder().getParentFile(),
                        this));
    }

    public void setSkinFlags(byte flags) {
        getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, flags);
    }

    @Override
    public boolean alwaysAccepts() {
        return super.alwaysAccepts();
    }

    @Override
    public float getPathfindingMalus(BlockPathTypes var1) {
        return ForwardingMobAI.super.getPathfindingMalus(var1);
    }

    @Override
    public void setPathfindingMalus(BlockPathTypes water, float oldWaterCost) {
        ForwardingMobAI.super.setPathfindingMalus(water, oldWaterCost);
    }

    @Override
    public void tickAI() {
        ForwardingMobAI.super.tickAI();
    }

    @Override
    public void updatePathfindingRange(float range) {
        ForwardingMobAI.super.updatePathfindingRange(range);
    }


    @Override
    public JumpControl getJumpControl() {
        return ForwardingMobAI.super.getJumpControl();
    }

    @Override
    public Map<BlockPathTypes, Float> getMalus() {
        return ForwardingMobAI.super.getMalus();
    }

    @Override
    public MoveControl getMoveControl() {
        return ForwardingMobAI.super.getMoveControl();
    }

    @Override
    public PathNavigation getNavigation() {
        return ForwardingMobAI.super.getNavigation();
    }
    public static class PlayerNPC extends CraftPlayer implements NPCHolder {
        private final NPC npc;

        private PlayerNPC(EntityPlayerNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
            npc.getOrAddTrait(Inventory.class);
        }

        @Override
        public boolean canSee(org.bukkit.entity.Entity entity) {
            if (entity != null && entity.getType().name().contains("ITEM_FRAME")) {
                return false; // optimise for large maps in item frames
            }
            return super.canSee(entity);
        }

        @Override
        public EntityPlayerNPC getHandle() {
            return (EntityPlayerNPC) this.entity;
        }

        @Override
        public List<MetadataValue> getMetadata(String metadataKey) {
            return ((CraftServer) Bukkit.getServer()).getEntityMetadata().getMetadata(this, metadataKey);
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean hasMetadata(String metadataKey) {
            return ((CraftServer) Bukkit.getServer()).getEntityMetadata().hasMetadata(this, metadataKey);
        }

        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {
            ((CraftServer) Bukkit.getServer()).getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
        }

        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
            ((CraftServer) Bukkit.getServer()).getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
        }
    }

    private static final float EPSILON = 0.003F;
    private static final MethodHandle GAMEMODE_SETTING = NMS.getFirstMethodHandle(ServerPlayerGameMode.class, true,
            GameType.class, GameType.class);
}
