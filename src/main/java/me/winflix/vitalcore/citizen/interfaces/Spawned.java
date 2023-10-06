package me.winflix.vitalcore.citizen.interfaces;

import me.winflix.vitalcore.citizen.trait.traits.TraitName;

@TraitName("spawned")
public class Spawned extends Trait {
    private boolean shouldSpawn = true;

    public Spawned() {
        super("spawned");
    }

    @Override
    public void load(DataKey key) {
        shouldSpawn = key.getBoolean("");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", shouldSpawn);
    }

    public void setSpawned(boolean shouldSpawn) {
        this.shouldSpawn = shouldSpawn;
    }

    public boolean shouldSpawn() {
        return shouldSpawn;
    }

    @Override
    public String toString() {
        return "Spawned{" + shouldSpawn + "}";
    }
}