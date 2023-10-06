package me.winflix.vitalcore.skins.models;

import java.util.UUID;

public class Skin {

    private UUID playerUUID; // UUID of user
    private String skinURL; // URL of the skin

    public Skin() {
    }

    public Skin(final UUID playerUUID, final String skinURL) {
        this.playerUUID = playerUUID;
        this.skinURL = skinURL;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getSkinURL() {
        return skinURL;
    }

    public void setSkinURL(final String skinURL) {
        this.skinURL = skinURL;
    }

}
