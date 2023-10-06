package me.winflix.vitalcore.citizen.enums;

import me.winflix.vitalcore.citizen.utils.EnumUtil;

public enum InteractType implements EnumUtil.Identifiable<String> {

    RIGHT_CLICK("INTERACT"),
    LEFT_CLICK("ATTACK"),
    RIGHT_CLICK_AT("INTERACT_AT");

    private final String id;

    InteractType(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }
}