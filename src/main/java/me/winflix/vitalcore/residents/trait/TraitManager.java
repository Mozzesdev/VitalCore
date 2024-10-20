package me.winflix.vitalcore.residents.trait;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import me.winflix.vitalcore.residents.trait.traits.Gravity;
import me.winflix.vitalcore.residents.trait.traits.LookClose;

public class TraitManager {
    private final List<TraitInfo> defaultTraits = new ArrayList<>();
    private final Map<String, TraitInfo> registered = new HashMap<>();

    public TraitManager() {
        registerTrait(TraitInfo.create(LookClose.class));
        registerTrait(TraitInfo.create(Gravity.class));
    }

    public void addDefaultTraits(TraitStorage traitStorage) {
        for (TraitInfo info : defaultTraits) {
            traitStorage.addTrait(create(info));
        }
    }

    private <T extends Trait> T create(TraitInfo info) {
        return info.tryCreateInstance();
    }

    public void deregisterTrait(TraitInfo info) {
        Preconditions.checkNotNull(info, "info cannot be null");
        registered.remove(info.getTraitName());
    }

    public Collection<TraitInfo> getRegisteredTraits() {
        return registered.values();
    }

    public <T extends Trait> T getTrait(Class<T> clazz) {
        for (TraitInfo entry : registered.values()) {
            if (clazz == entry.getTraitClass()) {
                return create(entry);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> T getTrait(String name) {
        TraitInfo info = registered.get(name.toLowerCase());
        if (info == null)
            return null;
        return (T) create(info);
    }

    public Class<? extends Trait> getTraitClass(String name) {
        TraitInfo info = registered.get(name.toLowerCase());
        return info == null ? null : info.getTraitClass();
    }

    public void registerTrait(TraitInfo info) {
        Preconditions.checkNotNull(info, "info cannot be null");
        info.checkValid();
        if (registered.containsKey(info.getTraitName())) {
            throw new IllegalArgumentException("Trait name " + info.getTraitName() + " already registered");
        }
        registered.put(info.getTraitName(), info);
        if (info.isDefaultTrait()) {
            defaultTraits.add(info);
        }
    }

    public boolean trackStats(Trait trait) {
        return registered.get(trait.getName()).trackStats();
    }

}
