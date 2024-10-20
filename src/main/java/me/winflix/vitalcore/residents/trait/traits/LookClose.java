package me.winflix.vitalcore.residents.trait.traits;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.winflix.vitalcore.residents.trait.Trait;
import me.winflix.vitalcore.residents.trait.TraitName;
import me.winflix.vitalcore.residents.utils.nms.NMS;
import net.minecraft.world.entity.EntityType;

@TraitName("lookclose")
public class LookClose extends Trait {

    private double range = 10;
    private Player lookingAt;

    public LookClose() {
        super("lookclose");
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public Player getLookingAt() {
        return lookingAt;
    }

    public Location getEyeLocation() {
        return npc.getLocation().clone().add(0, EntityType.PLAYER.getDimensions().width() * 0.1, 0);
    }

    public void findTarget() {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player player : getNearbyPlayers()) {
            double distance = player.getLocation().distance(npc.getLocation());
            if (distance < closestDistance) {
                closestPlayer = player;
                closestDistance = distance;
            }
        }

        lookingAt = closestPlayer;
    }

    @Override
    public void run() {
        if (!npc.isSpawned()) {
            lookingAt = null;
            return;
        }

        findTarget();

        if (lookingAt == null)
            return;

        lookAtPoint(lookingAt.getLocation());
    }

    public void lookAtPoint(Location location) {
        Location eyeLocation = this.getEyeLocation();
        float yaw = (float) Math.toDegrees(Math.atan2(location.getZ() - eyeLocation.getZ(), location.getX() - eyeLocation.getX())) - 90;
        yaw = (float) (yaw + Math.ceil(-yaw / 360) * 360);
        float deltaXZ = (float) Math.sqrt(Math.pow(eyeLocation.getX() - location.getX(), 2) + Math.pow(eyeLocation.getZ() - location.getZ(), 2));
        float pitch = (float) Math.toDegrees(Math.atan2(deltaXZ, location.getY() - eyeLocation.getY())) - 90;
        pitch = (float) (pitch + Math.ceil(-pitch / 360) * 360);

        npc.getLocation().setYaw((float) yaw);
        npc.getLocation().setPitch((float) pitch);

        for (Player player : Bukkit.getOnlinePlayers()) {
            NMS.rotateHead(player, npc, true);
        }
    }

    public Set<Player> getNearbyPlayers() {
        return Bukkit.getOnlinePlayers().stream().filter(player -> isValid(player)).collect(Collectors.toSet());
    }

    private boolean isInvisible(Player player) {
        return player.getGameMode() == GameMode.SPECTATOR || player.hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    private boolean isValid(Player entity) {
        return entity.isOnline() && entity.isValid() && entity.getWorld() == npc.getEntity().getWorld()
                && entity.getLocation().distance(npc.getLocation()) <= range && !isInvisible(entity);
    }

    @Override
    public void onDespawn() {
        lookingAt = null;
    }

    @Override
    public String toString() {
        return "LookClose{" + npc.toString() + "}";
    }
}
