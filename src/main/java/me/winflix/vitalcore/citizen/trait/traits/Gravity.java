package me.winflix.vitalcore.citizen.trait.traits;

import me.winflix.vitalcore.citizen.interfaces.Toggleable;
import me.winflix.vitalcore.citizen.interfaces.Trait;
import me.winflix.vitalcore.core.nms.NMS;

@TraitName("gravity")
public class Gravity extends Trait implements Toggleable {
    private boolean nogravity;

    public Gravity() {
        super("gravity");
    }

    private void applyImmediately() {
        if (nogravity && npc.getEntity() != null) {
            npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0));
            NMS.setNoGravity(npc.getEntity(), nogravity);
        }
    }

    public void gravitate(boolean gravitate) {
        nogravity = gravitate;
    }

    public boolean hasGravity() {
        return !nogravity;
    }

    @Override
    public void onSpawn() {
        applyImmediately();
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        NMS.setNoGravity(npc.getEntity(), nogravity);
    }

    public void setEnabled(boolean enabled) {
        this.nogravity = enabled;
    }

    @Override
    public boolean toggle() {
        nogravity = !nogravity;
        applyImmediately();
        return nogravity;
    }
}