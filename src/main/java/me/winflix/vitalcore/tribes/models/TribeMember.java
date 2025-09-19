package me.winflix.vitalcore.tribes.models;

import java.util.UUID;

public class TribeMember {
    String playerName;
    UUID playerId;
    Rank range;
    UUID tribeId;

    public TribeMember() {
    }

    public TribeMember(String playerName, UUID id, UUID tribeId) {
        this.playerName = playerName;
        this.playerId = id;
        this.tribeId = tribeId;
    }

    public UUID getTribeId() {
        return tribeId;
    }

    public String getPlayerName() {
        return this.playerName;
    };

    public void setPlayerName(final String name) {
        this.playerName = name;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(final UUID id) {
        this.playerId = id;
    }

    public Rank getRange() {
        return range;
    }

    public void setRange(final Rank range) {
        this.range = range;
    }

    @Override
    public String toString() {
        return "TribeMember [playerName="
                + playerName
                + ", id=" + playerId
                + ", range=" + range
                + "]";
    }
}
