package me.winflix.vitalcore.residents.utils.nms;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import com.google.common.collect.Lists;

public class Reflection {

    public static MethodHandle getFirstGetter(Class<?> clazz, Class<?> type) {
        try {
            List<Field> found = getFieldsMatchingType(clazz, type, false);
            if (found.isEmpty())
                return null;
            return LOOKUP.unreflectGetter(found.get(0));
        } catch (Exception e) {
        }
        return null;
    }

    public static MethodHandle getFirstSetter(Class<?> clazz, Class<?> type) {
        try {
            List<Field> found = getFieldsMatchingType(clazz, type, false);
            if (found.isEmpty())
                return null;
            return LOOKUP.unreflectSetter(found.get(0));
        } catch (Exception e) {
        }
        return null;
    }

    private static List<Field> getFieldsMatchingType(Class<?> clazz, Class<?> type, boolean allowStatic) {
        List<Field> found = Lists.newArrayList();
        for (Field field : clazz.getDeclaredFields()) {
            if (allowStatic ^ Modifier.isStatic(field.getModifiers()))
                continue;
            if (field.getType() == type) {
                found.add(field);
                field.setAccessible(true);
            }
        }
        return found;
    }

    public static MethodHandle getMethodHandle(Class<?> clazz, String method, Class<?>... params) {
        if (clazz == null)
            return null;
        try {
            return LOOKUP.unreflect(getMethod(clazz, method, params));
        } catch (Exception e) {
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String method, Class<?>... params) {
        if (clazz == null)
            return null;
        Method f = null;
        try {
            f = clazz.getDeclaredMethod(method, params);
            f.setAccessible(true);
        } catch (Exception e) {
        }
        return f;
    }

    public static MethodHandle getSetter(Class<?> clazz, String name) {
        if (clazz == null)
            return null;
        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);

            return LOOKUP.unreflectSetter(field);
        } catch (Exception e) {
        }
        return null;
    }

    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

}
