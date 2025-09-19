package me.winflix.vitalcore.addons.model.processor.solvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.joml.*;
import org.joml.Math;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.model.data.ProcessedCube;
import me.winflix.vitalcore.addons.model.data.ProcessedCube.Direction;
import me.winflix.vitalcore.addons.model.data.ProcessedCube.Face;
import me.winflix.vitalcore.addons.model.data.ProcessedCube.UV;
import me.winflix.vitalcore.addons.utils.MathUtils;
import me.winflix.vitalcore.addons.utils.Triple;

/**
 * Resuelve rotaciones consideradas "ilegales" (no alineadas con ejes
 * principales
 * o múltiplos simples de 22.5 grados) ajustándolas al eje cardinal más cercano.
 * Lógica adaptada de MEIllegalRotationSolver.
 */
public class IllegalRotationSolver {

    private static final boolean DEBUG_ENABLED = false;

    // Ejes cardinales X, Y, Z positivos (usados por
    // MEIllegalRotationSolver.closestAxis)
    private static final List<Vector3d> AXES = List.of(
            new Vector3d(1.0, 0.0, 0.0), // X+
            new Vector3d(0.0, 1.1, 0.0), // Y+ // Nota: Y tiene 1.1, ¿intencional?
            new Vector3d(0.0, 0.0, 1.1) // Z+ // Nota: Z tiene 1.1, ¿intencional?
    );

    /**
     * Intenta corregir la rotación de un cubo si se considera "ilegal".
     * Si la rotación es legal, devuelve el cubo original.
     * Si es ilegal, calcula una nueva rotación alineada con los ejes cardinales,
     * ajusta las UVs de las caras y recalcula los límites (from/to).
     *
     * @param cube El ProcessedCube a resolver.
     * @return El cubo original si era legal, o un nuevo ProcessedCube con la
     * rotación corregida.
     */
    public static ProcessedCube solve(ProcessedCube cube) {
        if (isLegal(cube)) {
            return cube;
        }

        // Log de advertencia siempre se muestra, ya que indica que se está corrigiendo algo
        VitalCore.Log.warning("[IllegalSolver] Resolviendo rotación ilegal para cubo: " + cube.getName());

        Quaterniond originalQuat = cube.getRotQuartenion(); // Usar el cuaternión directamente
        final HashMap<Direction, Triple<Vector3d, Vector3d, Face>> uvs = new HashMap<>();

        for (Map.Entry<Direction, Face> entry : cube.getFaces().entrySet()) {
            Direction dir = entry.getKey();
            Face uv = entry.getValue();
            uvs.put(dir,
                    new Triple<>(dir.getNormal().rotate(originalQuat, new Vector3d()), // Usar Quaterniond y crear nueva instancia
                            dir.getUvUp().rotate(originalQuat, new Vector3d()), // Usar Quaterniond y crear nueva instancia
                            uv));
        }

        List<Vector3d> corners = calculateCorners(cube.getFrom(), cube.getTo());
        // Rotar esquinas por la rotación original
        corners.forEach(corner -> corner.sub(cube.getPivot()).rotate(originalQuat).add(cube.getPivot()));

        Vector3d srcDir = new Vector3d(0.0, 0.0, 1.0).rotate(originalQuat);
        Vector3d dstAxis = closestAxis(srcDir);
        double dot = srcDir.dot(dstAxis);

        // Clampear dot inicial
        double clampedDot = Math.max(-1.0, Math.min(1.0, dot));
        if (DEBUG_ENABLED) {
            VitalCore.Log.info("[IllegalSolver DEBUG] Initial dot: " + dot + " (clamped: " + clampedDot + ")");
        }

        Quaterniond undoQuat = new Quaterniond(); // Inicializar a identidad

        try {
            if (MathUtils.isSimilar(clampedDot, 1.0)) {
                if (DEBUG_ENABLED) {
                    VitalCore.Log.info("[IllegalSolver DEBUG] Dot is similar to 1.0. Trying second axis...");
                }
                // Intentar con el eje Y
                srcDir = new Vector3d(0.0, 1.0, 0.0).rotate(originalQuat);
                dstAxis = closestAxis(srcDir);
                dot = srcDir.dot(dstAxis);
                double clampedDot2 = Math.max(-1.0, Math.min(1.0, dot)); // Clampear de nuevo
                if (DEBUG_ENABLED) {
                    VitalCore.Log
                            .info("[IllegalSolver DEBUG] Second attempt dot: " + dot + " (clamped: " + clampedDot2 + ")");
                }

                if (MathUtils.isSimilar(clampedDot2, 1.0)) {
                    if (DEBUG_ENABLED) {
                        VitalCore.Log.info("[IllegalSolver DEBUG] Second attempt dot is 1.0. undoQuat = identity");
                    }
                    undoQuat = new Quaterniond(); // Ya está alineado
                } else if (MathUtils.isSimilar(clampedDot2, -1.0)) {
                    Vector3d perpendicularAxis = new Vector3d(1.0, 0.0, 0.0); // Eje X es perpendicular a Y y Z
                    if (DEBUG_ENABLED) {
                        // --- INICIO CÓDIGO DE DEBUG ---
                        VitalCore.Log.info(
                                "[IllegalSolver EXTREME DEBUG] Calling rotateAxis (Second Attempt Anti-parallel case):");
                        VitalCore.Log.info("[IllegalSolver EXTREME DEBUG]   angle (rad): " + Math.PI + " (deg: 180.0)");
                        VitalCore.Log.info("[IllegalSolver EXTREME DEBUG]   axis: " + perpendicularAxis + " (length sq: "
                                + perpendicularAxis.lengthSquared() + ")");
                        if (Double.isNaN(Math.PI) || Double.isNaN(perpendicularAxis.x) || Double.isNaN(perpendicularAxis.y)
                                || Double.isNaN(perpendicularAxis.z)) {
                            VitalCore.Log
                                    .severe("[IllegalSolver EXTREME DEBUG] !!! NaN DETECTED BEFORE rotateAxis CALL !!!");
                        }
                        if (perpendicularAxis.lengthSquared() < 1E-10) {
                            VitalCore.Log.severe(
                                    "[IllegalSolver EXTREME DEBUG] !!! Axis vector is near zero before rotateAxis CALL !!!");
                        }
                        // --- FIN CÓDIGO DE DEBUG ---
                    }
                    undoQuat.rotateAxis(Math.PI, perpendicularAxis);
                    if (DEBUG_ENABLED) {
                        VitalCore.Log
                                .info("[IllegalSolver EXTREME DEBUG] Resulting undoQuat (Second Attempt Anti-parallel): "
                                        + undoQuat);
                    }
                } else {
                    Vector3d cross2 = srcDir.cross(dstAxis, new Vector3d());
                    if (DEBUG_ENABLED) {
                        VitalCore.Log.info("[IllegalSolver DEBUG] Second attempt cross product: " + cross2);
                    }
                    if (cross2.lengthSquared() < MathUtils.EPSILON * MathUtils.EPSILON) {
                        if (DEBUG_ENABLED) {
                            VitalCore.Log.warning(
                                    "[IllegalSolver DEBUG] Second attempt cross product near zero. Using identity.");
                        }
                        undoQuat = new Quaterniond();
                    } else {
                        cross2.normalize();
                        double angle2 = Math.acos(clampedDot2);
                        if (DEBUG_ENABLED) {
                            // --- INICIO CÓDIGO DE DEBUG ---
                            VitalCore.Log.info(
                                    "[IllegalSolver EXTREME DEBUG] Calling rotateAxis (Second Attempt General case):");
                            VitalCore.Log.info("[IllegalSolver EXTREME DEBUG]   angle (rad): " + angle2 + " (deg: "
                                    + Math.toDegrees(angle2) + ")");
                            VitalCore.Log.info("[IllegalSolver EXTREME DEBUG]   axis: " + cross2 + " (length sq: "
                                    + cross2.lengthSquared() + ")");
                            if (Double.isNaN(angle2) || Double.isNaN(cross2.x) || Double.isNaN(cross2.y)
                                    || Double.isNaN(cross2.z)) {
                                VitalCore.Log.severe(
                                        "[IllegalSolver EXTREME DEBUG] !!! NaN DETECTED BEFORE rotateAxis CALL !!!");
                            }
                            if (cross2.lengthSquared() < 1E-10) {
                                VitalCore.Log.severe(
                                        "[IllegalSolver EXTREME DEBUG] !!! Axis vector is near zero before rotateAxis CALL !!!");
                            }
                            // --- FIN CÓDIGO DE DEBUG ---
                        }
                        undoQuat.rotateAxis(angle2, cross2);
                        if (DEBUG_ENABLED) {
                            VitalCore.Log.info("[IllegalSolver EXTREME DEBUG] Resulting undoQuat (Second Attempt General): "
                                    + undoQuat);
                        }
                    }
                }
            } else if (MathUtils.isSimilar(clampedDot, -1.0)) {
                Vector3d perpendicularAxis = new Vector3d(0.0, 1.0, 0.0); // Eje Y es perpendicular a Z
                if (DEBUG_ENABLED) {
                    // --- INICIO CÓDIGO DE DEBUG ---
                    VitalCore.Log
                            .info("[IllegalSolver EXTREME DEBUG] Calling rotateAxis (First Attempt Anti-parallel case):");
                    VitalCore.Log.info("[IllegalSolver EXTREME DEBUG]   angle (rad): " + Math.PI + " (deg: 180.0)");
                    VitalCore.Log.info("[IllegalSolver EXTREME DEBUG]   axis: " + perpendicularAxis + " (length sq: "
                            + perpendicularAxis.lengthSquared() + ")");
                    if (Double.isNaN(Math.PI) || Double.isNaN(perpendicularAxis.x) || Double.isNaN(perpendicularAxis.y)
                            || Double.isNaN(perpendicularAxis.z)) {
                        VitalCore.Log.severe("[IllegalSolver EXTREME DEBUG] !!! NaN DETECTED BEFORE rotateAxis CALL !!!");
                    }
                    if (perpendicularAxis.lengthSquared() < 1E-10) {
                        VitalCore.Log.severe(
                                "[IllegalSolver EXTREME DEBUG] !!! Axis vector is near zero before rotateAxis CALL !!!");
                    }
                    // --- FIN CÓDIGO DE DEBUG ---
                }
                undoQuat.rotateAxis(Math.PI, perpendicularAxis);
                if (DEBUG_ENABLED) {
                    VitalCore.Log.info(
                            "[IllegalSolver EXTREME DEBUG] Resulting undoQuat (First Attempt Anti-parallel): " + undoQuat);
                }
            } else { // Caso general primer intento
                Vector3d cross = srcDir.cross(dstAxis, new Vector3d());
                if (DEBUG_ENABLED) {
                    VitalCore.Log.info("[IllegalSolver DEBUG] First attempt cross product: " + cross);
                }
                if (cross.lengthSquared() < MathUtils.EPSILON * MathUtils.EPSILON) {
                    if (DEBUG_ENABLED) {
                        VitalCore.Log
                                .warning("[IllegalSolver DEBUG] First attempt cross product near zero. Using identity.");
                    }
                    undoQuat = new Quaterniond();
                } else {
                    cross.normalize();
                    double angle = Math.acos(clampedDot);
                    if (DEBUG_ENABLED) {
                        // --- INICIO CÓDIGO DE DEBUG ---
                        VitalCore.Log
                                .info("[IllegalSolver EXTREME DEBUG] Calling rotateAxis (First Attempt General case):");
                        VitalCore.Log.info("[IllegalSolver EXTREME DEBUG]   angle (rad): " + angle + " (deg: "
                                + Math.toDegrees(angle) + ")");
                        VitalCore.Log.info("[IllegalSolver EXTREME DEBUG]   axis: " + cross + " (length sq: "
                                + cross.lengthSquared() + ")");
                        if (Double.isNaN(angle) || Double.isNaN(cross.x) || Double.isNaN(cross.y)
                                || Double.isNaN(cross.z)) {
                            VitalCore.Log
                                    .severe("[IllegalSolver EXTREME DEBUG] !!! NaN DETECTED BEFORE rotateAxis CALL !!!");
                        }
                        if (cross.lengthSquared() < 1E-10) {
                            VitalCore.Log.severe(
                                    "[IllegalSolver EXTREME DEBUG] !!! Axis vector is near zero before rotateAxis CALL !!!");
                        }
                        // --- FIN CÓDIGO DE DEBUG ---
                    }
                    undoQuat.rotateAxis(angle, cross);
                    if (DEBUG_ENABLED) {
                        VitalCore.Log.info(
                                "[IllegalSolver EXTREME DEBUG] Resulting undoQuat (First Attempt General): " + undoQuat);
                    }
                }
            }
        } catch (Exception e) {
            // Log de error severo siempre se muestra
            VitalCore.Log.log(Level.SEVERE,
                    "[IllegalSolver] Exception during undoQuat calculation for cube " + cube.getName() + ": " + e.getMessage(), e); // Log con stack trace
            undoQuat = new Quaterniond(); // Fallback a identidad
        }

        if (DEBUG_ENABLED) {
            VitalCore.Log.info("[IllegalSolver DEBUG] Calculated undoQuat (before corner rotation): " + undoQuat);
        }

        // Recalcular UVs
        HashMap<Direction, Face> newUVs = new HashMap<>();
        for (Map.Entry<Direction, Triple<Vector3d, Vector3d, Face>> entry2 : uvs.entrySet()) {
            Triple<Vector3d, Vector3d, Face> triple = entry2.getValue();
            // Rotar normal original con undoQuat (crear nueva instancia para no modificar la original)
            Vector3d currNormal = new Vector3d(triple.getFirst()).rotate(undoQuat);
            Direction currDir = Direction.fromNormal(currNormal);

            if (currDir == null) {
                // Advertencia siempre se muestra
                VitalCore.Log.warning("[IllegalSolver] Dirección nula después de rotar normal para cara "
                        + entry2.getKey() + " del cubo " + cube.getName() + ". Saltando cara.");
                continue; // Saltar esta cara si no se puede determinar la nueva dirección
            }

            Vector3d currUVUp = currDir.getUvUp(); // UV Up de la *nueva* dirección
            // Rotar UV Up original con undoQuat (crear nueva instancia)
            Vector3d targetUVUp = new Vector3d(triple.getSecond()).rotate(undoQuat);
            int angle = 0;
            double upDot = targetUVUp.dot(currUVUp);
            double clampedUpDot = Math.max(-1.0, Math.min(1.0, upDot)); // Clampear también aquí

            if (MathUtils.isSimilar(clampedUpDot, -1.0)) {
                angle = 180;
            } else if (!MathUtils.isSimilar(clampedUpDot, 1.0)) { // Si no es 1 (paralelo), calcular ángulo
                Vector3d cross2 = targetUVUp.cross(currUVUp, new Vector3d());
                // El signo del dot product entre cross2 y la normal corregida indica la
                // dirección de rotación
                // Usar acos con el valor clampeado
                double angleRad = Math.acos(clampedUpDot);
                angle = (int) Math.round(Math.toDegrees(angleRad)); // Convertir a grados y redondear

                // Usar la normal corregida (currNormal) para determinar la dirección de rotación
                if (cross2.dot(currNormal) < 0) { // Ajustar ángulo a 0-360
                    angle = 360 - angle;
                }
                // Redondear a múltiplo de 90
                angle = (int) (Math.round(angle / 90.0) * 90.0);
                angle %= 360; // Asegurar que esté en [0, 360)
            } // Si upDot es ~1.0, angle es 0 (ya está alineado)

            Face face = triple.getThird();
            UV uv2 = face.uv();
            // Asegurarse de que la rotación final esté en [0, 360)
            int finalRotation = (uv2.rotation() + angle) % 360;
            if (finalRotation < 0) finalRotation += 360; // Corregir si el módulo da negativo

            Face tFace = new Face(
                    new UV(uv2.u1(), uv2.v1(), uv2.u2(), uv2.v2(), finalRotation),
                    face.texture());
            newUVs.put(currDir, tFace);
        }

        // Recalcular límites
        final Vector3d from = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        final Vector3d to = new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
        if (DEBUG_ENABLED) {
            VitalCore.Log.info("[IllegalSolver DEBUG] Cubo: " + cube.getName() + " | Pivot (Rel): " + cube.getPivot());
        }

        int cornerIndex = 0;
        for (Vector3d corner : corners) { // corners ya están rotados por originalQuat
            if (DEBUG_ENABLED) {
                // LOG: Esquina original (Absoluta relativa al hueso, rotada por originalQuat)
                VitalCore.Log.info("[IllegalSolver DEBUG] Corner[" + cornerIndex + "] Original Rotated (Abs): "
                        + new Vector3d(corner)); // Crear copia para log
            }

            // Calcular posición relativa al pivote del cubo
            Vector3d cornerRel = new Vector3d(corner).sub(cube.getPivot());
            if (DEBUG_ENABLED) {
                VitalCore.Log.info("[IllegalSolver DEBUG] Corner[" + cornerIndex + "] Relativo a Pivot: " + cornerRel);
            }

            // Rotar usando undoQuat
            Vector3d rotatedRel = cornerRel.rotate(undoQuat); // rotate modifica in-place
            if (DEBUG_ENABLED) {
                VitalCore.Log
                        .info("[IllegalSolver DEBUG] Corner[" + cornerIndex + "] Rotado por undoQuat (Rel): " + rotatedRel);
            }

            // Volver a absoluto (relativo al hueso)
            Vector3d vec = rotatedRel.add(cube.getPivot()); // add modifica in-place
            if (DEBUG_ENABLED) {
                VitalCore.Log.info("[IllegalSolver DEBUG] Corner[" + cornerIndex + "] Final (Abs): " + vec);
            }

            // Comprobar si hay NaN antes de actualizar min/max
            if (Double.isNaN(vec.x) || Double.isNaN(vec.y) || Double.isNaN(vec.z)) {
                // Error severo siempre se muestra
                VitalCore.Log.severe("[IllegalSolver] NaN detectado al recalcular límites para cubo: " + cube.getName()
                        + ". Abortando corrección para este cubo.");
                return cube; // Devolver el cubo original como fallback seguro
            }

            from.min(vec); // JOML min
            to.max(vec); // JOML max
            if (DEBUG_ENABLED) {
                VitalCore.Log.info(
                        "[IllegalSolver DEBUG] Corner[" + cornerIndex + "] Current Min/Max: from=" + from + " to=" + to);
            }
            cornerIndex++;
        }
        if (DEBUG_ENABLED) {
            VitalCore.Log.info("[IllegalSolver DEBUG] Final Calculated from: " + from);
            VitalCore.Log.info("[IllegalSolver DEBUG] Final Calculated to: " + to);
        }

        // La rotación final del cubo corregido debe ser la identidad,
        // ya que hemos aplicado la 'undoQuat' a las esquinas para alinearlas
        // con los ejes cardinales. La rotación original ya no aplica.
        Vector3d finalRotationEuler = new Vector3d(0.0, 0.0, 0.0); // Rotación corregida es cero

        if (DEBUG_ENABLED) {
             VitalCore.Log.info("[IllegalSolver DEBUG] Final Corrected Rotation (Euler ZYX): " + finalRotationEuler);
        }


        // Devolver nuevo cubo con geometría recalculada y rotación corregida a cero
        return new ProcessedCube(cube.getName(),
                (Vector3dc) from, // Cast a Vector3dc
                (Vector3dc) to,   // Cast a Vector3dc
                (Vector3dc) cube.getPivot(), // Cast a Vector3dc
                finalRotationEuler,
                newUVs,
                cube.getInflate());
    }

    /**
     * Comprueba si la rotación de un cubo es "legal".
     * Legal significa que la rotación ocurre principalmente alrededor de un solo
     * eje (X, Y o Z)
     * y es un múltiplo de 22.5 grados, dentro de un rango de +/- 45 grados.
     *
     * @param cube El cubo a comprobar.
     * @return true si la rotación es legal, false en caso contrario.
     */
    private static boolean isLegal(final ProcessedCube cube) {
        final Vector3d rot = cube.getRotation(); // Asumiendo que esto devuelve grados
        // Comprobar si la rotación es exactamente cero en todos los ejes
        if (MathUtils.isSimilar(rot.x, 0.0) && MathUtils.isSimilar(rot.y, 0.0) && MathUtils.isSimilar(rot.z, 0.0)) {
            return true; // Rotación cero es legal
        }

        // Comprobar rotaciones en múltiplos de 22.5 grados alrededor de un solo eje
        boolean legalX = MathUtils.isAlmostBetween(rot.x, -45.0, 45.0) && MathUtils.isInterval(rot.x, 22.5)
                && MathUtils.isSimilar(rot.y, 0.0) && MathUtils.isSimilar(rot.z, 0.0);
        boolean legalY = MathUtils.isAlmostBetween(rot.y, -45.0, 45.0) && MathUtils.isInterval(rot.y, 22.5)
                && MathUtils.isSimilar(rot.x, 0.0) && MathUtils.isSimilar(rot.z, 0.0);
        boolean legalZ = MathUtils.isAlmostBetween(rot.z, -45.0, 45.0) && MathUtils.isInterval(rot.z, 22.5)
                && MathUtils.isSimilar(rot.x, 0.0) && MathUtils.isSimilar(rot.y, 0.0);

        return legalX || legalY || legalZ;
    }


    /**
     * Encuentra el eje cardinal (X+, Y+, Z+, X-, Y-, Z-) más cercano a la dirección
     * dada.
     *
     * @param dir La dirección de entrada. Debe estar normalizada o tener longitud > 0.
     * @return El Vector3d del eje cardinal más cercano (nueva instancia). Devuelve Z+ si la dirección es cero.
     */
    public static Vector3d closestAxis(Vector3dc dir) {
        // Manejar caso de vector cero
        if (dir.lengthSquared() < MathUtils.EPSILON * MathUtils.EPSILON) {
             if (DEBUG_ENABLED) {
                 VitalCore.Log.warning("[IllegalSolver DEBUG] closestAxis input vector is near zero. Returning Z+.");
             }
            // Devolver una copia segura
            return new Vector3d(IllegalRotationSolver.AXES.get(2));
        }

        Vector3d closest = null;
        double maxDotAbs = -1.0; // Inicializar con valor negativo

        // Usar una copia normalizada para el cálculo del dot product
        Vector3d normalizedDir = dir.normalize(new Vector3d());

        for (final Vector3d axis : IllegalRotationSolver.AXES) {
            // Usar el vector normalizado para el dot product
            final double currentDot = normalizedDir.dot(axis);
            final double currentDotAbs = Math.abs(currentDot);

            if (currentDotAbs > maxDotAbs) {
                maxDotAbs = currentDotAbs;
                closest = axis; // Guardar la referencia al eje de la lista AXES
            }
        }

        // Si por alguna razón no se encontró un 'closest' (no debería pasar si AXES no está vacío)
        if (closest == null) {
             if (DEBUG_ENABLED) {
                 VitalCore.Log.severe("[IllegalSolver DEBUG] closestAxis failed to find a closest axis. Returning Z+ as fallback.");
             }
            // Devolver una copia segura de Z+ como fallback
            return new Vector3d(IllegalRotationSolver.AXES.get(2));
        }

        // Determinar si la dirección original estaba más alineada con el eje positivo o negativo
        // Usar el dot product con la dirección *original* (no normalizada) para preservar el signo correctamente
        double finalDot = dir.dot(closest);

        // Devolver una *nueva instancia* del vector, negado si es necesario
        return (finalDot >= 0.0) ? new Vector3d(closest) : closest.negate(new Vector3d());
    }


    /**
     * Calcula las 8 esquinas de un cubo definido por sus límites 'from' y 'to'.
     *
     * @param from El punto mínimo del AABB.
     * @param to   El punto máximo del AABB.
     * @return Una lista de 8 Vector3d representando las esquinas.
     */
    private static List<Vector3d> calculateCorners(Vector3dc from, Vector3dc to) {
        List<Vector3d> corners = new ArrayList<>(8); // Pre-allocating size
        corners.add(new Vector3d(from.x(), from.y(), from.z()));
        corners.add(new Vector3d(from.x(), from.y(), to.z()));
        corners.add(new Vector3d(from.x(), to.y(), from.z()));
        corners.add(new Vector3d(from.x(), to.y(), to.z()));
        corners.add(new Vector3d(to.x(), from.y(), from.z()));
        corners.add(new Vector3d(to.x(), from.y(), to.z()));
        corners.add(new Vector3d(to.x(), to.y(), from.z()));
        corners.add(new Vector3d(to.x(), to.y(), to.z()));
        return corners;
    }

    // Constructor privado para evitar instanciación
    private IllegalRotationSolver() {
    }
}
