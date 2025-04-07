package me.winflix.vitalcore.residents.entities;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R3.CraftServer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.mojang.authlib.GameProfile;

import me.winflix.vitalcore.residents.Residents;
import me.winflix.vitalcore.residents.interfaces.NPC;
import me.winflix.vitalcore.residents.interfaces.NPCHolder;
import me.winflix.vitalcore.residents.models.ResidentNPC;
import me.winflix.vitalcore.residents.trait.traits.Gravity;
import me.winflix.vitalcore.residents.utils.network.EmptyNetHandler;
import me.winflix.vitalcore.residents.utils.network.EmptyNetManager;
import me.winflix.vitalcore.residents.utils.network.EmptySocket;
import me.winflix.vitalcore.residents.utils.nms.NMS;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class PlayerEntityNPC extends ServerPlayer implements NPCHolder {

    private final ResidentNPC npc;
    private boolean isEntitySetter;

    public PlayerEntityNPC(MinecraftServer server, ServerLevel level, GameProfile profile, NPC npc) {
        super(server, level, profile, ClientInformation.createDefault());
        this.npc = (ResidentNPC) npc;
        if (npc != null) {
            getGameMode().changeGameModeForPlayer(GameType.SURVIVAL);
            initialise(server);
        }
    }

    private void initialise(MinecraftServer server) {
        Socket socket = new EmptySocket();
        EmptyNetManager conn = null;

        try {
            conn = new EmptyNetManager(PacketFlow.CLIENTBOUND);
            connection = new EmptyNetHandler(server, conn, this);
            conn.setListenerForServerboundHandshake(connection);
            socket.close();
        } catch (IOException e) {
        }

        this.invulnerableTime = 0;

        NMS.setStepHeight(getBukkitEntity(), 1);

        byte flags = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        setSkinFlags(flags);
    }

    @Override
    public void die(DamageSource damagesource) {
        if (dead) {
            return;
        }
        super.die(damagesource);
        Bukkit.getScheduler().runTaskLater(Residents.getPlugin(), () -> {
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
        if (getBukkitEntity() != null
                && (!npc.hasTrait(Gravity.class) || npc.getOrAddTrait(Gravity.class).hasGravity())) {
            moveWithFallDamage(Vec3.ZERO);
        }
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

    @Override
    protected SoundEvent getDeathSound() {
        return NMS.getSoundEffect(npc, super.getDeathSound(), NPC.Metadata.DEATH_SOUND);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damagesource) {
        return NMS.getSoundEffect(npc, super.getHurtSound(damagesource), NPC.Metadata.HURT_SOUND);
    }

    private void setSkinFlags(byte flags) {
        getEntityData().set(EntityDataSerializers.BYTE.createAccessor(17), flags);
    }

    @Override
    public boolean isPushable() {
        return npc == null ? super.isPushable() : npc.getMetadata().<Boolean>get(NPC.Metadata.COLLIDABLE, false);
    }

    public NPC getNPC() {
        return npc;
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        if (npc != null && !isEntitySetter) {
            NMS.setBukkitEntity(this, new PlayerNPC(this));
            isEntitySetter = true;
        }
        return super.getBukkitEntity();
    }

    public ResidentNPC getNpc() {
        return npc;
    }

    public ServerPlayerGameMode getGameMode() {
        return gameMode;
    }

    public static class PlayerNPC extends CraftPlayer {
        private final ResidentNPC npc;

        private PlayerNPC(PlayerEntityNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public boolean canSee(org.bukkit.entity.Entity entity) {
            if (entity != null && entity.getType().name().contains("ITEM_FRAME")) {
                return false; // optimise for large maps in item frames
            }
            return super.canSee(entity);
        }

        @Override
        public PlayerEntityNPC getHandle() {
            return (PlayerEntityNPC) this.entity;
        }

        @Override
        public List<MetadataValue> getMetadata(String metadataKey) {
            return ((CraftServer) Bukkit.getServer()).getEntityMetadata().getMetadata(this, metadataKey);
        }

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

}
