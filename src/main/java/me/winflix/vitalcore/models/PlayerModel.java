package me.winflix.vitalcore.models;

public class PlayerModel {
    String playerName;
    String id;
    String tribeId;
    TribeModel tribe;

    public PlayerModel() {
    }

    public PlayerModel(final String playerName, final String id,
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

    public TribeModel getTribe() {
        return tribe;
    }

    public void setTribe(TribeModel tribe) {
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
