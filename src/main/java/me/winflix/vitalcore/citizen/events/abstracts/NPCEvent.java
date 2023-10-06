package me.winflix.vitalcore.citizen.events;

import me.winflix.vitalcore.citizen.models.NPC;
import org.bukkit.event.Event;

public abstract class NPCEvent extends Event {
    final NPC npc;

    protected NPCEvent(NPC npc){
        this.npc = npc;
    }

    public NPC getNpc() {
        return npc;
    }
}
