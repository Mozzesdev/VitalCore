package me.winflix.vitalcore.skins.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;

import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.skins.models.Skin;
import me.winflix.vitalcore.skins.models.fetch.SkinFetch;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

public class SkinGrabber {

    private static final String MOJANG_PROFILE_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MOJANG_SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String UNSIGNED_PARAM = "?unsigned=false";

    public static Skin fetchSkinByUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            URI uri = URI.create(MOJANG_SESSION_API + uuid + UNSIGNED_PARAM);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                SkinFetch skinFetch = new SkinFetch(responseBody);
                Property property = new Property("textures", skinFetch.getValue(), skinFetch.getSignature());

                return new Skin(UUID.fromString(Utils.formatUUIDString(skinFetch.getId())), skinFetch.getName(),
                        property);
            }

            return null;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Skin fetchSkinByName(String name) {
        if (name == null) {
            return null;
        }
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            URI uri = URI.create(MOJANG_PROFILE_API + name);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(responseBody);

                    if (jsonNode.has("id")) {
                        String uuid = Utils.formatUUIDString(jsonNode.get("id").asText());

                        return fetchSkinByUUID(UUID.fromString(uuid));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return null;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Skin changeSkin(Player player, String identifier) {
        try {
            Skin skin = getSkin(identifier);
            if (skin == null)
                return null;

            return applySkin(player, skin);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método auxiliar para obtener la propiedad de la piel
    private static Skin getSkin(String identifier) {
        try {
            // Intentar como UUID primero
            UUID uuid = UUID.fromString(identifier);
            return fetchSkinByUUID(uuid);
        } catch (IllegalArgumentException e) {
            // Si falla, tratar como nombre de usuario
            return fetchSkinByName(identifier);
        }
    }

    // Método centralizado para aplicar la skin
    public static Skin applySkin(Player player, Skin skin) {
        if (skin == null || ((CraftPlayer) player).getHandle().hasDisconnected())
            return null;

        try {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            GameProfile profile = craftPlayer.getHandle().getGameProfile();

            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", skin.getProperty());

            refreshSkinForAll(player);
            refreshSelfSkin(player);
            return skin;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void refreshSkinForAll(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer targetEntity = craftPlayer.getHandle();
        UUID targetUUID = targetEntity.getUUID();
        int targetId = targetEntity.getId();

        List<SynchedEntityData.DataValue<?>> metadata = targetEntity.getEntityData().getNonDefaultValues();

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(targetUUID))
                continue;

            ServerPlayer viewerConnection = ((CraftPlayer) viewer).getHandle();

            sendPacket(viewerConnection, new ClientboundPlayerInfoRemovePacket(List.of(targetUUID)));
            sendPacket(viewerConnection,
                    ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(targetEntity)));
            sendPacket(viewerConnection, new ClientboundRemoveEntitiesPacket(targetId));

            ClientboundAddEntityPacket addPacket = new ClientboundAddEntityPacket(
                    targetEntity.getId(),
                    targetEntity.getUUID(),
                    targetEntity.getX(),
                    targetEntity.getY(),
                    targetEntity.getZ(),
                    targetEntity.getXRot(),
                    targetEntity.getYRot(),
                    EntityType.PLAYER,
                    0,
                    targetEntity.getDeltaMovement(),
                    targetEntity.getYHeadRot());
            sendPacket(viewerConnection, addPacket);
            sendPacket(viewerConnection, new ClientboundSetEntityDataPacket(targetId, metadata));
            sendPacket(viewerConnection, new ClientboundSetEquipmentPacket(
                    targetId,
                    List.of(
                            new Pair<>(EquipmentSlot.HEAD, targetEntity.getItemBySlot(EquipmentSlot.HEAD)),
                            new Pair<>(EquipmentSlot.CHEST, targetEntity.getItemBySlot(EquipmentSlot.CHEST)),
                            new Pair<>(EquipmentSlot.BODY, targetEntity.getItemBySlot(EquipmentSlot.BODY)),
                            new Pair<>(EquipmentSlot.FEET, targetEntity.getItemBySlot(EquipmentSlot.FEET)),
                            new Pair<>(EquipmentSlot.LEGS, targetEntity.getItemBySlot(EquipmentSlot.LEGS)),
                            new Pair<>(EquipmentSlot.MAINHAND, targetEntity.getItemBySlot(EquipmentSlot.MAINHAND)),
                            new Pair<>(EquipmentSlot.OFFHAND, targetEntity.getItemBySlot(EquipmentSlot.OFFHAND)))));
        }
    }

    public static void refreshSelfSkin(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer entityPlayer = craftPlayer.getHandle();
        ServerLevel world = entityPlayer.serverLevel();

        CommonPlayerSpawnInfo spawnInfo = entityPlayer.createCommonSpawnInfo(world);

        ClientboundRespawnPacket respawnPacket = new ClientboundRespawnPacket(spawnInfo,
                ClientboundRespawnPacket.KEEP_ALL_DATA);

        sendPacket(entityPlayer, new ClientboundPlayerInfoRemovePacket(List.of(entityPlayer.getGameProfile().getId())));
        sendPacket(entityPlayer, ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(entityPlayer)));
        sendPacket(entityPlayer, respawnPacket);

        entityPlayer.onUpdateAbilities();
        entityPlayer.connection.teleport(entityPlayer.getX(), entityPlayer.getY(), entityPlayer.getZ(),
                entityPlayer.getYRot(), entityPlayer.getXRot());

        entityPlayer.resetSentInfo();

        PlayerList playerList = entityPlayer.server.getPlayerList();
        playerList.sendPlayerPermissionLevel(entityPlayer);
        playerList.sendLevelInfo(entityPlayer, world);
        playerList.sendAllPlayerInfo(entityPlayer);

        for (MobEffectInstance effect : entityPlayer.getActiveEffects()) {
            sendPacket(entityPlayer, new ClientboundUpdateMobEffectPacket(entityPlayer.getId(), effect, false));
        }
    }

    private static void sendPacket(ServerPlayer player, Packet<?> packet) {
        player.connection.send(packet);
    }
}
