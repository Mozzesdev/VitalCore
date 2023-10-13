package me.winflix.vitalcore.residents.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import me.winflix.vitalcore.residents.utils.Utils;
import me.winflix.vitalcore.residents.utils.controllers.EntityController;
import me.winflix.vitalcore.residents.utils.nms.NMS;

public abstract class AbstractEntityController implements EntityController {
    private Entity bukkitEntity;

    public AbstractEntityController() {
    }

    @Override
    public void create(Location at, NPC npc) {
        bukkitEntity = createEntity(at, npc);
    }

    protected abstract Entity createEntity(Location at, NPC npc);

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
            bukkitEntity = null;
        } else {
            bukkitEntity.remove();
            bukkitEntity = null;
        }
    }

    @Override
    public boolean spawn(Location at) {
        return !Utils.isLoaded(at) ? false : NMS.addEntityToWorld(bukkitEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
}