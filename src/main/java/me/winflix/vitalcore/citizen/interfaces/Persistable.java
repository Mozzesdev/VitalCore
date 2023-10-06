package me.winflix.vitalcore.citizen.interfaces;

public interface Persistable {
    public void load(DataKey root);

    public void save(DataKey root);
}
