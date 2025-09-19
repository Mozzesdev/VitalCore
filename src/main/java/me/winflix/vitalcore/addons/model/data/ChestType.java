package me.winflix.vitalcore.addons.model.data;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public class ChestType {
    private final String modelId;
    private final String idleAnim;
    private final String openAnim;
    private final String closeAnim;
    private final List<ItemStack> rewards;

    public ChestType(String modelId, String idleAnim, String openAnim, String closeAnim, List<ItemStack> rewards) {
        this.modelId = modelId;
        this.idleAnim = idleAnim;
        this.openAnim = openAnim;
        this.closeAnim = closeAnim;
        this.rewards = rewards;
    }

    public String getCloseAnim() {
        return closeAnim;
    }

    public String getIdleAnim() {
        return idleAnim;
    }

    public String getModelId() {
        return modelId;
    }

    public String getOpenAnim() {
        return openAnim;
    }

    public List<ItemStack> getRewards() {
        return rewards;
    }

}