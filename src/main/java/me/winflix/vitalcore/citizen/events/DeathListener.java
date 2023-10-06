package me.winflix.vitalcore.npc.events;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.npc.Citizen;
import me.winflix.vitalcore.npc.managers.BodyManager;
import me.winflix.vitalcore.npc.models.Body;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class DeathListener implements Listener {

    BodyManager bodyManger = Citizen.getBodyManger();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

       if(p.getInventory().getContents().length > 0){
           Body body = spawnBody(p);
           bodyManger.getBodies().add(body);
           e.getDrops().clear();
       }

    }

    @EventHandler
    public void onBodyRightClick(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand armorStand) {
            for (Body body : bodyManger.getBodies()) {
                if (body.getArmorStandList().contains(armorStand)) {
                    Player whoClicked = e.getPlayer();
                    Inventory bodyInventory = Bukkit.createInventory(null, InventoryType.CHEST, "Cuerpo de " + Bukkit.getOfflinePlayer(body.getWhoDied()));

                    ItemStack[] bodyItems = body.getInventoryItems();
                    bodyInventory.setContents(bodyItems);

                    whoClicked.openInventory(bodyInventory);

                    final BodyInventoryListener bodyInventoryListener = new BodyInventoryListener(whoClicked, body, bodyInventory);
                    Bukkit.getPluginManager().registerEvents(bodyInventoryListener, VitalCore.getPlugin());
                }
            }
        }
    }

    private Body spawnBody(Player p) {

        Body body = new Body();
        body.setWhoDied(p.getUniqueId());
        body.setWhenDied(System.currentTimeMillis());
        body.setInventoryItems(Arrays.stream(p.getInventory().getContents()).filter(Objects::nonNull).toArray(ItemStack[]::new));

        CraftPlayer craftPlayer = (CraftPlayer) p;
        MinecraftServer server = craftPlayer.getHandle().getServer();
        ServerLevel level = craftPlayer.getHandle().serverLevel();

        GameProfile bodyProfile = new GameProfile(UUID.randomUUID(), " ");
        GameProfile deadProfile = craftPlayer.getProfile();
        Property bodySkin = (Property) deadProfile.getProperties().get("textures").toArray()[0];

        if (bodySkin != null) {
            bodyProfile.getProperties().put("textures", bodySkin);
        }

        ServerPlayer npc = new ServerPlayer(server, level, bodyProfile);
        body.setNpc(npc);

        npc.setPose(Pose.SLEEPING);

        Location groundPosition = p.getLocation().getBlock().getLocation().clone();
        while (groundPosition.getBlock().getType() == Material.AIR) {
            groundPosition = groundPosition.subtract(0, 1, 0);
        }

        npc.setPos(p.getLocation().getX() + 1, groundPosition.getY() + 1, p.getLocation().getZ());

        PlayerTeam team = new PlayerTeam(new Scoreboard(), npc.displayName.toLowerCase());
        team.getPlayers().add(bodyProfile.getName());
        team.setNameTagVisibility(Team.Visibility.NEVER);

        ArmorStand armorStand1 = (ArmorStand) p.getWorld().spawnEntity(npc.getBukkitEntity().getLocation().subtract(.3, .5, 0), EntityType.ARMOR_STAND);
        armorStand1.setInvisible(true);
        armorStand1.setInvulnerable(true);
        armorStand1.setGravity(false);
        armorStand1.setSmall(true);

        ArmorStand armorStand2 = (ArmorStand) p.getWorld().spawnEntity(npc.getBukkitEntity().getLocation().subtract(1, .5, 0), EntityType.ARMOR_STAND);
        armorStand2.setInvisible(true);
        armorStand2.setInvulnerable(true);
        armorStand2.setGravity(false);
        armorStand2.setSmall(true);

        ArmorStand armorStand3 = (ArmorStand) p.getWorld().spawnEntity(npc.getBukkitEntity().getLocation().subtract(1.7, .5, 0), EntityType.ARMOR_STAND);
        armorStand3.setInvisible(true);
        armorStand3.setInvulnerable(true);
        armorStand3.setGravity(false);
        armorStand3.setSmall(true);
        body.getArmorStandList().add(armorStand1);
        body.getArmorStandList().add(armorStand2);
        body.getArmorStandList().add(armorStand3);

        Bukkit.getOnlinePlayers().forEach(player -> {
            ServerGamePacketListenerImpl packetListener = ((CraftPlayer) player).getHandle().connection;
            packetListener.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc));
            packetListener.send(new ClientboundAddPlayerPacket(npc));
            packetListener.send(new ClientboundSetEntityDataPacket(npc.getId(), npc.getEntityData().getNonDefaultValues()));

            packetListener.send(ClientboundSetPlayerTeamPacket.createRemovePacket(team));
            packetListener.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
        });

        return body;

    }
}
