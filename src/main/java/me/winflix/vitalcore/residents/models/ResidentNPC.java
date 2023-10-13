package me.winflix.vitalcore.residents.models;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Preconditions;
import com.mojang.authlib.properties.Property;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.residents.Residents;
import me.winflix.vitalcore.residents.interfaces.NPC;
import me.winflix.vitalcore.residents.trait.Trait;
import me.winflix.vitalcore.residents.trait.TraitStorage;
import me.winflix.vitalcore.residents.utils.controllers.EntityController;
import me.winflix.vitalcore.residents.utils.controllers.EntityControllers;
import me.winflix.vitalcore.residents.utils.metadata.MetadataStore;
import me.winflix.vitalcore.residents.utils.metadata.SimpleMetadataStore;
import me.winflix.vitalcore.residents.utils.nms.NMS;

public class ResidentNPC extends TraitStorage implements NPC {

    private final int id;
    private final UUID uuid;
    private final String name;
    private final Location location;
    private static AtomicInteger atomicInteger;
    private final EntityController entityController;
    public final Set<Runnable> runnables = new HashSet<>();

    static {
        try {
            Field field = net.minecraft.world.entity.Entity.class.getDeclaredField("d");
            field.setAccessible(true);
            atomicInteger = (AtomicInteger) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Bukkit.getLogger().info("Error when accessing the field 'd' of the Entity class: " + e.getMessage());
        }
    }

    public ResidentNPC(UUID uuid, Location location, String displayName, EntityType type) {
        this.entityController = EntityControllers.createForType(type);
        this.id = atomicInteger.incrementAndGet();
        this.location = location;
        this.uuid = uuid;
        this.name = displayName;
    }

    public ResidentNPC(Location loc, String displayName, EntityType type) {
        this(UUID.randomUUID(), loc, displayName, type);
        Residents.getTraitManager().addDefaultTraits(this);
    }

    @Override
    public MetadataStore getMetadata() {
        return metadata;
    }

    @Override
    public boolean spawn(Location location) {
        Preconditions.checkNotNull(location, "Location cannot be null");

        if (getEntity() != null) {
            Bukkit.getLogger().info("Tried to spawn" + this + " while already spawned.");
            return false;
        }
        if (location.getWorld() == null) {
            Bukkit.getLogger().info("Tried to spawn" + this + " but the world was null. SpawnReason.");
            return false;
        }

        location = location.clone();

        if (!location.getChunk().isLoaded()) {
            location.getChunk().load();
        }

        entityController.create(location, this);
        getEntity().setMetadata("NPC", new FixedMetadataValue(Residents.getPlugin(), true));
        getEntity().setMetadata("NPC-ID", new FixedMetadataValue(Residents.getPlugin(), getId()));

        Set<Trait> preSpawnTraits = new HashSet<Trait>(traits.values());

        preSpawnTraits.forEach(trait -> {
            try {
                trait.onPreSpawn();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        });

        boolean isSpawned = entityController.spawn(location);

        if (!isSpawned) {
            entityController.remove();
            Bukkit.getLogger().info("Could not spawn NPC" + this);
            return false;
        }

        NMS.setExactLocation(getEntity(), location);
        NMS.setHeadYaw(getEntity(), location.getYaw());
        NMS.setBodyYaw(getEntity(), location.getYaw());

        return true;
    }

    @Override
    public boolean despawn() {
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void faceLocation(Location location) {
    }

    @Override
    public Entity getEntity() {
        return entityController == null ? null : entityController.getBukkitEntity();
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isSpawned() {
        return getEntity() != null && NMS.isEntityValid(getEntity());
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isPushableByFluids() {
        return false;
    }

    @Override
    public void setBukkitEntityType(EntityType type) {
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public void setProtected(boolean isProtected) {
    }

    @Override
    public void teleport(Location location, TeleportCause cause) {
    }

    @Override
    public void setSkin(Property property) {
    }

    public boolean requiresNameHologram() {
        EntityType entityType = getEntityType();
        boolean isArmorStand = entityType == EntityType.ARMOR_STAND;
        boolean isTextDisplay = entityType.name().equals("TEXT_DISPLAY");
        boolean isPlayer = entityType == EntityType.PLAYER;

        return !(isArmorStand || isTextDisplay || (isPlayer && name.length() <= 16))
                || getMetadata().get(NPC.Metadata.ALWAYS_USE_NAME_HOLOGRAM, false);
    }

    protected EntityType getEntityType() {
        return isSpawned() ? getEntity().getType() : EntityType.PLAYER;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ResidentNPC other = (ResidentNPC) obj;
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

    @Override
    public void addTrait(Trait trait) {
        if (trait == null) {
            Bukkit.getLogger().severe("Cannot register a null trait. Was it registered properly?");
            return;
        }

        if (trait.getNPC() == null) {
            trait.linkToNPC(this);
        }

        Class<? extends Trait> clazz = trait.getClass();
        Trait replaced = traits.get(clazz);

        Bukkit.getPluginManager().registerEvents(trait, Residents.getPlugin());

        traits.put(clazz, trait);

        if (isSpawned()) {
            trait.onSpawn();
        }

        if (trait.isRunImplemented()) {
            if (replaced != null) {
                runnables.remove(replaced);
            }
            runnables.add(trait);
        }
    }

    @Override
    public String toString() {
        return getId() + "{" + getName() + ", " + null + "}";
    }

    MetadataStore metadata = new SimpleMetadataStore() {
        @Override
        public void remove(String key) {
            super.remove(key);
            if (getEntity() != null) {
                getEntity().removeMetadata(key, VitalCore.getPlugin());
            }
        }

        @Override
        public void set(String key, Object data) {
            super.set(key, data);
            if (getEntity() != null) {
                getEntity().setMetadata(key, new FixedMetadataValue(VitalCore.getPlugin(), data));
            }
        }
    };

}
