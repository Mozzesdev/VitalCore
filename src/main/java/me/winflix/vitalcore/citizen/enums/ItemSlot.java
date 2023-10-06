package me.winflix.vitalcore.citizen.enums;

import net.minecraft.world.entity.EquipmentSlot;

public enum ItemSlot {

    MAIN_HAND(EquipmentSlot.MAINHAND),
    OFF_HAND(EquipmentSlot.OFFHAND),
    BOOTS(EquipmentSlot.FEET),
    LEGGINGS(EquipmentSlot.LEGS),
    CHESTPLATE(EquipmentSlot.CHEST),
    HELMET(EquipmentSlot.HEAD);

    private final EquipmentSlot nmsItemSlot;

    ItemSlot(EquipmentSlot nmsItemSlot) {
        this.nmsItemSlot = nmsItemSlot;
    }

    public EquipmentSlot getNMSItemSlot() {
        return nmsItemSlot;
    }
}
