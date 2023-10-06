package me.winflix.vitalcore.citizen.enums;

import me.winflix.vitalcore.citizen.utils.EnumUtil;
import net.minecraft.world.InteractionHand;

public enum Hand implements EnumUtil.BiIdentifiable<InteractionHand, Integer> {

    OFF_HAND(InteractionHand.OFF_HAND, 1),
    MAIN_HAND(InteractionHand.MAIN_HAND, 0);

    private final InteractionHand id;
    private final int type;

    Hand(InteractionHand id, int type) {
        this.id = id;
        this.type = type;
    }

    public Integer getSecondID() {
        return type;
    }

    public InteractionHand getID() {
        return id;
    }

}