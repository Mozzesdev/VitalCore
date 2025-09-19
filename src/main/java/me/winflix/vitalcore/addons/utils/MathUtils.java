package me.winflix.vitalcore.addons.utils;

import javax.annotation.Nullable;

import org.bukkit.util.Vector;
import org.joml.Math;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Matrix3f;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;

public class MathUtils {
    public static final float PI = 3.1415927f;
    public static final double TAU = 6.283185307179586;
    public static final double PI_2 = 1.5707963267948966;
    public static final double PI_4 = 0.7853981633974483;
    public static final double EPSILON = 1.0E-5;
    public static final float DEG2RAD = 0.017453292f;
    public static final float RAD2DEG = 57.29578f;
    public static final double D_DEG2RAD = 0.017453292519943295;
    public static final double D_RAD2DEG = 57.29577951308232;

    protected static double movementResolution;

    /**
     * Compara si dos doubles son similares dentro de la tolerancia EPSILON.
     * 
     * @param a Primer valor double.
     * @param b Segundo valor double.
     * @return true si son similares, false en caso contrario.
     */
    public static boolean isSimilar(final double a, final double b) {
        return Math.abs(b - a) < EPSILON;
    }

    public static Vector3d toEulerZYX(Quaterniond quaternion) {
        return getEulerAnglesZYX(quaternion, new Vector3d()).mul(D_RAD2DEG);
    }

    public static Vector3d toEulerXYZ(Quaterniond quaternion) {
        return matToEulerXYZ(quaternion.get(new Matrix3d()), new Vector3d()).mul(57.29577951308232);
    }

    public static Vector3f getEulerAnglesZYX(Quaternionf quat, Vector3f eulerAngles) {
        return matToEulerZYX(quat.get(new Matrix3f()), eulerAngles);
    }

    public static Vector3d getEulerAnglesZYX(Quaterniond quat, Vector3d eulerAngles) {
        return matToEulerZYX(quat.get(new Matrix3d()), eulerAngles);
    }

    public static Quaterniond fromEulerXYZ(final Vector3d euler) {
        return new Quaterniond().rotateXYZ(euler.x * 0.017453292519943295, euler.y * 0.017453292519943295, euler.z * 0.017453292519943295);
    }

    /**
     * Convierte un Vector3d que representa ángulos de Euler (en radianes)
     * a un Quaterniond.
     *
     * @param eulerAngles Vector3d con los ángulos en radianes.
     * @return Quaterniond representando la orientación equivalente.
     */
    public static Quaterniond eulerToQuaternion(Vector3d eulerAngles) {
        double rollRad = eulerAngles.x;
        double pitchRad = eulerAngles.y;
        double yawRad = eulerAngles.z;

        return new Quaterniond().rotationZYX(yawRad, pitchRad, rollRad);
    }

    public static Vector3f matToEulerZYX(Matrix3f mat, Vector3f result) {
        result.y = Math.asin(-Math.clamp(mat.m02, -1.0f, 1.0f));
        if (Math.abs(mat.m02) < 0.9999999) {
            result.x = Math.atan2(mat.m12, mat.m22);
            result.z = Math.atan2(mat.m01, mat.m00);
        } else {
            result.x = 0.0f;
            result.z = Math.atan2(-mat.m10, mat.m11);
        }
        return result;
    }

    /**
     * Ajusta los componentes de un vector a cero si son muy cercanos a cero (usando
     * EPSILON).
     * Modifica el vector original.
     * 
     * @param v El vector a ajustar.
     * @return El mismo vector modificado.
     */
    public static Vector3d fixVector(final Vector3d v) {
        if (isSimilar(v.x, 0.0)) {
            v.x = 0.0;
        }
        if (isSimilar(v.y, 0.0)) {
            v.y = 0.0;
        }
        if (isSimilar(v.z, 0.0)) {
            v.z = 0.0;
        }
        return v;
    }

    /**
     * Convierte un cuaternión a ángulos de Euler XYZ en grados.
     * 
     * @param quaternion El cuaternión a convertir.
     * @return Un nuevo Vector3d con los ángulos de Euler (X, Y, Z) en grados.
     */
    public static Vector3d toEulerXYZDegrees(final Quaterniond quaternion) {
        // Llama a la versión que devuelve radianes y convierte
        return toEulerXYZRadians(quaternion).mul(D_RAD2DEG);
    }

    public static Quaterniond fromEulerZYX(final Vector3d euler) {
        return new Quaterniond().rotateZYX(euler.z * 0.017453292519943295, euler.y * 0.017453292519943295,
                euler.x * 0.017453292519943295);
    }

    /**
     * Convierte un cuaternión a ángulos de Euler XYZ en radianes.
     * 
     * @param quaternion El cuaternión a convertir.
     * @return Un nuevo Vector3d con los ángulos de Euler (X, Y, Z) en radianes.
     */
    public static Vector3d toEulerXYZRadians(final Quaterniond quaternion) {
        // Llama a la conversión desde matriz
        return matToEulerXYZRadians(quaternion.get(new Matrix3d()), new Vector3d());
    }

    /**
     * Convierte una matriz de rotación 3x3 a ángulos de Euler XYZ en radianes.
     * 
     * @param mat    La matriz de rotación.
     * @param result El Vector3d donde se almacenarán los ángulos resultantes
     *               (modificado).
     * @return El mismo Vector3d result modificado con los ángulos en radianes.
     */
    public static Vector3d matToEulerXYZRadians(final Matrix3d mat, final Vector3d result) {
        // Usa Math.clamp de JOML
        result.y = Math.asin(Math.clamp(mat.m20, -1.0, 1.0));
        // Usar una comparación robusta cerca de +/- PI/2 para el ángulo Y
        if (Math.abs(result.y) < (Math.PI / 2.0 - EPSILON)) { // Lejos de los polos
            result.x = Math.atan2(-mat.m21, mat.m22);
            result.z = Math.atan2(-mat.m10, mat.m00);
        } else { // En o cerca de los polos (gimbal lock)
            // Asignar toda la rotación Z al eje X (o Z, es una convención)
            result.x = Math.atan2(mat.m12, mat.m11);
            result.z = 0.0; // Fijar Z a 0 en este caso
        }
        return result;
    }

    /**
     * Ajusta los componentes de un vector Euler (grados) a cero si son cercanos
     * (usando EPSILON)
     * y redondea a una precisión razonable (ej. 4 decimales).
     * Modifica el vector original.
     * 
     * @param v El vector de ángulos Euler en grados a ajustar.
     * @return El mismo vector modificado.
     */
    public static Vector3d fixEuler(final Vector3d v) {
        fixVector(v);
        v.x = Math.round(v.x * 10000.0) * 1.0E-4;
        v.y = Math.round(v.y * 10000.0) * 1.0E-4;
        v.z = Math.round(v.z * 10000.0) * 1.0E-4;
        return v;
    }

    /**
     * Comprueba si un valor es aproximadamente un múltiplo de un intervalo dado,
     * usando la tolerancia EPSILON.
     * 
     * @param value    El valor a comprobar.
     * @param interval El intervalo del cual debe ser múltiplo.
     * @return true si el valor es aproximadamente un múltiplo del intervalo.
     */
    public static boolean isInterval(final double value, final double interval) {
        final double half = interval * 0.5;
        return isSimilar(Math.abs(Math.abs(value) % interval - half), half);
    }

    // Constructor privado para evitar instanciación
    private MathUtils() {
    }

    public static boolean isSimilar(final float a, final float b) {
        return Math.abs(b - a) < EPSILON;
    }

    public static double clamp(final double value, final double min, final double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(final float value, final float min, final float max) {
        return Math.min(Math.max(value, min), max);
    }

    public static int clamp(final int value, final int min, final int max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Devuelve el índice (0, 1, o 2) del componente con el mayor valor absoluto.
     * 
     * @param a Componente x
     * @param b Componente y
     * @param c Componente z
     * @return 0 si a es el mayor, 1 si b es el mayor, 2 si c es el mayor.
     */
    public static int absMax(final double a, final double b, final double c) {
        final double absA = Math.abs(a);
        final double absB = Math.abs(b);
        final double absC = Math.abs(c);
        if (absA >= absB && absA >= absC)
            return 0;
        if (absB >= absA && absB >= absC)
            return 1;
        return 2;
    }

    // --- fixVector para float (mantener si se usa en otro lugar) ---
    public static Vector3f fixVector(final Vector3f v) {
        if (isSimilar(v.x, 0.0f)) {
            v.x = 0.0f;
        }
        if (isSimilar(v.y, 0.0f)) {
            v.y = 0.0f;
        }
        if (isSimilar(v.z, 0.0f)) {
            v.z = 0.0f;
        }
        return v;
    }

    // --- fixEuler para float (mantener si se usa) ---
    public static Vector3f fixEuler(Vector3f eulerDeg) {
        fixVector(eulerDeg);
        eulerDeg.x = Math.round(eulerDeg.x * 10000.0f) / 10000.0f;
        eulerDeg.y = Math.round(eulerDeg.y * 10000.0f) / 10000.0f;
        eulerDeg.z = Math.round(eulerDeg.z * 10000.0f) / 10000.0f;
        return eulerDeg;
    }

    public static float[] unwrap(final Vector3f vec) {
        return new float[] { vec.x, vec.y, vec.z };
    }
    
    public static float[] unwrap(final Vector3d vec) {
        return new float[] { (float)vec.x, (float)vec.y, (float)vec.z };
    }

    public static Vector3d matToEulerZYX(final Matrix3d mat, final Vector3d result) {
        result.y = Math.asin(-clamp(mat.m02, -1.0, 1.0));
        if (Math.abs(mat.m02) < 0.9999999) { // Usar un epsilon pequeño implícito
            result.x = Math.atan2(mat.m12, mat.m22);
            result.z = Math.atan2(mat.m01, mat.m00);
        } else {
            result.x = 0.0;
            result.z = Math.atan2(-mat.m10, mat.m11);
        }
        return result;
    }

    // Conversión Matriz a Euler XYZ (double) - Ya existente
    public static Vector3d matToEulerXYZ(final Matrix3d mat, final Vector3d result) {
        result.y = Math.asin(clamp(mat.m20, -1.0, 1.0));
        if (Math.abs(mat.m20) < 0.9999999) {
            result.x = Math.atan2(-mat.m21, mat.m22);
            result.z = Math.atan2(-mat.m10, mat.m00);
        } else {
            result.x = Math.atan2(mat.m12, mat.m11);
            result.z = 0.0;
        }
        return result;
    }

    // --- NUEVO: Quaterniond a Euler ZYX en Grados ---
    /**
     * Convierte un Quaterniond a ángulos Euler ZYX en GRADOS.
     * 
     * @param q El cuaternión de entrada.
     * @return Un *nuevo* Vector3d con los ángulos en grados.
     */
    public static Vector3d toEulerZYXDegrees(Quaterniondc q) {
        Vector3d eulerRad = new Vector3d();
        // JOML getEulerAnglesZYX necesita un Vector3d destino
        q.getEulerAnglesZYX(eulerRad);
        // Convierte radianes a grados (usando constante double)
        eulerRad.mul(D_RAD2DEG);
        return eulerRad; // Devuelve el vector con grados
    }

    // --- Quaternionf a Euler ZYX en Grados (mantener) ---
    public static Vector3f toEulerZYXDegrees(Quaternionf q) {
        Vector3f eulerRad = new Vector3f();
        q.getEulerAnglesZYX(eulerRad);
        eulerRad.mul(RAD2DEG);
        return eulerRad;
    }

    /**
     * Comprueba si un valor está dentro de un rango [min, max] o muy cerca
     * de los límites, usando la tolerancia EPSILON de la clase.
     *
     * @param val El valor a comprobar.
     * @param min El límite inferior del rango.
     * @param max El límite superior del rango.
     * @return true si el valor está dentro o muy cerca del rango.
     */
    public static boolean isAlmostBetween(final double val, final double min, final double max) {
        return (val >= min - EPSILON && val <= max + EPSILON);
    }

    public static boolean isSimilar(final Vector a, final Vector b) {
        return Math.abs(b.getX() - a.getX()) < MathUtils.movementResolution
                && Math.abs(b.getY() - a.getY()) < MathUtils.movementResolution
                && Math.abs(b.getZ() - a.getZ()) < MathUtils.movementResolution;
    }

    /**
     * Convierte un Quaterniond a ángulos Euler XYZ en GRADOS.
     * 
     * @param q El cuaternión de entrada.
     * @return Un *nuevo* Vector3d con los ángulos en grados.
     */
    public static Vector3d toEulerXYZDegrees(Quaterniondc q) {
        Matrix3d mat = new Matrix3d();
        q.get(mat); // Obtiene la matriz de rotación del cuaternión
        Vector3d resultRad = new Vector3d();
        matToEulerXYZ(mat, resultRad); // Usa el método existente para double
        resultRad.mul(D_RAD2DEG); // Convierte radianes a grados
        return resultRad;
    }

    // --- Quaternionf a Euler XYZ en Grados (mantener) ---
    public static Vector3f toEulerXYZDegrees(Quaternionf q) {
        Matrix3f mat = new Matrix3f();
        q.get(mat);
        Vector3f resultRad = new Vector3f();
        // Lógica de conversión Matriz -> Euler XYZ (float)
        resultRad.y = (float) Math.asin(clamp(mat.m20(), -1.0f, 1.0f));
        if (Math.abs(mat.m20()) < 0.9999999f) {
            resultRad.x = (float) Math.atan2(-mat.m21(), mat.m22());
            resultRad.z = (float) Math.atan2(-mat.m10(), mat.m00());
        } else {
            resultRad.x = (float) Math.atan2(mat.m12(), mat.m11());
            resultRad.z = 0.0f;
        }
        resultRad.mul(RAD2DEG);
        return resultRad;
    }

    public static Vector3f toVector3f(@Nullable final Float[] vec) {
        return (vec == null) ? new Vector3f()
                : new Vector3f((vec[0] == null) ? 0.0f : ((float) vec[0]), (vec[1] == null) ? 0.0f : ((float) vec[1]),
                        (vec[2] == null) ? 0.0f : ((float) vec[2]));
    }

    public static Vector3d toVector3d(@Nullable final Float[] vec) {
        return (vec == null) ? new Vector3d()
                : new Vector3d((vec[0] == null) ? 0.0 : ((double) vec[0]), (vec[1] == null) ? 0.0 : ((double) vec[1]),
                        (vec[2] == null) ? 0.0 : ((double) vec[2]));
    }

    static {
        MathUtils.movementResolution = 0.001;
    }
}