package me.winflix.vitalcore.models;

public class TribeMember {
    String playerName;
    String id;
    PlayerRank range;

    public TribeMember() {
    }

    public TribeMember(final String playerName, final String id) {
        this.playerName = playerName;
        this.id = id;
    }

    public String getPlayerName() {
        return this.playerName;
    };

    public void setPlayerName(final String name) {
        this.playerName = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public PlayerRank getRange() {
        return range;
    }

    public void setRange(final PlayerRank range) {
        this.range = range;
    }

    @Override
    public String toString() {
        return "TribeMember [playerName="
                + playerName
                + ", id=" + id
                + ", range=" + range
                + "]";
    }
}
