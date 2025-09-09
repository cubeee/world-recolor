package com.github.cubeee.worldrecolor;

public class Colors {
    public static final int MIN_HUE = 0;
    public static final int MAX_HUE = 63;
    public static final int MIN_SATURATION = 0;
    public static final int MAX_SATURATION = 7;
    public static final int MIN_LIGHTNESS = 0;
    public static final int MAX_LIGHTNESS = 127;

    public static final int MIN_HSL = packJagexHsl(MIN_HUE, MIN_SATURATION, MIN_LIGHTNESS);
    public static final int MAX_HSL = packJagexHsl(MAX_HUE, MAX_SATURATION, MAX_LIGHTNESS);

    private Colors() {}

    public static int[] getUnpackedJagexHsl(int jagexHsl) {
        int h = unpackJagexHue(jagexHsl);
        int s = unpackJagexSaturation(jagexHsl);
        int l = unpackJagexLightness(jagexHsl);
        return new int[] { h, s, l };
    }

    public static int packJagexHsl(int h, int s, int l) {
        return h << 10 | s << 7 | l;
    }

    public static int unpackJagexHue(int jagexHsl) {
        return jagexHsl >> 10 & 0x3F;
    }

    public static int unpackJagexSaturation(int jagexHsl) {
        return jagexHsl >> 7 & 7;
    }

    public static int unpackJagexLightness(int jagexHsl) {
        return jagexHsl & 0x7F;
    }
}
