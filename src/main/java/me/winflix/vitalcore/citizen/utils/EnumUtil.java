package me.winflix.vitalcore.citizen.utils;


import java.util.ArrayList;
import java.util.List;

public class EnumUtil {

    public interface Maskable<M> {
        int getMask();
    }

    public interface Identifiable<I> {
        I getID();

    }

    public interface BiIdentifiable<I, J> extends Identifiable<I> {
        J getSecondID();
    }

    @SafeVarargs
    public static <M> int createMask(Maskable<M>... maskables) {
        int mask = 0;
        for (Maskable m : maskables) {
            mask |= m.getMask();
        }
        return mask;
    }

    public static <M extends Maskable<M>> List<M> fromMask(int mask, Class<M> enumClass) {
        List<M> list = new ArrayList<M>();
        for (M maskable : enumClass.getEnumConstants()) {
            if ((maskable.getMask() & mask) != 0) {
                list.add(maskable);
            }
        }
        return list;
    }

    public static <I, M extends Identifiable<I>> M getByID(I id, Class<M> enumClass) {
        for (M identifiable : enumClass.getEnumConstants()) {
            if (id == identifiable.getID()) {
                return identifiable;
            }
        }
        return null;
    }

    public static <I, J, M extends BiIdentifiable<I, J>> M getBySecondID(J id, Class<M> enumClass) {
        for (M identifiable : enumClass.getEnumConstants()) {
            if (id == identifiable.getID()) {
                return identifiable;
            }
        }
        return null;
    }
}
