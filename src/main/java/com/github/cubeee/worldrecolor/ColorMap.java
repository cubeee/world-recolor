package com.github.cubeee.worldrecolor;

public class ColorMap {
    private final Integer[] modifiedColors;

    private int lastHueShift;
    private int lastSaturationShift;
    private int lastLightnessReduction;

    public ColorMap() {
        this.modifiedColors = new Integer[Colors.MAX_HSL];
    }

    public void updateColors(int hueShift, int saturationShift, int lightnessReduction) {
        if (hueShift == lastHueShift
            && saturationShift == lastSaturationShift
            && lightnessReduction == lastLightnessReduction) {
            return;
        }

        for (int hsl = 0; hsl < modifiedColors.length; hsl++) {
            int modified = getNewHsl(hsl, hueShift, saturationShift, lightnessReduction);
            modifiedColors[hsl] = modified;
        }

        this.lastHueShift = hueShift;
        this.lastSaturationShift = saturationShift;
        this.lastLightnessReduction = lightnessReduction;
    }

    public int getModifiedHsl(int hsl) {
        if (hsl == 12_345_678 || hsl < 0 || hsl > modifiedColors.length - 1) {
            return hsl;
        }
        Integer modified = modifiedColors[hsl];
        return modified == null ? hsl : modified;
    }

    private int getNewHsl(int hsl, int hueShift, int satShift, int lightnessReduction) {
        if (hsl == 12_345_678 || hsl < Colors.MIN_HSL || hsl > Colors.MAX_HSL) {
            return hsl;
        }

        if (hueShift == 0 && satShift == 0 && lightnessReduction == 0) {
            return hsl;
        }

        int[] unpackedHsl = Colors.getUnpackedJagexHsl(hsl);

        int newHue = Utils.clamp(unpackedHsl[0] + hueShift, Colors.MIN_HUE, Colors.MAX_HUE);
        int newSaturation = Utils.clamp(unpackedHsl[1] + satShift, Colors.MIN_SATURATION, Colors.MAX_SATURATION);
        int newLightness = Utils.clamp(
            (int) Math.ceil(((100 - lightnessReduction) / 100.0) * unpackedHsl[2]),
            Colors.MIN_LIGHTNESS,
            Colors.MAX_LIGHTNESS);

        return Colors.packJagexHsl(newHue, newSaturation, newLightness);
    }

}
