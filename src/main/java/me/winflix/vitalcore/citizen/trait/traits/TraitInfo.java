package me.winflix.vitalcore.citizen.utils.trait.traits;

import com.google.common.base.Preconditions;
import me.winflix.vitalcore.citizen.interfaces.Trait;

import java.util.function.Supplier;

public final class TraitInfo {
    private boolean defaultTrait;
    private String name;
    private boolean trackStats;
    private Supplier<? extends Trait> supplier;
    private final Class<? extends Trait> trait;
    private boolean triedAnnotation;

    private TraitInfo(Class<? extends Trait> trait) {
        this.trait = trait;
    }

    public TraitInfo asDefaultTrait() {
        this.defaultTrait = true;
        return this;
    }

    public void checkValid() {
        if (supplier == null) {
            try {
                trait.getConstructor(new Class<?>[] {});
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Trait class must have a no-arguments constructor");
            }
        }
    }

    public Class<? extends Trait> getTraitClass() {
        return trait;
    }

    public String getTraitName() {
        if (name == null && !triedAnnotation) {
            TraitName anno = trait.getAnnotation(TraitName.class);
            if (anno != null) {
                name = anno.value().toLowerCase();
            }
            triedAnnotation = true;
        }
        return name;
    }

    public boolean isDefaultTrait() {
        return defaultTrait;
    }

    public TraitInfo optInToStats() {
        this.trackStats = true;
        return this;
    }

    public boolean trackStats() {
        return trackStats;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> T tryCreateInstance() {
        if (supplier != null)
            return (T) supplier.get();
        try {
            return (T) trait.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public TraitInfo withName(String name) {
        Preconditions.checkNotNull(name);
        this.name = name.toLowerCase();
        return this;
    }

    public TraitInfo withSupplier(Supplier<? extends Trait> supplier) {
        this.supplier = supplier;
        return this;
    }

    public static TraitInfo create(Class<? extends Trait> trait) {
        Preconditions.checkNotNull(trait);
        return new TraitInfo(trait);
    }
}