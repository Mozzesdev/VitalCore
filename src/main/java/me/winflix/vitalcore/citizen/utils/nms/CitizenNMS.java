package me.winflix.vitalcore.citizen.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import me.winflix.vitalcore.citizen.enums.Animation;
import me.winflix.vitalcore.citizen.enums.GameMode;
import me.winflix.vitalcore.citizen.enums.Ping;
import me.winflix.vitalcore.citizen.enums.PlayerInfo;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.core.interfaces.NMSBridge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.util.*;

public class CitizenNMS {

    private final NPC npc;

    public CitizenNMS(NPC npc) {
        this.npc = npc;
    }

    public ClientboundPlayerInfoUpdatePacket getUpdatePlayerInfoPacket(PlayerInfo playerInfo, Object obj) {
        return createDataSerializer(data -> {
            ClientboundPlayerInfoUpdatePacket.Action action = playerInfo.getNMSAction();

            UUID profileId = npc.getProfile().getId();
            GameProfile profile = npc.getProfile();
            RemoteChatSession.Data chatData = null;
            boolean listed = false;
            int pingMilliseconds = 0;
            GameType gameMode = null;
            Component displayName = null;

            switch (playerInfo) {
                case ADD_PLAYER -> {
                    listed = true;
                    pingMilliseconds = Ping.FIVE_BARS.getMilliseconds();
                    gameMode = GameMode.CREATIVE.getNMSGameMode();
                    displayName = CraftChatMessage.fromString(npc.getProfile().getName())[0];
                }
                case UPDATE_LATENCY -> pingMilliseconds = ((Ping) obj).getMilliseconds();
                case UPDATE_GAME_MODE -> gameMode = ((GameMode) obj).getNMSGameMode();
                case UPDATE_DISPLAY_NAME -> displayName = CraftChatMessage.fromString((String) obj)[0];
                case INITIALIZE_CHAT, UPDATE_LISTED ->
                        throw new IllegalArgumentException("PlayerInfo not supported: " + playerInfo);
            }

            ClientboundPlayerInfoUpdatePacket.Entry playerInfoData = new ClientboundPlayerInfoUpdatePacket.Entry(
                    profileId, profile,
                    listed, pingMilliseconds,
                    gameMode,
                    displayName,
                    chatData
            );

            List<ClientboundPlayerInfoUpdatePacket.Entry> list = List.of(playerInfoData);
            EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actionEnumSet = EnumSet.of(action);

            data.writeEnumSet(actionEnumSet, ClientboundPlayerInfoUpdatePacket.Action.class);
            data.writeCollection(list, (packet, clientbound) -> {
                packet.writeUUID(clientbound.profileId());
                actionEnumSet.forEach(act -> playerInfo.serialize(packet, clientbound));
            });

            return new ClientboundPlayerInfoUpdatePacket(data);
        });
    }


    public ClientboundSetPassengersPacket getEntityAttachPacket(int[] entityIDs) {
        return this.createDataSerializer(data -> {
            data.writeVarInt(this.npc.getEntityID());
            data.writeVarIntArray(entityIDs);
            return new ClientboundSetPassengersPacket(data);
        });
    }

    public ClientboundMoveEntityPacket.Rot getEntityLookPacket() {
        return new ClientboundMoveEntityPacket.Rot(this.npc.getEntityID(),
                (byte) ((int) (this.npc.getLocation().getYaw() * 256.0F / 360.0F)),
                (byte) ((int) (this.npc.getLocation().getPitch() * 256.0F / 360.0F)), true);
    }

    public ClientboundRotateHeadPacket getEntityHeadRotatePacket() {
        return this.createDataSerializer(data -> {
            data.writeVarInt(this.npc.getEntityID());
            data.writeByte((byte) ((int) (this.npc.getLocation().getYaw() * 256.0F / 360.0F)));
            return new ClientboundRotateHeadPacket(data);
        });
    }

    public ClientboundTeleportEntityPacket getEntityTeleportPacket(boolean onGround) {
        return this.createDataSerializer(data -> {
            data.writeVarInt(this.npc.getEntityID());
            data.writeDouble(this.npc.getLocation().getX());
            data.writeDouble(this.npc.getLocation().getY());
            data.writeDouble(this.npc.getLocation().getZ());
            data.writeByte((byte) ((int) (this.npc.getLocation().getYaw() * 256.0F / 360.0F)));
            data.writeByte((byte) ((int) (this.npc.getLocation().getPitch() * 256.0F / 360.0F)));
            data.writeBoolean(onGround);
            return new ClientboundTeleportEntityPacket(data);
        });
    }

    public static Entity getHandle(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity))
            return null;
        return ((CraftEntity) entity).getHandle();
    }

    public static boolean isValid(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle.valid && handle.isAlive();
    }

    public ClientboundMoveEntityPacket.Pos getEntityMovePacket(double x, double y, double z,
                                                               boolean onGround) {
        return new ClientboundMoveEntityPacket.Pos(this.npc.getEntityID(), (short) (x * 4096), (short) (y * 4096),
                (short) (z * 4096), onGround);
    }

    public ClientboundSetEntityLinkPacket getAttachEntityPacket(org.bukkit.entity.Entity entity) {
        return createDataSerializer(data -> {
            data.writeInt(entity.getEntityId());
            data.writeInt(this.npc.getEntityID());
            return new ClientboundSetEntityLinkPacket(data);
        });
    }

    public ClientboundSetEquipmentPacket getEntityEquipmentPacket(EquipmentSlot slot, ItemStack itemStack) {
        return new ClientboundSetEquipmentPacket(this.npc.getEntityID(),
                List.of(new Pair<>(slot, CraftItemStack.asNMSCopy(itemStack))));
    }

    public ClientboundAnimatePacket getEntityAnimationPacket(Animation animation) {
        return this.createDataSerializer((data) -> {
            data.writeVarInt(this.npc.getEntityID());
            data.writeByte((byte) animation.getType());
            return new ClientboundAnimatePacket(data);
        });
    }

    public ClientboundRemoveEntitiesPacket getEntityDestroyPacket() {
        return new ClientboundRemoveEntitiesPacket(this.npc.getEntityID());
    }

    public ClientboundSetEntityDataPacket getEntityMetadataPacket() {
        return createDataSerializer(data -> {

            data.writeVarInt(this.npc.getEntityID());
            this.npc.getMetaData().getList().forEach(dataItem -> dataItem.value().write(data));
            data.writeByte(255);

            return new ClientboundSetEntityDataPacket(data);
        });
    }

    public ClientboundAddPlayerPacket getEntitySpawnPacket() {
        return createDataSerializer(data -> {

            data.writeVarInt(this.npc.getEntityID());
            data.writeUUID(this.npc.getProfile().getId());
            data.writeDouble(this.npc.getLocation().getX());
            data.writeDouble(this.npc.getLocation().getY());
            data.writeDouble(this.npc.getLocation().getZ());
            data.writeByte((byte) (this.npc.getLocation().getYaw() * 256.0F / 360.0F));
            data.writeByte((byte) (this.npc.getLocation().getPitch() * 256.0F / 360.0F));

            return new ClientboundAddPlayerPacket(data);
        });
    }

    private <T> T createDataSerializer(NPC.UnsafeFunction<FriendlyByteBuf, T> callback) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        T result = null;
        try {
            result = callback.apply(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            data.release();
        }
        return result;
    }
}
