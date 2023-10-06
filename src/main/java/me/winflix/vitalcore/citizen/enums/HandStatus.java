package me.winflix.vitalcore.citizen.enums;

import me.winflix.vitalcore.citizen.utils.EnumUtil;

public enum HandStatus implements EnumUtil.Maskable<HandStatus> {

    MAIN_HAND(0x00),
    HAND_ACTIVE(0x01),
    OFF_HAND(0x02),
    RIPTIDE_SPIN_ATTACK(0x04);

    private final int mask;

    HandStatus(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }
}