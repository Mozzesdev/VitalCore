package me.winflix.vitalcore.citizen.enums;

import me.winflix.vitalcore.citizen.utils.EnumUtil;

public enum EntityState implements EnumUtil.Maskable<EntityState> {

    DEFAULT(0x00),
    ON_FIRE(0x01),
    @Deprecated
    CROUCHING(0x02),
    @Deprecated
    UNUSED(0x04),
    SPRINTING(0x08),
    SWIMMING(0x10),
    INVISIBLE(0x20),
    GLOWING(0x40),
    FLYING(0x80);

    private final int mask;

    EntityState(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }
}