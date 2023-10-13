package me.winflix.vitalcore.residents.utils.metadata;

import me.winflix.vitalcore.residents.interfaces.NPC;

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

    default void remove(NPC.Metadata distance) {
        remove(distance.getKey());
    }

    void remove(String key);

    default void set(NPC.Metadata key, Object data) {
        set(key.getKey(), data);
    }

    void set(String key, Object data);

    int size();
}
