package com.github.cubeee.worldrecolor;

import java.awt.Color;

public class ColorMap {
    private final int[] hueMap = identity(Colors.MAX_HUE + 1);
    private final int[] saturationMap = identity(Colors.MAX_SATURATION + 1);
    private final int[] lightnessMap = identity(Colors.MAX_LIGHTNESS + 1);

    private int lastHueReduction;
    private int lastSaturationReduction;
    private int lastLightnessReduction;
    private boolean useFlatColor;
    private Integer flatColor;

    public void updateColors(int hueReduction, int saturationReduction, int lightnessReduction,
                             boolean useFlatColor, Color flatColor) {
        this.useFlatColor = useFlatColor;
        this.flatColor = Colors.colorToRs2hsb(
                flatColor != null ? flatColor : WorldRecolorConfig.DEFAULT_TILE_FLAT_COLOR);

        if (hueReduction == lastHueReduction
            && saturationReduction == lastSaturationReduction
            && lightnessReduction == lastLightnessReduction) {
            return;
        }

        fillMap(hueMap, hueReduction, Colors.MIN_HUE, Colors.MAX_HUE);
        fillMap(saturationMap, saturationReduction, Colors.MIN_SATURATION, Colors.MAX_SATURATION);
        fillMap(lightnessMap, lightnessReduction, Colors.MIN_LIGHTNESS, Colors.MAX_LIGHTNESS);

        this.lastHueReduction = hueReduction;
        this.lastSaturationReduction = saturationReduction;
        this.lastLightnessReduction = lightnessReduction;
    }

    public int getModifiedHsl(int hsl) {
        if (hsl == 12_345_678 || hsl < Colors.MIN_HSL || hsl > Colors.MAX_HSL) {
            return hsl;
        }
        if (this.useFlatColor) {
            return this.flatColor;
        }
        return Colors.packJagexHsl(
            hueMap[Colors.unpackJagexHue(hsl)],
            saturationMap[Colors.unpackJagexSaturation(hsl)],
            lightnessMap[Colors.unpackJagexLightness(hsl)]);
    }

    private static int[] identity(int size) {
        int[] map = new int[size];
        for (int i = 0; i < size; i++) {
            map[i] = i;
        }
        return map;
    }

    private static void fillMap(int[] map, int reductionPercent, int min, int max) {
        for (int value = 0; value < map.length; value++) {
            map[value] = getReducedAmount(value, reductionPercent, min, max);
        }
    }

    private static int getReducedAmount(int value, int reductionPercent, int min, int max) {
        return Utils.clamp(
            (int) Math.ceil(((100 - reductionPercent) / 100.0) * value),
            min,
            max);
    }
}
