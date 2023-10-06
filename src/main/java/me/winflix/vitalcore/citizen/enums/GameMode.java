package me.winflix.vitalcore.citizen.enums;

import net.minecraft.world.level.GameType;

public enum GameMode {

    SURVIVAL(GameType.SURVIVAL),
    CREATIVE(GameType.CREATIVE),
    ADVENTURE(GameType.ADVENTURE),
    SPECTATOR(GameType.SPECTATOR);

    private final GameType nmsGameMode;

    GameMode(GameType nmsGameMode) {
        this.nmsGameMode = nmsGameMode;
    }

    public GameType getNMSGameMode() {
        return nmsGameMode;
    }
}
