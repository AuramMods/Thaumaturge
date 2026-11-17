package com.leclowndu93150.thaumaturge.compat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.NoSuchElementException;

/** Java 21 library behavior used by the baseline, available on the Forge JVM. */
public final class Java17 {
    private Java17() {}

    public static <T> T first(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException();
        return list.get(0);
    }

    public static <T> T last(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException();
        return list.get(list.size() - 1);
    }

    public static <T> List<T> reversed(List<T> list) {
        return Lists.reverse(list);
    }

    public static int clamp(long value, int min, int max) {
        if (min > max) throw new IllegalArgumentException(min + " > " + max);
        return (int) Math.min(max, Math.max(value, min));
    }

    public static long clamp(long value, long min, long max) {
        if (min > max) throw new IllegalArgumentException(min + " > " + max);
        return Math.min(max, Math.max(value, min));
    }

    public static float clamp(float value, float min, float max) {
        if (Float.isNaN(min)) throw new IllegalArgumentException("min is NaN");
        if (Float.isNaN(max)) throw new IllegalArgumentException("max is NaN");
        if (Float.compare(min, max) > 0) throw new IllegalArgumentException(min + " > " + max);
        return Math.min(max, Math.max(value, min));
    }

    public static double clamp(double value, double min, double max) {
        if (Double.isNaN(min)) throw new IllegalArgumentException("min is NaN");
        if (Double.isNaN(max)) throw new IllegalArgumentException("max is NaN");
        if (Double.compare(min, max) > 0) throw new IllegalArgumentException(min + " > " + max);
        return Math.min(max, Math.max(value, min));
    }
}
