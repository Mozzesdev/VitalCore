package me.winflix.vitalcore.citizen.events;

import me.winflix.vitalcore.citizen.events.abstracts.NPCEvent;
import me.winflix.vitalcore.citizen.models.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

public class NPCKnockbackEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private final Entity entity;
    private final double strength;
    private final Vector vector;

    public NPCKnockbackEvent(NPC npc, double strength, Vector vector, Entity entity) {
        super(npc);
        this.entity = entity;
        this.strength = strength;
        this.vector = vector;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Vector getKnockbackVector() {
        return vector;
    }

    public Entity getKnockingBackEntity() {
        return entity;
    }

    public double getStrength() {
        return strength;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
