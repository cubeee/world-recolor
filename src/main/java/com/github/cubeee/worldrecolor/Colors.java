package com.github.cubeee.worldrecolor;

import java.awt.Color;

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

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public static Color rs2hsbToColor(int rs2hsb) {
        float hue = (rs2hsb >> 10 & 0x3F) / 63.0f;
        float saturation = (rs2hsb >> 7 & 7) / 7.0f;
        float brightness = (rs2hsb & 0x7F) / 127.0f;

        if (saturation <= 0.5f) {
            brightness = brightness / (1.0f - saturation);
        } else {
            float bCandidate = 2.0f * brightness;
            if (saturation > brightness) {
                brightness = bCandidate;
            } else {
                brightness = brightness + saturation;
            }
        }

        brightness = Math.min(1.0f, Math.max(0.0f, brightness));
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public static int colorToRs2hsb(Color color) {
        float[] hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        hsbVals[2] -= Math.min(hsbVals[1], hsbVals[2] / 2);

        int encodeHue = (int) (hsbVals[0] * 63);
        int encodeSaturation = (int) (hsbVals[1] * 7);
        int encodeBrightness = (int) (hsbVals[2] * 127);
        return (encodeHue << 10) + (encodeSaturation << 7) + (encodeBrightness);
    }
}
