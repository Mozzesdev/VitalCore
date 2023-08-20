package me.winflix.vitalcore.interfaces;

import org.bukkit.entity.Player;

public interface ConfirmationHandler {
    void handleConfirmation(Player sender, Player receiver, boolean confirmed);
}