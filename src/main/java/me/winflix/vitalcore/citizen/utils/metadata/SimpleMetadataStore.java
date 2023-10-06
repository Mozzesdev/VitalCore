package me.winflix.vitalcore.citizen.utils.metadata;

import com.google.common.base.Preconditions;
import me.winflix.vitalcore.citizen.interfaces.DataKey;
import me.winflix.vitalcore.citizen.interfaces.MetadataStore;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SimpleMetadataStore implements MetadataStore {
    private final Map<String, MetadataObject> metadata = new HashMap<>();
    private final Map<Metadata, MetadataObject> npcMetadata = new EnumMap<>(Metadata.class);

    private void checkPrimitive(Object data) {
        Preconditions.checkNotNull(data, "data cannot be null");
        boolean isPrimitive = data instanceof String || data instanceof Boolean || data instanceof Number;
        if (!isPrimitive) {
            throw new IllegalArgumentException("data is not primitive");
        }
    }

    public MetadataStore clone() {
        MetadataStore copy = new SimpleMetadataStore();
        copy.metadata.putAll(metadata);
        return copy;
    }

    public <T> T get(Metadata key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        MetadataObject normal = this.npcMetadata.get(key);
        return normal == null ? null : (T) normal.value;
    }

    public <T> T get(Metadata key, T def) {
        T t = get(key);
        return t == null ? def : t;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        MetadataObject normal = metadata.get(key);
        return normal == null ? null : (T) normal.value;
    }

    public <T> T get(String key, T def) {
        T t = get(key);
        if (t == null) {
            return def;
        }
        return t;
    }

    public boolean has(Metadata key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        return this.npcMetadata.containsKey(key);
    }

    public boolean has(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        return metadata.containsKey(key);
    }

    public void loadFrom(DataKey key) {
        metadata.entrySet().removeIf(e -> e.getValue().persistent);
        npcMetadata.entrySet().removeIf(e -> e.getValue().persistent);
        for (DataKey sub : key.getSubKeys()) {
            Metadata meta = Metadata.byKey(sub.name());
            if (meta != null) {
                setPersistent(String.valueOf(meta), sub.getRaw(""));
            } else {
                setPersistent(sub.name(), sub.getRaw(""));
            }
        }

    }

    public void remove(Metadata key) {
        npcMetadata.remove(key);
    }

    public void remove(String key) {
        metadata.remove(key);
    }

    public void saveTo(DataKey key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        for (Map.Entry<String, MetadataObject> entry : metadata.entrySet()) {
            if (entry.getValue().persistent) {
                key.setRaw(entry.getKey(), entry.getValue().value);
            }
        }

        for (Map.Entry<Metadata, MetadataObject> entry : npcMetadata.entrySet()) {
            if (entry.getValue().persistent) {
                key.setRaw(entry.getKey().getKey(), entry.getValue().value);
            }
        }
    }

    public void set(Metadata key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            this.remove(key);
        } else {
            this.npcMetadata.put(key, new MetadataObject(data, false));
        }

    }

    public void set(String key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            remove(key);
        } else {
            metadata.put(key, new MetadataObject(data, false));
        }

    }

    public void setPersistent(Metadata key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            this.remove(key);
        } else {
            this.checkPrimitive(data);
            this.npcMetadata.put(key, new MetadataObject(data, true));
        }
    }

    public void setPersistent(String key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            this.remove(key);
        } else {
            this.checkPrimitive(data);
            this.metadata.put(key, new MetadataObject(data, true));
        }
    }

    public int size() {
        return this.metadata.size() + this.npcMetadata.size();
    }

    private static class MetadataObject {
        final boolean persistent;
        final Object value;

        public MetadataObject(Object raw, boolean persistent) {
            this.value = raw;
            this.persistent = persistent;
        }
    }
}
