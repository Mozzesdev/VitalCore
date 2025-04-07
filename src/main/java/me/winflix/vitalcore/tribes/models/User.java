package me.winflix.vitalcore.tribes.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    String playerName;
    UUID id;
    List<Invitation> invitations;
    Tribe tribe;

    public User(String playerName, UUID id, Tribe tribe) {
        this.playerName = playerName;
        this.id = id;
        this.invitations = new ArrayList<Invitation>();
        this.tribe = tribe;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    public Tribe getTribe() {
        return tribe;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(final String name) {
        this.playerName = name;
    }

    public UUID getId() {
        return this.id;
    }

    public List<Invitation> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<Invitation> invitations) {
        this.invitations = invitations;
    }

    public boolean addInvitation(Invitation inv) {
        return invitations.add(inv);
    }

    public boolean removeInvitation(Invitation inv) {
        return invitations.remove(inv);
    }

    @Override
    public String toString() {
        return "PlayerModel [player="
                + playerName
                + ", id=" + id
                + ", tribeId=" + tribe.getId()
                + ", invitations=" + invitations
                + "]";
    }

}
