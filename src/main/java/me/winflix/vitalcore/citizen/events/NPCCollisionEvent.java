package me.winflix.vitalcore.citizen.events;

import me.winflix.vitalcore.citizen.events.abstracts.NPCEvent;
import me.winflix.vitalcore.citizen.models.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

public class NPCCollisionEvent extends NPCEvent {
    private final Entity entity;

    public NPCCollisionEvent(NPC npc, Entity entity) {
        super(npc);
        this.entity = entity;
    }

    public Entity getCollidedWith() {
        return entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
