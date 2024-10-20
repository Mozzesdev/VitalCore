package me.winflix.vitalcore.skins.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.skins.models.fetch.SkinFetch;
import me.winflix.vitalcore.skins.models.Skin;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class SkinGrabber {

    private static final String MOJANG_PROFILE_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MOJANG_SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String UNSIGNED_PARAM = "?unsigned=false";

    public static void changeSkinByPlayer(Player p) {
        CraftPlayer craftPlayer = (CraftPlayer) p;
        ServerPlayer serverPlayer = craftPlayer.getHandle();
        GameProfile playerProfile = craftPlayer.getProfile();
        ServerGamePacketListenerImpl listener = serverPlayer.connection;

        Property skinProperty = fetchSkinByName(p.getDisplayName());

        if(skinProperty == null){
            return;
        }

        playerProfile.getProperties().removeAll("textures");
        playerProfile.getProperties().put("textures", skinProperty);

        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, serverPlayer);

        listener.send(packet);
    }
    
    public static void changeSkinBySkin(Skin skin) {

    }

    public static Property fetchSkinByUUID(UUID uuid) {
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

                return new Property("textures", skinFetch.getValue(), skinFetch.getSignature());
            }

            return null;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Property fetchSkinByName(String name) {
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

}
