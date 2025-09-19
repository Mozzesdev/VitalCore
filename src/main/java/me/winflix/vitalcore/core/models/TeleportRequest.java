package me.winflix.vitalcore.core.models;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TeleportRequest {
    private final Player sender;
    private final Player target;
    private final long timestamp;
    private final boolean isTpaHere; // true = tpahere, false = tpa normal
    private BukkitTask timeoutTask;

    public TeleportRequest(Player sender, Player target) {
        this.sender = sender;
        this.target = target;
        this.timestamp = System.currentTimeMillis();
        this.isTpaHere = false; // Por defecto es tpa normal
    }

    public TeleportRequest(Player sender, Player target, boolean isTpaHere) {
        this.sender = sender;
        this.target = target;
        this.timestamp = System.currentTimeMillis();
        this.isTpaHere = isTpaHere;
    }

    public Player getSender() {
        return sender;
    }

    public Player getTarget() {
        return target;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimeoutTask(BukkitTask timeoutTask) {
        this.timeoutTask = timeoutTask;
    }

    public BukkitTask getTimeoutTask() {
        return timeoutTask;
    }

    public boolean isTpaHere() {
        return isTpaHere;
    }

}
