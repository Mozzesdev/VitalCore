package me.winflix.vitalcore.tribe.utils;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.TribeMember;

public class TribeUtils {
    
    public static void castMessageToAllMembersTribe(Tribe tribe, String message) {
        List<TribeMember> membersTribe = tribe.getMembers();
        for (TribeMember member : membersTribe) {
            Player memberPlayer = Bukkit.getPlayer(UUID.fromString(member.getId()));
            if (memberPlayer.isOnline()) {
                Utils.infoMessage(memberPlayer, message);
            }
        }
    }
}
