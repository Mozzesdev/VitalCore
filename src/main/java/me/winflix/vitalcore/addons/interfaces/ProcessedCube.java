package me.winflix.vitalcore.addons.interfaces;

import java.util.Map;

import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import me.winflix.vitalcore.addons.interfaces.JavaItemModel.JavaElement.Rotation;
import me.winflix.vitalcore.addons.utils.MathUtils;

public class ProcessedCube {
    private String name;
    private Vector3d from;
    private Vector3d to;
    private Vector3d origin;
    private Vector3d rotation;
    private Quaterniond rotQuartenion;
    private Map<Direction, Face> faces;
    private float inflate;

    public ProcessedCube(String name, Vector3dc from, Vector3dc to,
            Vector3dc pivot,
            Vector3d finalRotation,
            Map<Direction, Face> faces,
            float inflate) {
        this.name = name;
        this.from = new Vector3d(from);
        this.to = new Vector3d(to);
        this.origin = new Vector3d(pivot);
        this.rotation = new Vector3d(finalRotation);
        this.rotQuartenion = MathUtils.fromEulerZYX(finalRotation);
        this.faces = faces;
        this.inflate = inflate;
    }

    public String getName() {
        return name;
    }

    public Vector3d getFrom() {
        return new Vector3d(from);
    }

    public Vector3d getTo() {
        return new Vector3d(to);
    }

    public Vector3d getPivot() {
        return new Vector3d(origin);
    }

    public Vector3d getRotation() {
        return rotation;
    }

    public Quaterniond getRotQuartenion() {
        return rotQuartenion;
    }

    public Map<Direction, Face> getFaces() {
        return faces;
    }

    public float getInflate() {
        return inflate;
    }

    /**
     * Rota los puntos relativos y la rotación actual usando tipos double.
     * 
     * @param rot La rotación a aplicar (Quaterniond).
     */
    public void rotate(final Quaterniond rot) {
        this.from.sub((Vector3dc) this.origin);
        this.to.sub((Vector3dc) this.origin);
        this.origin.rotate((Quaterniondc) rot);
        this.rotQuartenion.premul((Quaterniondc) rot);
        this.rotation.set((Vector3dc) MathUtils.fixEuler(MathUtils.toEulerZYX(this.rotQuartenion)));
        this.from.add((Vector3dc) this.origin);
        this.to.add((Vector3dc) this.origin);
    }

    public Rotation rotation() {
        int zeros = 0;
        final float[] origin = MathUtils.unwrap(this.origin);
        final float[] unwrap;
        final float[] rotation = unwrap = MathUtils.unwrap(this.rotation);
        for (final float angle : unwrap) {
            zeros += ((angle == 0.0f) ? 1 : 0);
        }
        if (zeros == 3) {
            return null;
        }
        final Rotation javaRotation = new Rotation();
        final int i = MathUtils.absMax(rotation[0], rotation[1], rotation[2]);
        final Rotation rotation2 = javaRotation;
        rotation2.setAxis(switch (i) {
            case 1 -> "y";
            case 2 -> "z";
            default -> "x";
        });
        final float angle2 = Math.round(rotation[i] / 22.5f) * 22.5f;
        javaRotation.setAngle(angle2);
        javaRotation.origin(origin);
        return javaRotation;
    }

    /**
     * Devuelve una representación en String del ProcessedCube (double).
     * 
     * @return String representando el objeto ProcessedCube.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProcessedCube[");
        sb.append(", name='").append(name).append('\'');
        sb.append(", relativeFrom=").append(from);
        sb.append(", relativeTo=").append(to);
        sb.append(", relativePivot=").append(origin);
        sb.append(", currentRotation=").append(rotation);
        sb.append(", inflate=").append(inflate);
        sb.append(']');
        return sb.toString();
    }

    public record Face(UV uv, int texture) {
        public boolean isEmpty() {
            return MathUtils.isSimilar(this.uv.u1(), this.uv.u2()) || MathUtils.isSimilar(this.uv.v1, this.uv.v2);
        }
    }

    public record UV(float u1, float v1, float u2, float v2, int rotation) {
    }

    public enum Direction {
        NORTH(new Vector3d(0.0, 0.0, -1.0), new Vector3d(0.0, 1.0, 0.0).normalize()),
        EAST(new Vector3d(1.0, 0.0, 0.0), new Vector3d(0.0, 1.0, 0.0).normalize()),
        SOUTH(new Vector3d(0.0, 0.0, 1.0), new Vector3d(0.0, 1.0, 0.0).normalize()),
        WEST(new Vector3d(-1.0, 0.0, 0.0), new Vector3d(0.0, 1.0, 0.0).normalize()),
        UP(new Vector3d(0.0, 1.0, 0.0), new Vector3d(0.0, 0.0, -1.0).normalize()),
        DOWN(new Vector3d(0.0, -1.0, 0.0), new Vector3d(0.0, 0.0, 1.0).normalize());

        private final Vector3d normal;
        private final Vector3d uvUp;

        public Vector3d getNormal() {
            return new Vector3d((Vector3dc) this.normal);
        }

        public Vector3d getUvUp() {
            return new Vector3d((Vector3dc) this.uvUp);
        }

        public static Direction fromNormal(final Vector3d normal) {
            for (final Direction value : values()) {
                if (MathUtils.isSimilar(value.normal.dot((Vector3dc) normal), 1.0)) {
                    return value;
                }
            }
            return null;
        }

        private Direction(final Vector3d normal, final Vector3d uvUp) {
            this.normal = normal;
            this.uvUp = uvUp;
        }
    }

}
