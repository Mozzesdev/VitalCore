package me.winflix.vitalcore.structures.interfaces;

public enum StructuresType {
    WALL("WALL"), FOUNDATION("FOUNDATION"), CEILING("CEILING"), DOOR_FRAME("DOOR_FRAME"), DOOR("DOOR"),
    WINDOW_FRAME("WINDOW_FRAME");

    private String type;

    StructuresType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
