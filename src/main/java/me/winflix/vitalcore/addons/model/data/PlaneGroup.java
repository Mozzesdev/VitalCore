package me.winflix.vitalcore.addons.model.data;

import java.text.NumberFormat;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.winflix.vitalcore.addons.utils.MathUtils;

public record PlaneGroup(Vector3d axis, int modAngle, Quaterniond origin, Quaterniond invOrigin, IntSet cubes) {
    @Override
    public String toString() {
        return "PlaneGroup{axis=" + String.valueOf(this.axis) + ", modAngle=" + this.modAngle + ", origin="
                + MathUtils.toEulerZYX(this.origin).toString(NumberFormat.getInstance()) + ", invOrigin="
                + MathUtils.toEulerZYX(this.invOrigin).toString(NumberFormat.getInstance()) + ", cubes="
                + String.valueOf(this.cubes);
    }

    public String toString(final Int2ObjectMap<ProcessedCube> map) {
        return "PlaneGroup{axis=" + String.valueOf(this.axis) + ", modAngle=" + this.modAngle + ", origin="
                + MathUtils.toEulerZYX(this.origin).toString(NumberFormat.getInstance()) + ", invOrigin="
                + MathUtils.toEulerZYX(this.invOrigin).toString(NumberFormat.getInstance()) + ", cubes="
                + String.valueOf(map.int2ObjectEntrySet().stream()
                        .filter(cubeEntry -> this.cubes.contains(cubeEntry.getIntKey())).toList());
    }
}
