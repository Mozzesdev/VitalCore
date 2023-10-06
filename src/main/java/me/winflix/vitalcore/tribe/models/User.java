package me.winflix.vitalcore.tribe.models;

import me.winflix.vitalcore.skins.models.Skin;

import java.util.ArrayList;

public class User {
    String playerName;
    String id;
    String tribeId;
    Tribe tribe;
    ArrayList<Invitation> invitations;
    Skin skin;

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

    public boolean addInvitation(Invitation inv) {
        return invitations.add(inv);
    }

    public boolean removeInvitation(Invitation inv) {
        return invitations.remove(inv);
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

    public Skin getSkin() {
        return skin;
    }

    public void setSkin(final Skin skin) {
        this.skin = skin;
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
