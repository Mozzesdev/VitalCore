package me.winflix.vitalcore.citizen.utils.trait.traits;

import me.winflix.vitalcore.citizen.interfaces.Trait;
import org.bukkit.entity.EntityType;

@TraitName("mob")
public class MobTrait extends Trait {
    private EntityType type = EntityType.PLAYER;
    protected MobTrait() {
        super("mob");
    }

    public EntityType getType() {
        return type;
    }

    @Override
    public void onSpawn() {
        type = npc.getEntity().getType();
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MobType{" + type + "}";
    }
}
