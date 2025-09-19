package me.winflix.vitalcore.addons.model.data;

import java.util.Objects;

import org.joml.Vector3d;

public class HashedVector3d extends Vector3d {
    private static final int PRECISION = 10000;
    private final Axis axis;

    public HashedVector3d(final Axis axis, final double x, final double y, final double z) {
        super(x, y, z);
        this.axis = axis;
    }

    public HashedVector3d() {
        this.axis = Axis.X;
    }

    public int hashCode() {
        return Objects.hash(this.axis, (int) Math.round(this.x * 10000.0), (int) Math.round(this.y * 10000.0),
                (int) Math.round(this.z * 10000.0));
    }

    public boolean equals(final Object obj) {
        return this == obj || (obj != null && this.getClass() == obj.getClass() && this.hashCode() == obj.hashCode());
    }
}