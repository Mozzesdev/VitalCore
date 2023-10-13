package me.winflix.vitalcore.residents.trait;

import org.bukkit.event.Listener;

import me.winflix.vitalcore.residents.interfaces.NPC;

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
    
    public void onRemove() {
    }

    public void onPreSpawn() {
    }

    public void onSpawn() {
    }

    @Override
    public void run() {
        runImplemented = false;
    }

}
