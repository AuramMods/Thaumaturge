package com.leclowndu93150.thaumaturge.compat;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/** Packed-color operations used by the 1.21.1 source, backed by native 1.20.1 math. */
public final class Colors {
    private Colors() {}

    public static final class ARGB32 extends FastColor.ARGB32 {
        private ARGB32() {}

        public static int color(int red, int green, int blue) { return color(255, red, green, blue); }
        public static int color(int alpha, int rgb) { return alpha << 24 | rgb & 0xffffff; }
        public static int opaque(int color) { return color | 0xff000000; }
        public static int colorFromFloat(float alpha, float red, float green, float blue) {
            return color(Mth.floor(alpha * 255.0F), Mth.floor(red * 255.0F),
                    Mth.floor(green * 255.0F), Mth.floor(blue * 255.0F));
        }
        public static int average(int first, int second) {
            return color((alpha(first) + alpha(second)) / 2, (red(first) + red(second)) / 2,
                    (green(first) + green(second)) / 2, (blue(first) + blue(second)) / 2);
        }
    }
}
