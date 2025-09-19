package me.winflix.vitalcore.tribes.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.winflix.vitalcore.tribes.utils.RankManager;

public class Tribe {
    String tribeName;
    String description;
    String tribeHome;
    UUID id;
    String tag;
    boolean open;
    List<Invitation> invitations;
    List<TribeMember> members;
    List<Rank> ranks;

    public Tribe() {
        this.members = new ArrayList<>();
        this.ranks = new ArrayList<>();
        this.invitations = new ArrayList<>();
    }

    public Tribe(String tribeName, UUID id) {
        this();
        this.tribeName = tribeName;
        this.id = id;
        this.tag = "";
        this.description = "";
        this.tribeHome = "";
        this.open = true;
        setDefaultRanks();
    }

    public String getTribeName() {
        return tribeName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    private void setDefaultRanks() {
        this.ranks.add(RankManager.MEMBER_RANK);
        this.ranks.add(RankManager.OWNER_RANK);
    }

    public void setTribeName(final String name) {
        this.tribeName = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public List<Invitation> getInvitations() {
        return invitations;
    }

    public void setInvitations(final List<Invitation> invitations) {
        this.invitations = invitations;
    }

    public List<Invitation> addInvitation(Invitation inv) {
        invitations.add(inv);
        return invitations;
    }

    public boolean removeInvitation(Invitation inv) {
        boolean removed = invitations.remove(inv);
        return removed;
    }

    public void setDescription(final String des) {
        description = des;
    }

    public List<TribeMember> getMembers() {
        return members;
    }

    public TribeMember getMember(UUID id) {
        return getMembers().stream()
                .filter(tribeMember -> tribeMember.getPlayerId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public TribeMember getDifferentMember(UUID id) {
        return getMembers().stream()
                .filter(tribeMember -> !tribeMember.getPlayerId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Rank> getRanks() {
        return ranks;
    }

    public void setRanks(List<Rank> ranks) {
        this.ranks = ranks;
    }

    public List<TribeMember> addMember(TribeMember member) {
        members.add(member);
        return members;
    }

    public List<TribeMember> removeMember(TribeMember member) {
        members.remove(member);
        return members;
    }

    public List<TribeMember> replaceMember(UUID memberIdToReplace, TribeMember newMember) {
        members.replaceAll(tribeMember -> {
            if (tribeMember.getPlayerId().equals(memberIdToReplace)) {
                return newMember;
            } else {
                return tribeMember;
            }
        });

        return members;
    }

    public void setMembers(List<TribeMember> members) {
        this.members = members;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(final Boolean open) {
        this.open = open;
    }

    public String getTribeHome() {
        return tribeHome;
    }

    public void setTribeHome(final String home) {
        tribeHome = home;
    }

    @Override
    public String toString() {
        return "TribeModel [tribeName="
                + tribeName
                + ", id=" + id
                + ", tag=" + tag
                + ", description=" + description
                + ", members=" + members
                + ", open=" + open
                + ", tribeHome=" + tribeHome
                + ", invitations=" + invitations
                + "]";
    }

}
