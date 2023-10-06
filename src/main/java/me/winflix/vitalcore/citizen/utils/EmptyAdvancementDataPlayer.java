package me.winflix.vitalcore.citizen.utils;

import com.mojang.datafixers.DataFixer;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.core.nms.NMS;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmptyAdvancementDataPlayer extends PlayerAdvancements {
    public EmptyAdvancementDataPlayer(DataFixer datafixer, PlayerList playerlist,
                                      ServerAdvancementManager advancementdataworld, File file, ServerPlayer entityplayer) {
        super(datafixer, playerlist, advancementdataworld, VitalCore.getPlugin().getDataFolder().toPath(), entityplayer);
        this.save();
    }

    @Override
    public boolean award(Advancement advancement, String s) {
        return false;
    }

    @Override
    public void flushDirty(ServerPlayer entityplayer) {
    }

    @Override
    public AdvancementProgress getOrStartProgress(Advancement advancement) {
        return new AdvancementProgress();
    }

    @Override
    public boolean revoke(Advancement advancement, String s) {
        return false;
    }

    @Override
    public void save() {
        clear(this);
    }

    @Override
    public void setPlayer(ServerPlayer entityplayer) {
    }

    @Override
    public void setSelectedTab(Advancement advancement) {
    }

    @Override
    public void stopListening() {
    }

    public static void clear(PlayerAdvancements data) {
        data.stopListening();
        try {
            ((Map<?, ?>) PROGRESS.invoke(data)).clear();
            for (MethodHandle handle : SETS) {
                ((Set<?>) handle.invoke(data)).clear();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static final MethodHandle PROGRESS = NMS.getFirstGetter(PlayerAdvancements.class, Map.class);
    private static final List<MethodHandle> SETS = NMS.getFieldsOfType(PlayerAdvancements.class, Set.class);
}
