package me.winflix.vitalcore.tribes.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribes.models.Tribe;
import me.winflix.vitalcore.tribes.models.TribeMember;

public class TribeUtils {

    public static void castMessageToAllMembersTribe(Tribe tribe, String message) {
        List<TribeMember> membersTribe = tribe.getMembers();
        for (TribeMember member : membersTribe) {
            Player memberPlayer = Bukkit.getPlayer(member.getPlayerId());
            if (memberPlayer.isOnline()) {
                Utils.infoMessage(memberPlayer, message);
            }
        }
    }
}
