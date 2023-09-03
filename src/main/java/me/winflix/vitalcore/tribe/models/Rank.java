package me.winflix.vitalcore.tribe.models;
import me.winflix.vitalcore.tribe.utils.RankManager;

public class Rank {
    String name;
    String displayName;
    boolean canInvite;
    int privilege;

    public Rank() {
    }

    public Rank(final String name, final String displayName,
            final boolean canInvite, final int privilege) {
        this.name = name;
        this.displayName = displayName;
        this.canInvite = canInvite;
        this.privilege = privilege;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public boolean canKick(TribeMember member) {
        return privilege > member.getRange().getPrivilege();
    }

    public boolean canPromote() {
        return privilege >= RankManager.ADMIN_RANK.getPrivilege();
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
        return "PlayerRank [name="
                + name
                + ", displayName=" + displayName
                + ", canInvite=" + canInvite
                + ", privilege=" + privilege
                + "]";
    }

}