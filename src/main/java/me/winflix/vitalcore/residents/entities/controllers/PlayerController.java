package me.winflix.vitalcore.residents.entities.controllers;

import com.mojang.authlib.GameProfile;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.residents.entities.PlayerEntityNPC;
import me.winflix.vitalcore.residents.interfaces.AbstractEntityController;
import me.winflix.vitalcore.residents.interfaces.NPC;
import me.winflix.vitalcore.residents.utils.nms.NMS;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerController extends AbstractEntityController {

    public PlayerController(){
    }

    @Override
    protected Entity createEntity(Location at, NPC npc) {
        final ServerLevel nmsWorld = ((CraftWorld) at.getWorld()).getHandle();
        String coloredName = npc.getName();
        String name = coloredName.length() > 16 ? coloredName.substring(0, 16) : coloredName;
        UUID uuid = npc.getUniqueId();
        String teamName = "RSD-" + uuid.toString().replace("-", "").substring(0, 12);

        if (npc.requiresNameHologram()) {
            name = teamName;
        }

        final GameProfile profile = new GameProfile(uuid, name);
        final PlayerEntityNPC handle = new PlayerEntityNPC(MinecraftServer.getServer(), nmsWorld, profile, npc);

        configureEntity(handle.getBukkitEntity(), npc);

        return handle.getBukkitEntity();
    }

    private void configureEntity(CraftPlayer entity, NPC npc) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(VitalCore.getPlugin(), () -> {
            if (getBukkitEntity() == null || !getBukkitEntity().isValid() || getBukkitEntity() != entity)
                return;
            boolean removeFromPlayerList = npc.getMetadata().get(NPC.Metadata.REMOVE_FROM_PLAYERLIST, true);
            NMS.addOrRemoveFromPlayerList(entity, removeFromPlayerList);
        }, 20);
        entity.setSleepingIgnored(true);
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) super.getBukkitEntity();
    }
}
