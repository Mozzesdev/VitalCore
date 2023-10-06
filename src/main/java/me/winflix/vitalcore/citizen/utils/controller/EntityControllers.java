package me.winflix.vitalcore.citizen.utils;

import com.google.common.base.Throwables;
import me.winflix.vitalcore.citizen.interfaces.EntityController;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.Map;

public class EntityControllers {
    public static boolean controllerExistsForType(EntityType type) {
        return TYPES.containsKey(type);
    }

    public static EntityController createForType(EntityType type) {
        Constructor<? extends EntityController> constructor = TYPES.get(type);
        if (constructor == null)
            throw new IllegalArgumentException("Unknown EntityType: " + type);
        try {
            return constructor.newInstance();
        } catch (Throwable ex) {
            Throwables.getRootCause(ex).printStackTrace();
            return null;
        }
    }

    public static void setEntityControllerForType(EntityType type, Class<? extends EntityController> controller) {
        try {
            Constructor<? extends EntityController> constructor = controller.getConstructor();
            constructor.setAccessible(true);
            TYPES.put(type, constructor);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private static final Map<EntityType, Constructor<? extends EntityController>> TYPES = new EnumMap<>(EntityType.class);
}