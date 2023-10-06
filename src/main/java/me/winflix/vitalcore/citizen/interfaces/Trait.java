package me.winflix.vitalcore.citizen.traits;

import me.winflix.vitalcore.citizen.models.NPC;

public interface Trait {
    void onStart(NPC npc);
    void onStop(NPC npc);
    void onTick(NPC npc);
}
