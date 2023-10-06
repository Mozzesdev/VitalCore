package me.winflix.vitalcore.npc.events;

import me.winflix.vitalcore.VitalCore;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        VitalCore.getNpcs().stream()
                .forEach(npc -> {
                    Location location = npc.getBukkitEntity().getLocation();
                    location.setDirection(e.getPlayer().getLocation().subtract(location).toVector());
                    float yaw = location.getYaw();
                    float pitch = location.getPitch();

                    ServerGamePacketListenerImpl serverConnection = ((CraftPlayer) e.getPlayer()).getHandle().connection;
                    ClientboundRotateHeadPacket rotateHeadPacket = new ClientboundRotateHeadPacket(npc, (byte) ((yaw % 360) * 256 / 360));
                    ClientboundMoveEntityPacket moveEntityPacket = new ClientboundMoveEntityPacket.Rot(npc.getBukkitEntity().getEntityId(), (byte) ((yaw % 360) * 256 / 360), (byte) ((pitch % 360) * 256 / 360), false);

                    serverConnection.send(rotateHeadPacket);
                    serverConnection.send(moveEntityPacket);
                });

    }

}
