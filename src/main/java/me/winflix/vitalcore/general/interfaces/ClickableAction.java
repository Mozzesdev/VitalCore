package me.winflix.vitalcore.general.interfaces;

import org.bukkit.entity.Player;

public interface ClickableAction {
    void action(Player sender, Player receiver, boolean confirmed);
}
