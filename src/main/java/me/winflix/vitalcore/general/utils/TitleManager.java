package me.winflix.vitalcore.general.utils;

import org.bukkit.entity.Player;

public class TitleManager {

    public static void sendTitle(Player player, String titleText, String subtitleText, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(Utils.useColors(titleText), Utils.useColors(subtitleText), fadeIn, stay, fadeOut);
    }

    public static void sendTitle(Player player, String titleText, String subtitleText) {
        sendTitle(player, titleText, subtitleText, 10, 20, 10);
    }

    public static void sendTitle(Player player, String titleText) {
        sendTitle(player, titleText, "", 10, 20, 10);
    }

}