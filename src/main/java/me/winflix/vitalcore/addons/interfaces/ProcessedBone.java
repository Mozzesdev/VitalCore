package me.winflix.vitalcore.addons.interfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.joml.Vector3f;

/**
 * Representa un hueso procesado con la información mínima necesaria
 * para el runtime (jerarquía, transformación inicial).
 */
public class ProcessedBone {
    private String name;
    private Vector3f initialPivot;
    private Vector3f initialRotation;
    private ProcessedBone parent = null;
    private Set<ProcessedCube> cubes;
    private final Set<ItemGroup> groups;
    private List<ProcessedBone> children = new ArrayList<>();
    private Set<JavaItemModel> itemModels;
    private int scale;
    private String uuid;

    public ProcessedBone(String uuid, String name, Vector3f pivot, Vector3f rotation) {
        this.uuid = Objects.requireNonNull(uuid);
        this.name = Objects.requireNonNull(name);
        this.initialPivot = pivot;
        this.initialRotation = rotation;
        this.groups = new LinkedHashSet<ItemGroup>();
        this.cubes = new LinkedHashSet<ProcessedCube>();
        this.itemModels = new LinkedHashSet<JavaItemModel>();
    }

    public Set<ItemGroup> getGroups() {
        return groups;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getScale() {
        return this.scale;
    }

    public Set<JavaItemModel> getItemModels() {
        return itemModels;
    }

    public Vector3f getInitialPivot() {
        return initialPivot;
    }

    public Vector3f getInitialRotation() {
        return initialRotation;
    }

    public ProcessedBone getParent() {
        return parent;
    }

    public List<ProcessedBone> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public Set<ProcessedCube> getCubes() {
        return cubes;
    }

    public void setParent(ProcessedBone parent) {
        this.parent = parent;
    }

    public void addChild(ProcessedBone child) {
        if (child != null) {
            this.children.add(child);
            child.setParent(this);
        }
    }

    @Override
    public String toString() {
        return "ProcessedBone{" +
                ", name='" + name + '\'' +
                ", initialPivot=" + initialPivot +
                ", initialRotation=" + initialRotation +
                ", childrenCount=" + children.size() +
                '}';
    }

}
