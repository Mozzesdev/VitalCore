package me.winflix.vitalcore.addons.interfaces;

import java.util.Map;
import java.util.Objects;

/**
 * Representa la estructura procesada y lista para usar en runtime de un modelo
 * BbModel.
 * Separa los datos crudos de los datos necesarios para la lógica del juego.
 */
public class ProcessedBbModel {
    private final String name;
    private final Map<String, ProcessedBone> bones;
    private final ProcessedPackData packData;

    public ProcessedBbModel(String name, Map<String, ProcessedBone> bones, ProcessedPackData packData) {
        this.name = Objects.requireNonNull(name);
        this.bones = Objects.requireNonNull(bones);
        this.packData = packData;
    }

    public String getName() {
        return name;
    }

    public ProcessedPackData getPackData() {
        return packData;
    }

    public Map<String, ProcessedBone> getBones() {
        return bones;
    }

    @Override
    public String toString() {
        return "ProcessedBbModel{" +
                "name='" + name + '\'' +
                ", bones=" + bones +
                '}';
    }
}
