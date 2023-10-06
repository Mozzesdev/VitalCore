package me.winflix.vitalcore.citizen.managers;

import me.winflix.vitalcore.citizen.models.DeathBody;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.List;

public class BodyManager {

    private final List<DeathBody> bodies;

    public BodyManager() {
        this.bodies = new ArrayList<>();
    }

    public List<DeathBody> getBodies() {
        return bodies;
    }

    public void removeBody(DeathBody deathBody){
        Bukkit.getOnlinePlayers().forEach(player -> {
            ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
            ps.send(new ClientboundRemoveEntitiesPacket(deathBody.getNpc().getId()));
        });
    }
}
