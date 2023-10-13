package me.winflix.vitalcore.residents.trait;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.HandlerList;

import me.winflix.vitalcore.residents.Residents;

public class TraitStorage {

    public final Map<Class<? extends Trait>, Trait> traits = new HashMap<>();

    public <T extends Trait> T getTrait(Class<T> clazz) {
        return clazz.cast(traits.get(clazz));
    }

    public Collection<Trait> getTraits() {
        return traits.values();
    }

    public boolean hasTrait(Class<? extends Trait> traitClass) {
        return traits.containsKey(traitClass);
    }

    public void removeTrait(Class<? extends Trait> traitClass) {
        Trait trait = traits.remove(traitClass);
        if (trait != null) {
            HandlerList.unregisterAll(trait);
            trait.onRemove();
        }
    }

    public void addTrait(Class<? extends Trait> clazz) {
        addTrait(getTraitFor(clazz));
    }

    protected Trait getTraitFor(Class<? extends Trait> clazz) {
        return Residents.getTraitManager().getTrait(clazz);
    }

    public <T extends Trait> T getOrAddTrait(Class<T> clazz) {
        Trait trait = traits.get(clazz);
        if (trait == null) {
            trait = getTraitFor(clazz);
            addTrait(trait);
        }
        return clazz.cast(trait);
    }

    public void addTrait(Trait trait) {
    };

}
