package me.winflix.vitalcore.citizen.models;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class DeathBody {
    private ServerPlayer npc;
    private UUID whoDied;
    private Inventory inventory;
    private long whenDied;
    private Map<UUID, List<ItemStack>> itemsTaken;
    private Location location;

    public DeathBody() {
        this.itemsTaken = new HashMap<>();
    }

    public DeathBody(ServerPlayer npc, UUID whoDied, Inventory inventory, long whenDied) {
        this.npc = npc;
        this.whoDied = whoDied;
        this.inventory = inventory;
        this.whenDied = whenDied;
    }

    public ServerPlayer getNpc() {
        return npc;
    }

    public void setNpc(ServerPlayer npc) {
        this.npc = npc;
    }

    public UUID getWhoDied() {
        return whoDied;
    }

    public void setWhoDied(UUID whoDied) {
        this.whoDied = whoDied;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventoryItems) {
        this.inventory = inventoryItems;
    }

    public long getWhenDied() {
        return whenDied;
    }

    public void setWhenDied(long whenDied) {
        this.whenDied = whenDied;
    }

    public Map<UUID, List<ItemStack>> getItemsTaken() {
        return itemsTaken;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "DeathBody{" +
                "npc=" + npc +
                ", whoDied=" + whoDied +
                ", whenDied=" + whenDied +
                '}';
    }

}
