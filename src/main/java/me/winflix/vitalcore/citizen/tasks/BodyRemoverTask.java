package me.winflix.vitalcore.citizen.tasks;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.citizen.models.DeathBody;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.citizen.utils.managers.BodyManager;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BodyRemoverTask extends BukkitRunnable {

    private final BodyManager bodyManger = Citizen.getBodyManger();

    public BodyRemoverTask() {
    }

    @Override
    public void run() {
        Iterator<DeathBody> bodyIterator = bodyManger.getBodies().iterator();

        while (bodyIterator.hasNext()) {
            DeathBody deathBody = bodyIterator.next();

            long currentTime = System.currentTimeMillis();

            if ((currentTime - deathBody.getWhenDied()) >= 30000 || deathBody.getInventory().isEmpty()) {
                bodyIterator.remove();

                deathBody.getItemsTaken().keySet().forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        if (Objects.equals(player.getOpenInventory().getInventory(1), deathBody.getInventory())) {
                            Bukkit.getScheduler().runTask(VitalCore.getPlugin(), player::closeInventory);
                        }
                    }
                });

                new BukkitRunnable() {
                    @Override
                    public void run() {

                        Location location = deathBody.getNpc().getBukkitEntity().getLocation();

                        Bukkit.getOnlinePlayers().forEach(player -> {
                            ServerGamePacketListenerImpl packetListener = ((CraftPlayer) player).getHandle().connection;
                            deathBody.getNpc().setPos(location.getX(), location.getY() - 0.1, location.getZ());
                            packetListener.send(new ClientboundTeleportEntityPacket(deathBody.getNpc()));
                        });

                        if (!location.add(0, 0.5, 0).getBlock().isPassable()) {
                            bodyManger.removeBody(deathBody);
                            Objects.requireNonNull(location.getWorld())
                                    .spawnParticle(Particle.SMOKE_NORMAL, location.subtract(1, 0, 0), 50, 0.2f, 0.2f,
                                            0.2f, 0.1);
                            this.cancel();
                        }

                    }
                }.runTaskTimerAsynchronously(VitalCore.getPlugin(), 0L, 1L);

                Player playerWhoDied = Bukkit.getPlayer(deathBody.getWhoDied());

                Location dropLocation = deathBody.getNpc().getBukkitEntity().getLocation();
                List<ItemStack> itemsToDrop = new ArrayList<>(Arrays.asList(deathBody.getInventory().getStorageContents()));

                for (ItemStack itemStack : itemsToDrop) {
                    if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                        Bukkit.getScheduler().runTask(VitalCore.getPlugin(), () -> {
                            Objects.requireNonNull(dropLocation.getWorld()).dropItem(dropLocation, itemStack);
                        });
                    }
                }

                if (playerWhoDied != null && !deathBody.getInventory().isEmpty()) {
                    deathBody.getInventory().clear();
                    Utils.errorMessage(playerWhoDied,
                            "Your deathBody was not reclaimed, all your stuffs was dropped on the floor.");
                }
            }

        }
    }
}
