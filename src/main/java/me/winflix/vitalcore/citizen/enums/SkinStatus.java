package me.winflix.vitalcore.citizen.enums;

import me.winflix.vitalcore.citizen.utils.EnumUtil;

public enum SkinStatus implements EnumUtil.Maskable<SkinStatus> {

    CAPE_ENABLED(0x01),
    JACKET_ENABLED(0x02),
    LEFT_SLEEVE_ENABLED(0x04),
    RIGHT_SLEEVE_ENABLED(0x08),
    LEFT_PANTS_LEG_ENABLED(0x10),
    RIGHT_PANTS_LEG_ENABLED(0x20),
    HAT_ENABLED(0x40),
    @Deprecated
    UNUSED(0x80);

    public static final SkinStatus[] ALL = {CAPE_ENABLED, JACKET_ENABLED, LEFT_SLEEVE_ENABLED,
            RIGHT_SLEEVE_ENABLED, LEFT_PANTS_LEG_ENABLED, RIGHT_PANTS_LEG_ENABLED, HAT_ENABLED};
    public final int mask;

    SkinStatus(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }
}