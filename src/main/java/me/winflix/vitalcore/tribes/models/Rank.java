package me.winflix.vitalcore.tribes.models;

import java.util.UUID;

public class Rank {
    UUID id;
    String name;
    String tag;
    boolean canInvite;
    int privilege;
    UUID tribeId;

    public Rank(String name, String tag, boolean canInvite, int privilege) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.canInvite = canInvite;
        this.privilege = privilege;
        this.tag = tag;
    }

    public Rank(String name, String tag, boolean canInvite, int privilege, UUID tribeId) {
        this(name, tag, canInvite, privilege);
        this.tribeId = tribeId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTribeId(UUID tribeId) {
        this.tribeId = tribeId;
    }

    public UUID getTribeId() {
        return tribeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public UUID getId() {
        return id;
    }

    public boolean canKick(TribeMember member) {
        return privilege > member.getRange().getPrivilege();
    }

    public boolean canPromote() {
        return privilege >= 10;
    }

    public boolean isCanInvite() {
        return canInvite;
    }

    public void setCanInvite(final Boolean canInvite) {
        this.canInvite = canInvite;
    }

    public int getPrivilege() {
        return privilege;
    }

    public void setPrivilege(final int privilege) {
        this.privilege = privilege;
    }

    @Override
    public String toString() {
        return "PlayerRank [id="
                + id.toString()
                + ", name=" + name
                + ", canInvite=" + canInvite
                + ", privilege=" + privilege
                + "]";
    }

}
