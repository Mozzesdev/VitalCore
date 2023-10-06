package me.winflix.vitalcore.npc.tasks;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.npc.Citizen;
import me.winflix.vitalcore.npc.managers.BodyManager;
import me.winflix.vitalcore.npc.models.Body;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class BodyRemoverTask extends BukkitRunnable {

    private final BodyManager bodyManger = Citizen.getBodyManger();

    public BodyRemoverTask() {
    }

    @Override
    public void run() {
        Iterator<Body> bodyIterator = bodyManger.getBodies().iterator();

        while (bodyIterator.hasNext()) {
            Body body = bodyIterator.next();

            long currentTime = System.currentTimeMillis();

            if ((currentTime - body.getWhenDied()) >= 20000) {
                bodyIterator.remove();

                new BukkitRunnable() {
                    @Override
                    public void run() {

                        Location location = body.getNpc().getBukkitEntity().getLocation();

                        Bukkit.getOnlinePlayers().forEach(player -> {
                            ServerGamePacketListenerImpl packetListener = ((CraftPlayer) player).getHandle().connection;
                            body.getNpc().setPos(location.getX(), location.getY() - 0.01, location.getZ());
                            packetListener.send(new ClientboundTeleportEntityPacket(body.getNpc()));
                        });

                        if (!location.add(0, 0.5, 0).getBlock().isPassable()){
                            bodyManger.removeBody(body);
                            Objects.requireNonNull(location.getWorld()).spawnParticle(Particle.SMOKE_NORMAL, location.subtract(1, 0,0), 50, 0.2f, 0.2f, 0.2f, 0.1);
                            this.cancel();
                        }

                    }
                }.runTaskTimerAsynchronously(VitalCore.getPlugin(), 0L, 5L);

                Player playerWhoDied = Bukkit.getPlayer(body.getWhoDied());

                if (playerWhoDied != null) {
                    Utils.errorMessage(playerWhoDied, "Your body was not reclaimed, all your stuffs was dropped on the floor.");

                    Location dropLocation = body.getNpc().getBukkitEntity().getLocation();
                    Arrays.stream(body.getInventoryItems()).forEach(itemStack -> {
                        Bukkit.getScheduler().runTask(VitalCore.getPlugin(), () -> {
                            Objects.requireNonNull(dropLocation.getWorld()).dropItem(dropLocation, itemStack);
                        });
                    });

                }

            }
        }
    }
}
