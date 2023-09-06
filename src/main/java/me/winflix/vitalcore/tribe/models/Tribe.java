package me.winflix.vitalcore.tribe.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.winflix.vitalcore.tribe.utils.RankManager;

public class Tribe {
    String tribeName;
    String description;
    String tribeHome;
    String id;
    String tag;
    boolean open;
    ArrayList<Invitation> invitations;
    ArrayList<TribeMember> members;
    ArrayList<Rank> ranks;

    public Tribe() {
    }

    public Tribe(final String tribeName, final String id) {
        this.tribeName = tribeName;
        this.id = id;
        this.tag = "";
        this.description = "";
        this.tribeHome = "";
        this.open = true;
        this.members = new ArrayList<TribeMember>();
        this.ranks = new ArrayList<Rank>();
        this.invitations = new ArrayList<Invitation>();
        setDefaultRanks();
    }

    public String getTribeName() {
        return tribeName;
    }

    private void setDefaultRanks() {
        this.ranks.add(RankManager.MEMBER_RANK);
        this.ranks.add(RankManager.OWNER_RANK);
        this.ranks.add(RankManager.ADMIN_RANK);
    }

    public void setTribeName(final String name) {
        this.tribeName = name;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
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

    public ArrayList<Invitation> getInvitations() {
        return invitations;
    }

    public void setInvitations(final ArrayList<Invitation> invitations) {
        this.invitations = invitations;
    }

    public ArrayList<Invitation> addInvitation(Invitation inv) {
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

    public ArrayList<TribeMember> getMembers() {
        return members;
    }

    public TribeMember getMember(UUID id) {
        return getMembers().stream()
                .filter(tribeMember -> tribeMember.getId().equals(id.toString()))
                .findFirst()
                .orElse(null);
    }

    public TribeMember getDifferentMember(UUID id) {
        return getMembers().stream()
                .filter(tribeMember -> !tribeMember.getId().equals(id.toString()))
                .findFirst()
                .orElse(null);
    }

    public ArrayList<Rank> getRanks() {
        return ranks;
    }

    public void setRanks(final ArrayList<Rank> ranks) {
        this.ranks = ranks;
    }

    public ArrayList<TribeMember> addMember(TribeMember member) {
        members.add(member);
        return members;
    }

    public ArrayList<TribeMember> removeMember(TribeMember member) {
        members.remove(member);
        return members;
    }

    public List<TribeMember> replaceMember(UUID memberIdToReplace, TribeMember newMember) {
        members.replaceAll(tribeMember -> {
            if (tribeMember.getId().equals(memberIdToReplace.toString())) {
                return newMember;
            } else {
                return tribeMember;
            }
        });

        return members;
    }

    public void setMembers(final ArrayList<TribeMember> members) {
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
