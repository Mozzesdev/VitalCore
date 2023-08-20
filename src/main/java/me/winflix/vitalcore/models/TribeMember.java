package me.winflix.vitalcore.models;

public class TribeMember {
    String playerName;
    String id;
    String range;

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

    public String getRange() {
        return this.range;
    }

    public void setRange(final String range) {
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
