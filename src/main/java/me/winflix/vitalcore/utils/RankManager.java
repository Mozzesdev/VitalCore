package me.winflix.vitalcore.utils;

import me.winflix.vitalcore.models.PlayerRank;

public class RankManager {
    public static final PlayerRank OWNER_RANK = new PlayerRank("owner", "[Owner]", true, 10);
    public static final PlayerRank ADMIN_RANK = new PlayerRank("admin", "[Admin]", true, 5);
    public static final PlayerRank MEMBER_RANK = new PlayerRank("member", "[Member]", true, 0);
}
