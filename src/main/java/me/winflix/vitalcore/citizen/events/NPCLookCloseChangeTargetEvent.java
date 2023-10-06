package me.winflix.vitalcore.citizen.events;

import me.winflix.vitalcore.citizen.events.abstracts.NPCEvent;
import me.winflix.vitalcore.citizen.models.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class NPCLookCloseChangeTargetEvent extends NPCEvent {
    private Player next;
    private Player old;

    public NPCLookCloseChangeTargetEvent (NPC npc, Player old, Player next){
        super(npc);
        this.next = next;
        this.old = old;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getNewTarget() {
        return next;
    }

    public Player getPreviousTarget() {
        return old;
    }

    public void setNewTarget(Player target) {
        this.next = target;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
