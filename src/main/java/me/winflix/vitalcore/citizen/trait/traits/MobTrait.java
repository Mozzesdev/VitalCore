package me.winflix.vitalcore.citizen.trait.traits;

import me.winflix.vitalcore.citizen.interfaces.DataKey;
import me.winflix.vitalcore.citizen.interfaces.Trait;
import org.bukkit.entity.EntityType;

@TraitName("mob")
public class MobTrait extends Trait {
    private EntityType type = EntityType.PLAYER;

    public MobTrait() {
        super("mob");
    }

    public EntityType getType() {
        return type;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void load(DataKey key) {
        try {
            if (key.getString("").equals("PIG_ZOMBIE")) {
                type = EntityType.ZOMBIFIED_PIGLIN;
            } else {
                type = EntityType.valueOf(key.getString(""));
            }
        } catch (IllegalArgumentException ex) {
            type = EntityType.fromName(key.getString(""));
        }
        if (type == null) {
            type = EntityType.PLAYER;
        }
    }

    @Override
    public void onSpawn() {
        type = npc.getEntity().getType();
    }

    @Override
    public void save(DataKey key) {
        key.setString("", type.name());
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MobTrait{" + type + "}";
    }
}
