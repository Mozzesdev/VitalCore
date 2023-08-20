package me.winflix.vitalcore.models;

public class BlockModel {
    private int x;
    private int y;
    private int z;
    private String world;

    public BlockModel() {
    }

    public BlockModel(final int x, final int y, final int z,
            final String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public int getX() {
        return this.x;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public int getZ() {
        return this.z;
    }

    public void setZ(final int z) {
        this.z = z;
    }

    public String getWorld() {
        return this.world;
    }

    public void setWorld(final String world) {
        this.world = world;
    }

    @Override
    public String toString() {
        return "BlockModel [x="
                + x
                + ", y=" + y
                + ", z=" + z
                + ", world=" + world
                + "]";
    }

}
