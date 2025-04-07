package me.winflix.vitalcore.tribes.models;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class Invitation {
    public enum InvitationType {
        TRIBE_TO_PLAYER,
        PLAYER_TO_TRIBE
    }

    private InvitationType type;
    private UUID senderId;
    private UUID targetId;
    private Date createdAt;
    private UUID id;

    public Invitation() {
    }

    // Constructor para invitación TRIBE → PLAYER
    public Invitation(UUID senderTribeId, UUID targetPlayerId) {
        this.id = UUID.randomUUID();
        this.type = InvitationType.TRIBE_TO_PLAYER;
        this.senderId = senderTribeId;
        this.targetId = targetPlayerId;
        this.createdAt = new Date();
    }

    // Constructor para solicitud PLAYER → TRIBE
    public Invitation(UUID senderPlayerId, UUID targetTribeId, boolean isPlayerRequest) {
        this.type = InvitationType.PLAYER_TO_TRIBE;
        this.senderId = senderPlayerId;
        this.targetId = targetTribeId;
        this.createdAt = new Date();
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public InvitationType getType() {
        return type;
    }

    public void setType(InvitationType type) {
        this.type = type;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format(
                "Invitation [%s] Sender: %s | Target: %s",
                type,
                senderId.toString().substring(0, 8),
                targetId.toString().substring(0, 8));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Invitation that = (Invitation) o;
        return type == that.type &&
                senderId.equals(that.senderId) &&
                targetId.equals(that.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, senderId, targetId);
    }
}
