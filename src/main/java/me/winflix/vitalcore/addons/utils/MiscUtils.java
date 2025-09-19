package me.winflix.vitalcore.addons.utils;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.UUID;

import javax.annotation.Nonnull;

public class MiscUtils {
    public static final DecimalFormat FORMATTER;

    @SafeVarargs
    @Nonnull
    public static <T> T or(final T... values) {
        for (final T v : values) {
            if (v != null) {
                return v;
            }
        }
        throw new RuntimeException("All values are null");
    }

    @SafeVarargs
    @Nonnull
    public static <T> T orDef(@Nonnull final T def, final T... values) {
        for (final T v : values) {
            if (v != null) {
                return v;
            }
        }
        return def;
    }

    public static boolean isJava21OrHigher() {
        try {
            final int version = Runtime.version().feature();
            return version >= 21;
        } catch (final Exception e) {
            final int version2 = Integer.parseInt(System.getProperty("java.version"));
            return version2 >= 21;
        }
    }

    public static UUID generateUUIDFromString(final String input) {
        return UUID.nameUUIDFromBytes(input.getBytes(StandardCharsets.UTF_8));
    }

    static {
        FORMATTER = new DecimalFormat() {
            {
                this.setMaximumFractionDigits(1);
                this.setMinimumFractionDigits(1);
            }
        };
    }
}
