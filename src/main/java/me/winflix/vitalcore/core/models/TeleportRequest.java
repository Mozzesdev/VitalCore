package me.winflix.vitalcore.core.models;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TeleportRequest {
    private final Player sender;
    private final Player target;
    private final long timestamp;
    private BukkitTask timeoutTask;

    public TeleportRequest(Player sender, Player target) {
        this.sender = sender;
        this.target = target;
        this.timestamp = System.currentTimeMillis();
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

}
