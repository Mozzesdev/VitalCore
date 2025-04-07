package me.winflix.vitalcore.tribes.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.tribes.models.Invitation;

public class InvitationsManager {
    private static final Map<UUID, List<Invitation>> playerInvitations = new ConcurrentHashMap<>();
    private static final Map<UUID, List<Invitation>> tribeInvitations = new ConcurrentHashMap<>();
    private static final long EXPIRATION_TIME = 60_000;
    private static final Map<UUID, BukkitTask> expirationTasks = new ConcurrentHashMap<>();

    public static void addInvitation(Invitation invitation) {
        Objects.requireNonNull(invitation, "La invitación no puede ser nula");

        // Validar IDs
        if (invitation.getSenderId() == null || invitation.getTargetId() == null) {
            throw new IllegalArgumentException("SenderId y TargetId no pueden ser nulos");
        }

        // Agregar la invitación
        if (invitation.getType() == Invitation.InvitationType.TRIBE_TO_PLAYER) {
            playerInvitations
                    .computeIfAbsent(invitation.getTargetId(), k -> new CopyOnWriteArrayList<>())
                    .add(invitation);

            tribeInvitations
                    .computeIfAbsent(invitation.getSenderId(), k -> new CopyOnWriteArrayList<>())
                    .add(invitation);
        } else if (invitation.getType() == Invitation.InvitationType.PLAYER_TO_TRIBE) {
            playerInvitations
                    .computeIfAbsent(invitation.getSenderId(), k -> new CopyOnWriteArrayList<>())
                    .add(invitation);

            tribeInvitations
                    .computeIfAbsent(invitation.getTargetId(), k -> new CopyOnWriteArrayList<>())
                    .add(invitation);
        }

        // Programar expiración
        scheduleExpiration(invitation);
    }

    public static void removeInvitation(Invitation invitation) {
        Objects.requireNonNull(invitation, "La invitación no puede ser nula");

        playerInvitations.getOrDefault(invitation.getTargetId(), new CopyOnWriteArrayList<>()).remove(invitation);

        tribeInvitations.getOrDefault(invitation.getSenderId(), new CopyOnWriteArrayList<>()).remove(invitation);

        // Cancelar la tarea de expiración si existe
        BukkitTask task = expirationTasks.remove(invitation.getId());
        if (task != null) {
            task.cancel();
        }
    }

    private static void notifyExpiration(Invitation invitation) {
        Player target = Bukkit.getPlayer(invitation.getTargetId());
        if (target != null && target.isOnline()) {
            target.sendMessage("Tu invitación ha expirado!");
        }
    }

    public static List<Invitation> getPlayerInvitations(UUID playerId) {
        return Collections.unmodifiableList(
                playerInvitations.getOrDefault(playerId, new ArrayList<>()));
    }

    public static List<Invitation> getTribeInvitations(UUID tribeId) {
        return Collections.unmodifiableList(
                tribeInvitations.getOrDefault(tribeId, new ArrayList<>()));
    }

    public static boolean hasInvitation(UUID playerId, UUID tribeId) {
        return playerInvitations.getOrDefault(playerId, new ArrayList<>())
                .stream()
                .anyMatch(inv -> inv.getType() == Invitation.InvitationType.TRIBE_TO_PLAYER &&
                        inv.getSenderId().equals(tribeId));
    }

    public static boolean hasPendingRequest(UUID playerId, UUID tribeId) {
        return playerInvitations.getOrDefault(playerId, new ArrayList<>())
                .stream()
                .anyMatch(inv -> inv.getType() == Invitation.InvitationType.PLAYER_TO_TRIBE &&
                        inv.getTargetId().equals(tribeId));
    }

    public static void cleanupExpiredInvitations() {
        long now = System.currentTimeMillis();

        playerInvitations.forEach(
                (playerId, invites) -> invites.removeIf(inv -> now - inv.getCreatedAt().getTime() > EXPIRATION_TIME));

        tribeInvitations.forEach(
                (tribeId, invites) -> invites.removeIf(inv -> now - inv.getCreatedAt().getTime() > EXPIRATION_TIME));
    }

    private static void scheduleExpiration(Invitation invitation) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(VitalCore.getPlugin(), () -> {
            removeInvitation(invitation);
            notifyExpiration(invitation);
        }, 20L * 60);

        expirationTasks.put(invitation.getId(), task);
    }

    public static int getTotalPlayerInvitations(UUID playerId) {
        return playerInvitations.getOrDefault(playerId, new ArrayList<>()).size();
    }

    public static int getTotalTribeInvitations(UUID tribeId) {
        return tribeInvitations.getOrDefault(tribeId, new ArrayList<>()).size();
    }
}