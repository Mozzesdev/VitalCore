package me.winflix.vitalcore.residents.utils.metadata;

import com.google.common.base.Preconditions;

import me.winflix.vitalcore.residents.interfaces.NPC;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SimpleMetadataStore implements MetadataStore {
    private final Map<String, MetadataObject> metadata = new HashMap<>();
    private final Map<NPC.Metadata, MetadataObject> npcMetadata = new EnumMap<>(NPC.Metadata.class);

    @Override
    public MetadataStore clone() {
        SimpleMetadataStore copy = new SimpleMetadataStore();
        copy.metadata.putAll(metadata);
        return copy;
    }

    @Override
    public <T> T get(NPC.Metadata key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        MetadataObject normal = this.npcMetadata.get(key);
        return normal == null ? null : (T) normal.value;
    }

    @Override
    public <T> T get(NPC.Metadata key, T def) {
        T t = get(key);
        return t == null ? def : t;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        MetadataObject normal = metadata.get(key);
        return normal == null ? null : (T) normal.value;
    }

    @Override
    public <T> T get(String key, T def) {
        T t = get(key);
        if (t == null) {
            return def;
        }
        return t;
    }

    @Override
    public boolean has(NPC.Metadata key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        return this.npcMetadata.containsKey(key);
    }

    @Override
    public boolean has(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        return metadata.containsKey(key);
    }

    @Override
    public void remove(NPC.Metadata key) {
        npcMetadata.remove(key);
    }

    @Override
    public void remove(String key) {
        metadata.remove(key);
    }

    @Override
    public void set(NPC.Metadata key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            this.remove(key);
        } else {
            this.npcMetadata.put(key, new MetadataObject(data));
        }

    }

    @Override
    public void set(String key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            remove(key);
        } else {
            metadata.put(key, new MetadataObject(data));
        }

    }

    @Override
    public int size() {
        return this.metadata.size() + this.npcMetadata.size();
    }

    private static class MetadataObject {
        final Object value;

        public MetadataObject(Object raw) {
            this.value = raw;
        }
    }
}
