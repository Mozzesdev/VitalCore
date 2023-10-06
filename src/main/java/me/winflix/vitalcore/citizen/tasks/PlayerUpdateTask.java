package me.winflix.vitalcore.citizen.tasks;

import me.winflix.vitalcore.citizen.interfaces.NPCHolder;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.citizen.utils.trait.traits.PacketHandlerNPC;
import me.winflix.vitalcore.core.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerUpdateTask extends BukkitRunnable {
    @Override
    public void cancel() {
        super.cancel();
        PLAYERS.clear();
    }

    @Override
    public void run() {
        for (Entity entity : PLAYERS_PENDING_REMOVE) {
            PLAYERS.remove(entity.getUniqueId());
        }
        for (Entity entity : PLAYERS_PENDING_ADD) {
            PlayerTick rm = PLAYERS.remove(entity.getUniqueId());
            NPC next = ((NPCHolder) entity).getNPC();
            if (rm != null) {
                NPC old = ((NPCHolder) rm.entity).getNPC();
                Bukkit.getLogger().warning(old == next ? "Player registered twice"
                        : "Player registered twice with different NPC instances");
                rm.entity.remove();
            }
            if (next.hasTrait(PacketHandlerNPC.class)) {
                PLAYERS.put(entity.getUniqueId(), new PlayerTick(entity, next::update));
            } else {
                PLAYERS.put(entity.getUniqueId(), new PlayerTick((Player) entity));
            }
        }
        PLAYERS_PENDING_ADD.clear();
        PLAYERS_PENDING_REMOVE.clear();

        PLAYERS.values().forEach(Runnable::run);
    }

    private static class PlayerTick implements Runnable {
        private final Entity entity;
        private final Runnable tick;

        public PlayerTick(Entity entity, Runnable tick) {
            this.entity = entity;
            this.tick = tick;
        }

        public PlayerTick(Player player) {
            this(player, NMS.playerTicker(player));
        }

        @Override
        public void run() {
            tick.run();
        }
    }

    public static void deregisterPlayer(org.bukkit.entity.Entity entity) {
        PLAYERS_PENDING_ADD.remove(entity);
        PLAYERS_PENDING_REMOVE.add(entity);
    }

    public static void registerPlayer(org.bukkit.entity.Entity entity) {
        PLAYERS_PENDING_REMOVE.remove(entity);
        PLAYERS_PENDING_ADD.add(entity);
    }

    private static final Map<UUID, PlayerTick> PLAYERS = new HashMap<>();
    private static final List<Entity> PLAYERS_PENDING_ADD = new ArrayList<>();
    private static final List<Entity> PLAYERS_PENDING_REMOVE = new ArrayList<>();
}
