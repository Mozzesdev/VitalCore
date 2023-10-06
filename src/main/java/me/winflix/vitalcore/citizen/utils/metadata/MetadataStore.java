package me.winflix.vitalcore.citizen.utils.metadata;

import me.winflix.vitalcore.citizen.interfaces.DataKey;
import me.winflix.vitalcore.citizen.models.NPC;

public interface MetadataStore {
    MetadataStore clone();

    default <T> T get(NPC.Metadata key) {
        return get(key.getKey());
    }

    default <T> T get(NPC.Metadata key, T def) {
        return get(key.getKey(), def);
    }

    <T> T get(String key);

    <T> T get(String key, T def);

    default boolean has(NPC.Metadata key) {
        return has(key.getKey());
    }

    boolean has(String key);

    void loadFrom(DataKey key);

    default void remove(NPC.Metadata distance) {
        remove(distance.getKey());
    }

    void remove(String key);

    void saveTo(DataKey key);

    default void set(NPC.Metadata key, Object data) {
        set(key.getKey(), data);
    }

    void set(String key, Object data);

    default void setPersistent(NPC.Metadata key, Object data) {
        setPersistent(key.getKey(), data);
    }

    void setPersistent(String key, Object data);

    int size();
}
