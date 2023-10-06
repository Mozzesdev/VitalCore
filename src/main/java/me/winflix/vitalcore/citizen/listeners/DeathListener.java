package me.winflix.vitalcore.citizen.listeners;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;

import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.citizen.utils.managers.BodyManager;
import me.winflix.vitalcore.citizen.models.DeathBody;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DeathListener implements Listener {

    BodyManager bodyManger = Citizen.getBodyManger();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        if (!p.getInventory().isEmpty()) {
            DeathBody deathBody = spawnBody(p);
            bodyManger.getBodies().add(deathBody);
            e.getDrops().clear();
        }

    }

    private DeathBody spawnBody(Player p) {

        DeathBody deathBody = new DeathBody();
        deathBody.setWhoDied(p.getUniqueId());
        deathBody.setWhenDied(System.currentTimeMillis());
        Inventory bodyInventory = Bukkit.createInventory(null, InventoryType.CHEST,
                "Cuerpo de " + Bukkit.getOfflinePlayer(deathBody.getWhoDied()).getName());
        bodyInventory.setContents(
                Arrays.stream(p.getInventory().getContents()).filter(Objects::nonNull).toArray(ItemStack[]::new));
        deathBody.setInventory(bodyInventory);

        CraftPlayer craftPlayer = (CraftPlayer) p;
        MinecraftServer server = craftPlayer.getHandle().getServer();
        ServerLevel level = craftPlayer.getHandle().serverLevel();

        GameProfile bodyProfile = new GameProfile(UUID.randomUUID(), " ");
        GameProfile deadProfile = craftPlayer.getProfile();
        deadProfile.getProperties().get("textures").stream().findFirst()
                .ifPresent(bodySkin -> bodyProfile.getProperties().put("textures", bodySkin));

        ServerPlayer npc = new ServerPlayer(server, level, bodyProfile);
        deathBody.setNpc(npc);
        npc.setPose(Pose.SWIMMING);

        byte b = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        npc.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(17), b);

        Location groundPosition = p.getLocation().getBlock().getLocation();

        Location deathBodyLocation = groundPosition.add(1, 0, 0).subtract(0, .6, 0);
        npc.setPos(deathBodyLocation.getX(), deathBodyLocation.getY(), deathBodyLocation.getZ());
        deathBody.setLocation(deathBodyLocation);

        PlayerTeam team = new PlayerTeam(new Scoreboard(), npc.displayName.toLowerCase());
        team.getPlayers().add(bodyProfile.getName());
        team.setNameTagVisibility(Team.Visibility.NEVER);

        ItemStack helmet = p.getInventory().getHelmet();
        ItemStack chest = p.getInventory().getChestplate();
        ItemStack legs = p.getInventory().getLeggings();
        ItemStack boots = p.getInventory().getBoots();

        ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(npc.getId(),
                npc.getEntityData().getNonDefaultValues());

        ClientboundPlayerInfoUpdatePacket infoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc);

        ClientboundAddPlayerPacket addPlayerPacket = new ClientboundAddPlayerPacket(npc);

        ClientboundSetPlayerTeamPacket removeTeamPacket = ClientboundSetPlayerTeamPacket.createRemovePacket(team);

        ClientboundSetPlayerTeamPacket addOrModifyTeamPacket = ClientboundSetPlayerTeamPacket
                .createAddOrModifyPacket(team, true);

        Bukkit.getOnlinePlayers().forEach(player -> {
            ServerGamePacketListenerImpl packetListener = ((CraftPlayer) player).getHandle().connection;

            packetListener.send(infoUpdatePacket);
            packetListener.send(addPlayerPacket);
            packetListener.send(removeTeamPacket);
            packetListener.send(addOrModifyTeamPacket);
            packetListener.send(dataPacket);

            if (helmet != null) {
                ClientboundSetEquipmentPacket helmetPacket = new ClientboundSetEquipmentPacket(npc.getId(),
                        List.of(Pair.of(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(helmet))));
                packetListener.send(helmetPacket);
            }

            if (chest != null) {
                ClientboundSetEquipmentPacket chestPacket = new ClientboundSetEquipmentPacket(npc.getId(),
                        List.of(Pair.of(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(chest))));
                packetListener.send(chestPacket);
            }

            if (legs != null) {
                ClientboundSetEquipmentPacket legsPacket = new ClientboundSetEquipmentPacket(npc.getId(),
                        List.of(Pair.of(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(legs))));
                packetListener.send(legsPacket);
            }

            if (boots != null) {
                ClientboundSetEquipmentPacket bootsPacket = new ClientboundSetEquipmentPacket(npc.getId(),
                        List.of(Pair.of(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(boots))));
                packetListener.send(bootsPacket);
            }
        });

        return deathBody;
    }
}
