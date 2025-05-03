package me.winflix.vitalcore.addons.solvers;

import java.util.*;

import org.joml.*;
import org.joml.Math;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import me.winflix.vitalcore.addons.interfaces.Axis;
import me.winflix.vitalcore.addons.interfaces.HashedVector3d;
import me.winflix.vitalcore.addons.interfaces.ItemGroup;
import me.winflix.vitalcore.addons.interfaces.PlaneGroup;
import me.winflix.vitalcore.addons.interfaces.ProcessedCube;
import me.winflix.vitalcore.addons.utils.MathUtils;

public class RotationSolver {
    private final Int2ObjectMap<ProcessedCube> cubes = new Int2ObjectOpenHashMap<ProcessedCube>();
    private final Int2ObjectMap<List<Vector3d>> axes = new Int2ObjectOpenHashMap<List<Vector3d>>();

    private static final List<Vector3d> CARDINAL_AXES = new ArrayList<Vector3d>() {
        {
            this.add(new Vector3d(1.0, 0.0, 0.0));
            this.add(new Vector3d(0.0, 1.0, 0.0));
            this.add(new Vector3d(0.0, 0.0, 1.0));
            this.add(new Vector3d(-1.0, 0.0, 0.0));
            this.add(new Vector3d(0.0, -1.0, 0.0));
            this.add(new Vector3d(0.0, 0.0, -1.0));
        }
    };

    public static void solve(final Collection<ItemGroup> result, final Collection<ProcessedCube> cubes) {
        final RotationSolver solver = new RotationSolver();
        solver.initialize(result, cubes);
        solver.solve(result);
    }

    private void initialize(final Collection<ItemGroup> result, final Collection<ProcessedCube> collection) {
        int cubeId = 0;
        final ArrayList<ProcessedCube> list = new ArrayList<ProcessedCube>();
        for (final ProcessedCube cube : collection) {
            if (isLegal(cube)) {
                list.add(IllegalRotationSolver.solve(cube));
            } else {
                this.cubes.put(cubeId++, cube);
            }
        }
        if (!list.isEmpty()) {
            result.add(new ItemGroup(new Quaterniond(), new Vector3d(), list));
        }
    }

    private void solve(final Collection<ItemGroup> result) {
        final Map<Vector3d, IntLinkedOpenHashSet> axisGroup = this.groupByAxis();
        final List<PlaneGroup> moduloGroup = this.groupByModulo(axisGroup);
        final Map<PlaneGroup, Set<PlaneGroup>> planeGroup = this.combineGroup(moduloGroup);
        this.fixGroups(result, planeGroup);
    }

    private void simpleConvert(final Collection<ItemGroup> result, final List<PlaneGroup> moduloGroup) {
        for (final PlaneGroup plane : moduloGroup) {
            final ArrayList<ProcessedCube> list = new ArrayList<ProcessedCube>();
            this.rotateCubes(list, plane.cubes(), plane.invOrigin());
            result.add(new ItemGroup(new Quaterniond((Quaterniondc) plane.origin()),
                    MathUtils.toEulerXYZ(plane.origin()), list));
        }
    }

    private List<Vector3d> toAxes(final int cubeId) {
        final ProcessedCube cube = (ProcessedCube) this.cubes.get(cubeId);
        final Vector3d px = MathUtils
                .fixVector(new HashedVector3d(Axis.X, 1.0, 0.0, 0.0).rotate((Quaterniondc) cube.getRotQuartenion()));
        final Vector3d py = MathUtils
                .fixVector(new HashedVector3d(Axis.Y, 0.0, 1.0, 0.0).rotate((Quaterniondc) cube.getRotQuartenion()));
        final Vector3d pz = MathUtils
                .fixVector(new HashedVector3d(Axis.Z, 0.0, 0.0, 1.0).rotate((Quaterniondc) cube.getRotQuartenion()));
        return Arrays.asList(px, py, pz);
    }

    private Map<Vector3d, IntLinkedOpenHashSet> groupByAxis() {
        final Object2ObjectLinkedOpenHashMap<Vector3d, IntLinkedOpenHashSet> map = new Object2ObjectLinkedOpenHashMap<>();
        for (final int key : this.cubes.keySet()) {
            final List<Vector3d> axes = this.toAxes(key);
            this.axes.put(key, axes);
            for (final Vector3d axis : axes) {
                ((IntLinkedOpenHashSet) map.computeIfAbsent(axis, vector3d -> new IntLinkedOpenHashSet()))
                        .add(key);
            }
        }
        return fetch((Map<Vector3d, IntLinkedOpenHashSet>) map);
    }

    private List<PlaneGroup> groupByModulo(final Map<Vector3d, IntLinkedOpenHashSet> byAxis) {
        final Object2ObjectLinkedOpenHashMap<Vector3d, Int2ObjectMap<IntSet>> temp = new Object2ObjectLinkedOpenHashMap<>();
        final ArrayList<PlaneGroup> result = new ArrayList<PlaneGroup>();
        for (final Map.Entry<Vector3d, IntLinkedOpenHashSet> entry : byAxis.entrySet()) {
            Quaterniond toOrigin = null;
            for (final int cubeId : entry.getValue()) {
                final ProcessedCube cube = (ProcessedCube) this.cubes.get(cubeId);
                double angle;
                if (toOrigin == null) {
                    toOrigin = cube.getRotQuartenion().invert(new Quaterniond());
                    angle = 0.0;
                } else {
                    final Quaterniond localQ = toOrigin.mul((Quaterniondc) cube.getRotQuartenion(), new Quaterniond());
                    final Vector3d lqVec = new Vector3d(localQ.x, localQ.y, localQ.z);
                    final double sign = Math.signum(lqVec.dot((Vector3dc) entry.getKey()));
                    angle = sign * 2.0 * Math.acos(localQ.w) * 57.29577951308232;
                }
                final int groupId = getGroupId(angle);
                ((IntSet) (temp.computeIfAbsent(entry.getKey(),
                        vector3d -> new Int2ObjectLinkedOpenHashMap<IntSet>()))
                        .computeIfAbsent(groupId, integer -> new IntLinkedOpenHashSet())).add(cubeId);
            }
        }
        for (final Map.Entry<Vector3d, Int2ObjectMap<IntSet>> group : temp.entrySet()) {
            for (final Int2ObjectMap.Entry<IntSet> subGroup : group.getValue().int2ObjectEntrySet()) {
                final int firstCubeId = ((IntSet) subGroup.getValue()).iterator().nextInt();
                final Quaterniond rotation = ((ProcessedCube) this.cubes.get(firstCubeId)).getRotQuartenion();
                result.add(new PlaneGroup(new Vector3d((Vector3dc) group.getKey()), subGroup.getIntKey(),
                        new Quaterniond((Quaterniondc) rotation), rotation.invert(new Quaterniond()),
                        (IntSet) subGroup.getValue()));
            }
        }
        return result;
    }

    private Map<PlaneGroup, Set<PlaneGroup>> combineGroup(final List<PlaneGroup> planes) {
        final Object2ObjectLinkedOpenHashMap<PlaneGroup, Set<PlaneGroup>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (int a = 0; a < planes.size(); ++a) {
            final PlaneGroup groupA = planes.get(a);
            final Set<PlaneGroup> set = (Set<PlaneGroup>) map.computeIfAbsent(groupA,
                    planeGroup -> new ObjectLinkedOpenHashSet<PlaneGroup>());
            set.add(groupA);
            for (int b = a + 1; b < planes.size(); ++b) {
                final PlaneGroup groupB = planes.get(b);
                if (MathUtils.isSimilar(groupA.axis().dot((Vector3dc) groupB.axis()), -1.0)) {
                    final List<Vector3d> axesA = (List<Vector3d>) this.axes.get(groupA.cubes().iterator().nextInt());
                    final List<Vector3d> axesB = (List<Vector3d>) this.axes.get(groupB.cubes().iterator().nextInt());
                    for (int i = 0; i < 3; ++i) {
                        final Vector3d axisA = axesA.get(i);
                        final Vector3d axisB = axesB.get(i);
                        if (getGroupId(Math.acos(axisA.dot((Vector3dc) axisB)) * 57.29577951308232) != 0) {
                            continue;
                        }
                    }
                } else {
                    for (final PlaneGroup g : set) {
                        final double dot = g.axis().dot((Vector3dc) groupB.axis());
                        if (!MathUtils.isSimilar(dot, 0.0) && !MathUtils.isSimilar(dot, -1.0)) {
                            continue;
                        }
                        final Vector3d vA = groupB.axis().rotate((Quaterniondc) g.invOrigin(), new Vector3d());
                        final Vector3d vB = g.axis().rotate((Quaterniondc) groupB.invOrigin(), new Vector3d());
                        if (!isGroupable(vA)) {
                            continue;
                        }
                        if (!isGroupable(vB)) {
                            continue;
                        }
                    }
                }
                set.add(groupB);
            }
        }
        return fetch((Map<PlaneGroup, Set<PlaneGroup>>) map);
    }

    private void fixGroups(final Collection<ItemGroup> result, final Map<PlaneGroup, Set<PlaneGroup>> planeGroupMap) {
        for (final Map.Entry<PlaneGroup, Set<PlaneGroup>> entry : planeGroupMap.entrySet()) {
            final Set<PlaneGroup> planeGroups = entry.getValue();
            if (planeGroups.size() < 2) {
                final PlaneGroup group = planeGroups.iterator().next();
                result.add(new ItemGroup(new Quaterniond((Quaterniondc) group.origin()),
                        MathUtils.toEulerXYZ(group.origin()),
                        this.rotateCubes(new ArrayList<ProcessedCube>(), group.cubes(), group.invOrigin())));
            } else {
                final Iterator<PlaneGroup> iterator = planeGroups.iterator();
                final PlaneGroup groupA = iterator.next();
                Vector3d axisA;
                Vector3d axisB;
                for (axisA = groupA.axis(), axisB = iterator.next().axis(); MathUtils
                        .isSimilar(Math.abs(axisA.dot((Vector3dc) axisB)), 1.0)
                        && iterator.hasNext(); axisB = iterator.next().axis()) {
                }
                if (MathUtils.isSimilar(Math.abs(axisA.dot((Vector3dc) axisB)), 1.0)) {
                    final ArrayList<ProcessedCube> list = new ArrayList<ProcessedCube>();
                    for (final PlaneGroup planeGroup : planeGroups) {
                        this.rotateCubes(list, planeGroup.cubes(), groupA.invOrigin());
                    }
                    result.add(new ItemGroup(new Quaterniond((Quaterniondc) groupA.origin()),
                            MathUtils.toEulerXYZ(groupA.origin()), list));
                } else {
                    final Vector3d cardinalA = getClosestCardinal(axisA);
                    final Quaterniond aToCardinal = axisA.rotationTo((Vector3dc) cardinalA, new Quaterniond());
                    final Vector3d axisBPrime = axisB.rotate((Quaterniondc) aToCardinal, new Vector3d());
                    final Vector3d cardinalB = getClosestCardinal(axisBPrime);
                    final Quaterniond bPrimeToCardinal = axisBPrime.rotationTo((Vector3dc) cardinalB,
                            new Quaterniond());
                    final Quaterniond invRoot = bPrimeToCardinal.mul((Quaterniondc) aToCardinal);
                    final Quaterniond rootRotation = invRoot.invert(new Quaterniond());
                    final Vector3d rootEuler = MathUtils.fixEuler(MathUtils.toEulerXYZ(rootRotation));
                    final ArrayList<ProcessedCube> list2 = new ArrayList<ProcessedCube>();
                    for (final PlaneGroup planeGroup2 : planeGroups) {
                        this.rotateCubes(list2, planeGroup2.cubes(), invRoot);
                    }
                    result.add(new ItemGroup(rootRotation, rootEuler, list2));
                }
            }
        }
    }

    private List<ProcessedCube> rotateCubes(final List<ProcessedCube> result, final IntSet cubeIds,
            final Quaterniond quaterniond) {
        for (final int cubeId : cubeIds) {
            final ProcessedCube cube = (ProcessedCube) this.cubes.get(cubeId);
            cube.rotate(quaterniond);
            result.add(IllegalRotationSolver.solve(cube));
        }
        return result;
    }

    public static <T, R extends Collection<S>, S> Map<T, R> fetch(final Map<T, R> originalMap) {
        if (originalMap == null) {
            return new LinkedHashMap<>();
        }

        Map<T, R> workingMap = new LinkedHashMap<>();
        for (Map.Entry<T, R> entry : originalMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                Collection<S> copiedCollection;
                try {
                    copiedCollection = new HashSet<>(entry.getValue());
                } catch (Exception e) {
                    try {
                        copiedCollection = new ArrayList<>(entry.getValue());
                    } catch (Exception e2) {
                        throw new IllegalArgumentException("Cannot copy collection type R for key: " + entry.getKey(),
                                e2);
                    }
                }
                try {
                    @SuppressWarnings("unchecked")
                    R newCollection = (R) copiedCollection;
                    workingMap.put(entry.getKey(), newCollection);
                } catch (ClassCastException e) {
                    System.err.println("Warning: Could not safely copy collection type R for key '" + entry.getKey()
                            + "'. The specific collection type might not be preserved. Error: " + e.getMessage());
                    throw new ClassCastException(
                            "Cannot cast internal HashSet/ArrayList back to original Collection type R: "
                                    + entry.getValue().getClass().getName());
                }
            }
        }

        Map<T, R> result = new LinkedHashMap<>();

        while (true) {

            Optional<Map.Entry<T, R>> largestEntryOpt = workingMap.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                    .max(Comparator.comparingInt(entry -> entry.getValue().size()));

            if (largestEntryOpt.isEmpty()) {
                break;
            }

            Map.Entry<T, R> largestEntry = largestEntryOpt.get();
            T largestKey = largestEntry.getKey();
            R largestCollection = largestEntry.getValue();

            result.put(largestKey, largestCollection);
            workingMap.remove(largestKey);

            Set<S> elementsToRemove = new HashSet<>(largestCollection);
            if (elementsToRemove.isEmpty()) {
                continue;
            }

            for (R remainingCollection : workingMap.values()) {
                if (remainingCollection != null) {
                    remainingCollection.removeAll(elementsToRemove);
                }
            }
        }

        return result;
    }

    private static int getGroupId(double angle) {
        while (angle < 0.0) {
            angle += 360.0;
        }
        final int intAngle = (int) Math.round(angle * 10000.0);
        return intAngle % 225000;
    }

    private static boolean isGroupable(final Vector3d vec) {
        for (final Axis axis : Axis.values()) {
            if (getGroupId(Math.acos(vec.dot((Vector3dc) axis.getVector())) * 57.295780181884766) != 0) {
                return false;
            }
        }
        return true;
    }

    private static Vector3d getClosestCardinal(final Vector3d vec) {
        double dot = -1.0;
        Vector3d cardinal = null;
        for (final Vector3d axis : RotationSolver.CARDINAL_AXES) {
            final double d = vec.dot((Vector3dc) axis);
            if (d > dot) {
                dot = d;
                cardinal = axis;
            }
        }
        return cardinal;
    }

    private static boolean isLegal(final ProcessedCube cube) {
        return legalId(cube.getRotation().x) + legalId(cube.getRotation().y) + legalId(cube.getRotation().z) < 2;
    }

    private static int legalId(final double angle) {
        if (MathUtils.isInterval(angle, 90.0)) {
            return 0;
        }
        if (MathUtils.isInterval(angle, 22.5)) {
            return 1;
        }
        return 999;
    }

    private RotationSolver() {
    }

}
