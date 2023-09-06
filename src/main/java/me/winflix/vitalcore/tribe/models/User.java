package me.winflix.vitalcore.tribe.models;

import java.util.ArrayList;

public class User {
    String playerName;
    String id;
    String tribeId;
    Tribe tribe;
    ArrayList<Invitation> invitations;

    public User() {
    }

    public User(final String playerName, final String id,
            final String tribeId) {
        this.playerName = playerName;
        this.id = id;
        this.tribeId = tribeId;
        this.invitations = new ArrayList<Invitation>();
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(final String name) {
        this.playerName = name;
    }

    public String getId() {
        return this.id;
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

    public Tribe getTribe() {
        return tribe;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTribeId() {
        return this.tribeId;
    }

    public void setTribeId(final String id) {
        this.tribeId = id;
    }

    @Override
    public String toString() {
        return "PlayerModel [player="
                + playerName
                + ", id=" + id
                + ", tribeId=" + tribeId
                + ", invitations=" + invitations
                + "]";
    }

}
