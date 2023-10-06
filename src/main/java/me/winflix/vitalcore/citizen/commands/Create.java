package me.winflix.vitalcore.npc.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.skins.utils.SkinGrabber;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Create extends SubCommand {
    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getVariants() {
        return "cr";
    }

    @Override
    public String getDescription() {
        return "Create a NPC";
    }

    @Override
    public String getPermission() {
        return "vitalcore.npc.bypass";
    }

    @Override
    public String getSyntax() {
        return "npc create <name>";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player player, String[] args) {

        CraftPlayer craftPlayer = (CraftPlayer) player;
        MinecraftServer minecraftServer = craftPlayer.getHandle().getServer();
        ServerLevel serverLevel = craftPlayer.getHandle().serverLevel();

        String npcName = "Survivor";

        if (args.length >= 2) {
            npcName = args[1];
        }

        GameProfile npcProfile = new GameProfile(UUID.randomUUID(), npcName);
        ServerPlayer npc = new ServerPlayer(minecraftServer, serverLevel, npcProfile);

        if (args.length >= 2) {
            Property propertySkin = SkinGrabber.fetchSkinByName(args[1]);

            if (propertySkin != null) {
                npc.getGameProfile().getProperties().put("textures", propertySkin);
            } else {
                Utils.errorMessage(player, "La skin de " + args[1] + " no fue encontrada, seteada skin por defecto.");
            }
        }

        Location loc = player.getLocation();
        npc.setPos(loc.getX(), loc.getY(), loc.getZ());

        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER);
        Collection<ServerPlayer> players = new ArrayList<>();
        players.add(npc);

        // Crear los paquetes y el ItemStack fuera del bucle
        ClientboundPlayerInfoUpdatePacket infoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(actions, players);
        ClientboundAddPlayerPacket addPlayerPacket = new ClientboundAddPlayerPacket(npc);

        // Obtener una lista de todos los jugadores en el servidor
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftOnlinePlayer = (CraftPlayer) onlinePlayer;
            ServerPlayer serverOnlinePlayer = craftOnlinePlayer.getHandle();

            ServerGamePacketListenerImpl packetListener = serverOnlinePlayer.connection;

            // Enviar la información del NPC a cada jugador en línea
            packetListener.send(infoUpdatePacket);
            packetListener.send(addPlayerPacket);
        }

        // Agregar el NPC a la lista de NPCs
        VitalCore.getNpcs().add(npc);
    }


}
