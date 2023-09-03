package me.winflix.vitalcore.tribe.models;

public class User {
    String playerName;
    String id;
    String tribeId;
    Tribe tribe;

    public User() {
    }

    public User(final String playerName, final String id,
            final String tribeId) {
        this.playerName = playerName;
        this.id = id;
        this.tribeId = tribeId;
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
                + "]";
    }

}