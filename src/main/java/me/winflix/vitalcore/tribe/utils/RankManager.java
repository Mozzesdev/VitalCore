package me.winflix.vitalcore.tribe.utils;
import me.winflix.vitalcore.tribe.models.Rank;

public class RankManager {
    public static final Rank OWNER_RANK = new Rank("owner", "&7[&6Owner&7]", true, 10);
    public static final Rank ADMIN_RANK = new Rank("admin", "&7[&cAdmin&7]", true, 5);
    public static final Rank MEMBER_RANK = new Rank("member", "&7[&aMember&7]", true, 0);
}
