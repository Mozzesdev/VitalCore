package me.winflix.vitalcore.citizen.utils;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.citizen.events.NPCCollisionEvent;
import me.winflix.vitalcore.citizen.events.NPCPushEvent;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.core.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Util {

    public static void callCollisionEvent(NPC npc, Entity entity) {
        if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length > 0) {
            Bukkit.getPluginManager().callEvent(new NPCCollisionEvent(npc, entity));
        }
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

    public static float clamp(float angle, float min, float max, float d) {
        while (angle < min) {
            angle += d;
        }
        while (angle >= max) {
            angle -= d;
        }
        return angle;
    }

    public static boolean isAlwaysFlyable(EntityType type) {
        if (type.name().equalsIgnoreCase("vex") || type.name().equalsIgnoreCase("parrot")
                || type.name().equalsIgnoreCase("allay") || type.name().equalsIgnoreCase("bee")
                || type.name().equalsIgnoreCase("phantom"))
            // 1.8.8 compatibility
            return true;
        return switch (type) {
            case BAT, BLAZE, ENDER_DRAGON, GHAST, WITHER -> true;
            default -> false;
        };
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
            return Bukkit.getScheduler().callSyncMethod(VitalCore.getPlugin(), callable).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Duration parseDuration(String raw, TimeUnit defaultUnits) {
        if (defaultUnits == null) {
            Integer ticks = Ints.tryParse(raw);
            if (ticks != null) {
                return Duration.ofMillis(ticks * 50);
            }
        } else if (NUMBER_MATCHER.matcher(raw).matches()) {
            return Duration.of(Longs.tryParse(raw), toChronoUnit(defaultUnits));
        }

        if (raw.endsWith("t")) {
            return Duration.ofMillis(Integer.parseInt(raw.substring(0, raw.length() - 1)) * 50);
        }
        raw = DAY_MATCHER.matcher(raw).replaceFirst("P$1T").replace("min", "m").replace("hr", "h");
        if (raw.charAt(0) != 'P') {
            raw = "PT" + raw;
        }
        return Duration.parse(raw);
    }

    public static int toTicks(Duration delay) {
        return (int) (TimeUnit.MILLISECONDS.convert(delay.getSeconds(), TimeUnit.SECONDS)
                + TimeUnit.MILLISECONDS.convert(delay.getNano(), TimeUnit.NANOSECONDS)) / 50;
    }

    public static Vector callPushEvent(NPC npc, double x, double y, double z) {
        boolean allowed = npc == null || !npc.isProtected()
                || (npc.getMetaData().has(NPC.Metadata.COLLIDABLE) && npc.getMetaData().<Boolean> get(NPC.Metadata.COLLIDABLE));
        if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return allowed ? new Vector(x, y, z) : null;
        }
        // when another entity collides, this method is called to push the NPC so we prevent it from
        // doing anything if the event is cancelled.
        Vector vector = new Vector(x, y, z);
        NPCPushEvent event = new NPCPushEvent(npc, vector, null);
        event.setCancelled(!allowed);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled() ? event.getCollisionVector() : null;
    }

    public static boolean inBlock(Entity entity) {
        // TODO: bounding box aware?
        Location loc = entity.getLocation();
        if (!Util.isLoaded(loc)) {
            return false;
        }
        Block in = loc.getBlock();
        Block above = in.getRelative(BlockFace.UP);
        return in.getType().isSolid() && above.getType().isSolid() && NMS.isSolid(in) && NMS.isSolid(above);
    }

    public static boolean isLoaded(Location location) {
        if (location.getWorld() == null)
            return false;
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return location.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public static boolean checkYSafe(double y, World world) {
        if (!SUPPORT_WORLD_HEIGHT || world == null) {
            return y >= 0 && y <= 255;
        }
        try {
            return y >= world.getMinHeight() && y <= world.getMaxHeight();
        } catch (Throwable t) {
            SUPPORT_WORLD_HEIGHT = false;
            return y >= 0 && y <= 255;
        }
    }

    public static String getTeamName(UUID id) {
        return "CIT-" + id.toString().replace("-", "").substring(0, 12);
    }

    public static Scoreboard getDummyScoreboard() {
        return DUMMY_SCOREBOARD;
    }


    private static ChronoUnit toChronoUnit(TimeUnit tu) {
        switch (tu) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new AssertionError();
        }
    }

    public static String getMinecraftRevision() {
        if (MINECRAFT_REVISION == null) {
            MINECRAFT_REVISION = Bukkit.getServer().getClass().getPackage().getName();
        }
        return MINECRAFT_REVISION.substring(MINECRAFT_REVISION.lastIndexOf('.') + 2);
    }

    private static boolean SUPPORT_WORLD_HEIGHT = true;
    private static Pattern NUMBER_MATCHER = Pattern.compile("(\\d+)");
    private static Pattern DAY_MATCHER = Pattern.compile("(\\d+d)");
    private static final Scoreboard DUMMY_SCOREBOARD = Bukkit.getScoreboardManager().getNewScoreboard();
    private static String MINECRAFT_REVISION;


}
