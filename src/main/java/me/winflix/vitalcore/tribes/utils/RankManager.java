package me.winflix.vitalcore.tribes.utils;

import me.winflix.vitalcore.tribes.models.Rank;

public class RankManager {
    public static final Rank OWNER_RANK = new Rank("owner", "&cOwner", true, 10);
    public static final Rank MEMBER_RANK = new Rank("member", "&aMember", true, 0);

    public static final Rank[] DEFAULT_RANKS = { OWNER_RANK, MEMBER_RANK };
}
