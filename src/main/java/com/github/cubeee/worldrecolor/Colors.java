package com.github.cubeee.worldrecolor;

public final class Colors {
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
        int hue = unpackJagexHue(jagexHsl);
        int saturation = unpackJagexSaturation(jagexHsl);
        int lightness = unpackJagexLightness(jagexHsl);
        return new int[] { hue, saturation, lightness };
    }

    public static int packJagexHsl(int hue, int saturation, int lightness) {
        return hue << 10 | saturation << 7 | lightness;
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
