package me.winflix.vitalcore.residents.utils;

import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.winflix.vitalcore.residents.Residents;

public class Utils {

    public static boolean isLoaded(Location location) {
        if (location.getWorld() == null)
            return false;
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return location.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public static float clamp(float angle) {
        while (angle < -180.0F) {
            angle += 360.0F;
        }
        while (angle >= 180.0F) {
            angle -= 360.0F;
        }
        return angle;
    }

    public static <T> T callPossiblySync(Callable<T> callable, boolean sync) {
        if (!sync) {
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            return Bukkit.getScheduler().callSyncMethod(Residents.getPlugin(), callable).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
