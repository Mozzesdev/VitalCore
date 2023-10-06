package me.winflix.vitalcore.citizen.models;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class Body {
    private ServerPlayer npc;
    private UUID whoDied;
    private Inventory inventory;
    private long whenDied;
    private List<ArmorStand> armorStandList;
    private Map<UUID, List<ItemStack>> itemsTaken;

    public Body() {
        this.armorStandList = new ArrayList<>();
        this.itemsTaken = new HashMap<>();
    }

    public Body(ServerPlayer npc, UUID whoDied, Inventory inventory, long whenDied) {
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

    public List<ArmorStand> getArmorStandList() {
        return armorStandList;
    }

    public Map<UUID, List<ItemStack>> getItemsTaken() {
        return itemsTaken;
    }

    @Override
    public String toString() {
        return "Body{" +
                "npc=" + npc +
                ", whoDied=" + whoDied +
                ", whenDied=" + whenDied +
                '}';
    }
}
