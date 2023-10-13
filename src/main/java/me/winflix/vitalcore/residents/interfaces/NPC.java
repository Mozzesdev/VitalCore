package me.winflix.vitalcore.residents.interfaces;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.reflect.TypeToken;
import com.mojang.authlib.properties.Property;

import me.winflix.vitalcore.residents.utils.metadata.MetadataStore;

public interface NPC {

    public boolean requiresNameHologram();

    public MetadataStore getMetadata();

    public boolean despawn();

    public void destroy();

    public void faceLocation(Location location);

    public Entity getEntity();

    public int getId();

    public UUID getUniqueId();

    public String getName();

    public Location getLocation();

    public boolean isSpawned();

    public boolean isProtected();

    public boolean isPushableByFluids();

    public void setBukkitEntityType(EntityType type);

    public void setName(String name);

    public void setProtected(boolean isProtected);

    public boolean spawn(Location location);

    public void teleport(Location location, TeleportCause cause);

    public void setSkin(Property property);

    public enum Metadata {
        ACTIVATION_RANGE("activation-range", Integer.class),
        AGGRESSIVE("entity-aggressive", Boolean.class),
        ALWAYS_USE_NAME_HOLOGRAM("always-use-name-hologram", Boolean.class),
        AMBIENT_SOUND("ambient-sound", String.class),
        COLLIDABLE("collidable", Boolean.class),
        DAMAGE_OTHERS("damage-others", Boolean.class),
        DEATH_SOUND("death-sound", String.class),
        DEFAULT_PROTECTED("protected", Boolean.class),
        DROPS_ITEMS("drops-items", Boolean.class),
        FLUID_PUSHABLE("fluid-pushable", Boolean.class),
        FLYABLE("flyable", Boolean.class),
        FORCE_PACKET_UPDATE("force-packet-update", Boolean.class),
        GLOWING("glowing", Boolean.class),
        HURT_SOUND("hurt-sound", String.class),
        KEEP_CHUNK_LOADED("keep-chunk-loaded", Boolean.class),
        KNOCKBACK("knockback", Boolean.class),
        NAMEPLATE_VISIBLE("nameplate-visible", Boolean.class),
        PACKET_UPDATE_DELAY("packet-update-delay", Integer.class),
        PATHFINDER_FALL_DISTANCE("pathfinder-fall-distance", Double.class),
        PICKUP_ITEMS("pickup-items", Boolean.class),
        REMOVE_FROM_PLAYERLIST("removefromplayerlist", Boolean.class),
        SCOREBOARD_FAKE_TEAM_NAME("fake-scoreboard-team-name", String.class),
        SILENT("silent-sounds", Boolean.class),
        SNEAKING("citizens-sneaking", Boolean.class),
        SWIMMING("swim", Boolean.class),
        TARGETABLE("protected-target", Boolean.class),
        TRACKING_RANGE("tracking-distance", Integer.class),
        USING_HELD_ITEM("using-held-item", Boolean.class),
        USING_OFFHAND_ITEM("using-offhand-item", Boolean.class),
        WATER_SPEED_MODIFIER("water-speed-modifier", Double.class);

        private final String key;
        private final TypeToken<?> type;

        Metadata(String key, Class<?> type) {
            this(key, TypeToken.of(type));
        }

        Metadata(String key, TypeToken<?> type) {
            this.key = key;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public TypeToken<?> getType() {
            return type;
        }

        public static NPC.Metadata byKey(String name) {
            for (NPC.Metadata v : NPC.Metadata.values()) {
                if (v.key.equals(name))
                    return v;
            }
            return null;
        }

        public static NPC.Metadata byName(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }
    }
}
