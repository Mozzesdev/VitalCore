package me.winflix.vitalcore.citizen.events;

import me.winflix.vitalcore.citizen.events.abstracts.TraitEvent;
import me.winflix.vitalcore.citizen.interfaces.Trait;
import me.winflix.vitalcore.citizen.models.NPC;
import org.bukkit.event.HandlerList;

public class NPCAddTraitEvent extends TraitEvent {
    public NPCAddTraitEvent(NPC npc, Trait trait) {
        super(npc, trait);
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