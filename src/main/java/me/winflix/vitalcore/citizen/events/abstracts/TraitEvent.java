package me.winflix.vitalcore.citizen.events;

import me.winflix.vitalcore.citizen.interfaces.Trait;
import me.winflix.vitalcore.citizen.models.NPC;

public abstract class TraitEvent extends NPCEvent {

    private final Trait trait;

    protected TraitEvent(NPC npc, Trait trait) {
        super(npc);
        this.trait = trait;
    }

    public Trait getTrait() {
        return trait;
    }

}
