package me.winflix.vitalcore.citizen.events;

import me.winflix.vitalcore.citizen.events.abstracts.NPCEvent;
import me.winflix.vitalcore.citizen.models.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

public class NPCPushEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private final Vector collisionVector;
    private final Entity pushedBy;

    public NPCPushEvent(NPC npc, Vector vector, Entity pushedBy) {
        super(npc);
        this.collisionVector = vector;
        this.pushedBy = pushedBy;
    }

    public Vector getCollisionVector() {
        return collisionVector;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Entity getPushedBy() {
        return pushedBy;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
