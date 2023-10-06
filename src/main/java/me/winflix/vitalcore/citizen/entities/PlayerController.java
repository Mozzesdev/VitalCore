package me.winflix.vitalcore.citizen.entities;

import com.mojang.authlib.GameProfile;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.citizen.interfaces.EntityController;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.citizen.utils.Util;
import me.winflix.vitalcore.core.nms.NMS;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.UUID;

public class PlayerController implements EntityController {
    private Entity bukkitEntity;

    public PlayerController(){
    }

    @Override
    public void create(Location at, NPC npc) {
        bukkitEntity = createEntity(at, npc);
    }

    protected Entity createEntity(Location at, NPC npc){
        final ServerLevel nmsWorld = ((CraftWorld) at.getWorld()).getHandle();
        String coloredName = npc.getProfile().getName();
        String name = coloredName.length() > 16 ? coloredName.substring(0, 16) : coloredName;
        UUID uuid = npc.getProfile().getId();
        String teamName = Util.getTeamName(uuid);

        if (npc.requiresNameHologram()) {
            name = teamName;
        }

        final GameProfile profile = npc.getProfile();
        final EntityPlayerNPC handle = new EntityPlayerNPC(MinecraftServer.getServer(), nmsWorld, profile, npc);

        Bukkit.getScheduler().scheduleSyncDelayedTask(VitalCore.getPlugin(), () -> {
            if (getBukkitEntity() == null || !getBukkitEntity().isValid()
                    || getBukkitEntity() != handle.getBukkitEntity())
                return;
            boolean removeFromPlayerList = npc.getMetaData().get(NPC.Metadata.REMOVE_FROM_PLAYERLIST, true);
            NMS.addOrRemoveFromPlayerList(getBukkitEntity(), removeFromPlayerList);
        }, 20);
        handle.getBukkitEntity().setSleepingIgnored(true);
        return handle.getBukkitEntity();
    };

    @Override
    public void die() {
        bukkitEntity = null;
    }

    @Override
    public Entity getBukkitEntity() {
        return bukkitEntity;
    }

    @Override
    public void remove() {
        if (bukkitEntity == null)
            return;
        if (bukkitEntity instanceof Player) {
            NMS.removeFromWorld(bukkitEntity);
            NMS.remove(bukkitEntity);
        } else {
            bukkitEntity.remove();
        }
        bukkitEntity = null;
    }

    @Override
    public boolean spawn(Location at) {
        return Util.isLoaded(at) && NMS.addEntityToWorld(bukkitEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
}
