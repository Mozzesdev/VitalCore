package me.winflix.vitalcore.tribe.models;

import java.util.Date;

public class Invitation {
    private String senderTribeId;
    private String targetUserId;
    private Date createdAt;

    public Invitation() {
    }

    public Invitation(final String senderTribeId, final String targetUserId) {
        this.senderTribeId = senderTribeId;
        this.targetUserId = targetUserId;
        this.createdAt = new Date();
    }

    public String getSenderTribeId() {
        return senderTribeId;
    }

    public void setSenderTribeId(final String senderTribeId) {
        this.senderTribeId = senderTribeId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(final String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

}
