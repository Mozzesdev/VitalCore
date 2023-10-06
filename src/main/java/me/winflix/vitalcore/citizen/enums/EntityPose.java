package me.winflix.vitalcore.citizen.enums;

import me.winflix.vitalcore.citizen.utils.EnumUtil;
import net.minecraft.world.entity.Pose;

public enum EntityPose implements EnumUtil.Identifiable<Pose> {

    STANDING(Pose.STANDING),
    FALL_FLYING(Pose.FALL_FLYING),
    SLEEPING(Pose.SLEEPING),
    SWIMMING(Pose.SWIMMING),
    SPIN_ATTACK(Pose.SPIN_ATTACK),
    CROUCHING(Pose.CROUCHING),
    LONG_JUMPING(Pose.LONG_JUMPING),
    DYING(Pose.DYING),
    CROAKING(Pose.CROAKING),
    ROARING(Pose.ROARING),
    SNIFFING(Pose.SNIFFING),
    EMERGING(Pose.EMERGING),
    DIGGING(Pose.DIGGING);

    private final Pose nmsPose;

    EntityPose(Pose nmsPose) {
        this.nmsPose = nmsPose;
    }

    public Pose getID() {
        return nmsPose;
    }

}
