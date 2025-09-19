package me.winflix.vitalcore.residents.trait.traits;

import me.winflix.vitalcore.residents.trait.Trait;
import me.winflix.vitalcore.residents.trait.TraitName;
import me.winflix.vitalcore.residents.utils.nms.NMS;

@TraitName("gravity")
public class Gravity extends Trait {

    private boolean nogravity = false;

    public Gravity() {
        super("gravity");
    }

    private void apply() {
        if (nogravity && npc.getEntity() != null) {
            npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0));
            NMS.setNoGravity(npc.getEntity(), nogravity);
        }
    }

    public boolean hasGravity() {
        return !nogravity;
    }

    @Override
    public void onSpawn() {
        apply();
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        NMS.setNoGravity(npc.getEntity(), nogravity);
    }

}
