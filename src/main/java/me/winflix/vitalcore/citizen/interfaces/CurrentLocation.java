package me.winflix.vitalcore.citizen.interfaces;

import org.bukkit.Location;

import me.winflix.vitalcore.citizen.trait.traits.TraitName;
import me.winflix.vitalcore.core.nms.NMS;

@TraitName("location")
public class CurrentLocation extends Trait {
    private float bodyYaw = Float.NaN;
    private Location location = new Location(null, 0, 0, 0);

    public CurrentLocation() {
        super("location");
    }

    public float getBodyYaw() {
        return bodyYaw;
    }

    public Location getLocation() {
        return location.getWorld() == null ? null : location.clone();
    }

    @Override
    public void load(DataKey key) {
        key.removeKey("headYaw");
    }

    @Override
    public void onSpawn() {
        if (!Float.isNaN(bodyYaw)) {
            NMS.setBodyYaw(npc.getEntity(), bodyYaw);
        }
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        location = npc.getEntity().getLocation(location);
        bodyYaw = NMS.getYaw(npc.getEntity());
    }

    public void setLocation(Location loc) {
        this.location = loc.clone();
    }

    @Override
    public String toString() {
        return "CurrentLocation{" + location + "}";
    }
}
