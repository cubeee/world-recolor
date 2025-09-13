package com.github.cubeee.worldrecolor;

public class ColorMap {
    private final Integer[] modifiedColors;

    private int lastHueReduction;
    private int lastSaturationReduction;
    private int lastLightnessReduction;

    public ColorMap() {
        this.modifiedColors = new Integer[Colors.MAX_HSL];
    }

    public void updateColors(int hueReduction, int saturationReduction, int lightnessReduction) {
        if (hueReduction == lastHueReduction
            && saturationReduction == lastSaturationReduction
            && lightnessReduction == lastLightnessReduction) {
            return;
        }

        for (int hsl = 0; hsl < modifiedColors.length; hsl++) {
            int modified = getNewHsl(hsl, hueReduction, saturationReduction, lightnessReduction);
            modifiedColors[hsl] = modified;
        }

        this.lastHueReduction = hueReduction;
        this.lastSaturationReduction = saturationReduction;
        this.lastLightnessReduction = lightnessReduction;
    }

    public int getModifiedHsl(int hsl) {
        if (hsl == 12_345_678 || hsl < 0 || hsl > modifiedColors.length - 1) {
            return hsl;
        }
        Integer modified = modifiedColors[hsl];
        return modified == null ? hsl : modified;
    }

    private int getNewHsl(int hsl, int hueReduction, int satReduction, int lightnessReduction) {
        if (hsl == 12_345_678 || hsl < Colors.MIN_HSL || hsl > Colors.MAX_HSL) {
            return hsl;
        }

        if (hueReduction == 0 && satReduction == 0 && lightnessReduction == 0) {
            return hsl;
        }

        int[] unpackedHsl = Colors.getUnpackedJagexHsl(hsl);

        int newHue = getReducedAmount(unpackedHsl[0], hueReduction, Colors.MIN_HUE, Colors.MAX_HUE);
        int newSaturation = getReducedAmount(unpackedHsl[1], satReduction, Colors.MIN_SATURATION, Colors.MAX_SATURATION);
        int newLightness = getReducedAmount(unpackedHsl[2], lightnessReduction, Colors.MIN_LIGHTNESS, Colors.MAX_LIGHTNESS);

        return Colors.packJagexHsl(newHue, newSaturation, newLightness);
    }

    private int getReducedAmount(int value, int reductionPercent, int min, int max) {
        return Utils.clamp(
            (int) Math.ceil(((100 - reductionPercent) / 100.0) * value),
            min,
            max);
    }

}
