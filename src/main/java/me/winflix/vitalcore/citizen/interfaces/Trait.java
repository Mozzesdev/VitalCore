package me.winflix.vitalcore.citizen.interfaces;

import me.winflix.vitalcore.citizen.enums.DespawnReason;
import me.winflix.vitalcore.citizen.enums.RemoveReason;
import me.winflix.vitalcore.citizen.models.NPC;
import org.bukkit.event.Listener;

public abstract class Trait implements Listener, Runnable {
    private final String name;
    protected NPC npc = null;
    private boolean runImplemented = true;

    protected Trait(String name) {
        this.name = name.toLowerCase();
    }

    public final String getName() {
        return name;
    }

    public final NPC getNPC() {
        return npc;
    }

    public boolean isRunImplemented() {
        run();
        return runImplemented;
    }

    public void linkToNPC(NPC npc) {
        if (this.npc != null)
            throw new IllegalArgumentException("npc may only be set once");
        this.npc = npc;
        onAttach();
    }

    public void onAttach() {
    }

    public void onDespawn() {
    }
    
    public void onDespawn(DespawnReason reason) {
    }

    public void onRemove() {
    }

    public void onPreSpawn() {
    }

    public void onRemove(RemoveReason reason) {
        onRemove();
    }

    public void onSpawn() {
    }

    public void load(DataKey key){
    }

    @Override
    public void run() {
        runImplemented = false;
    }

    public void save(DataKey key) {
    }

}
