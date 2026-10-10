package com.github.cubeee.worldrecolor;

import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;

public class ColorMapTest {
    private static final int[][] REDUCTIONS = {
        {0, 0, 0},
        {0, 0, 50},
        {10, 20, 30},
        {100, 100, 100},
        {-50, -100, -200},
        {-25, 40, -75},
    };

    @Test
    public void getModifiedHslMatchesPerColorCalculation() {
        for (int[] r : REDUCTIONS) {
            ColorMap colorMap = new ColorMap();
            colorMap.updateColors(r[0], r[1], r[2], false, null);
            for (int hsl = Colors.MIN_HSL; hsl <= Colors.MAX_HSL; hsl++) {
                assertEquals("hsl=" + hsl, expectedHsl(hsl, r[0], r[1], r[2]), colorMap.getModifiedHsl(hsl));
            }
        }
    }

    @Test
    public void unmodifiedBeforeUpdate() {
        ColorMap colorMap = new ColorMap();
        for (int hsl = Colors.MIN_HSL; hsl <= Colors.MAX_HSL; hsl++) {
            assertEquals(hsl, colorMap.getModifiedHsl(hsl));
        }
    }

    @Test
    public void outOfRangeAndHiddenColorsAreUnchanged() {
        ColorMap colorMap = new ColorMap();
        colorMap.updateColors(50, 50, 50, false, null);
        assertEquals(12_345_678, colorMap.getModifiedHsl(12_345_678));
        assertEquals(-1, colorMap.getModifiedHsl(-1));
        assertEquals(Colors.MAX_HSL + 1, colorMap.getModifiedHsl(Colors.MAX_HSL + 1));
    }

    @Test
    public void flatColorOverridesAdjustments() {
        ColorMap colorMap = new ColorMap();
        colorMap.updateColors(50, 50, 50, true, Color.RED);
        int flat = Colors.colorToRs2hsb(Color.RED);
        assertEquals(flat, colorMap.getModifiedHsl(1234));
    }

    private static int expectedHsl(int hsl, int hueReduction, int satReduction, int lightnessReduction) {
        int hue = reduce(Colors.unpackJagexHue(hsl), hueReduction, Colors.MIN_HUE, Colors.MAX_HUE);
        int sat = reduce(Colors.unpackJagexSaturation(hsl), satReduction, Colors.MIN_SATURATION, Colors.MAX_SATURATION);
        int lightness = reduce(Colors.unpackJagexLightness(hsl), lightnessReduction, Colors.MIN_LIGHTNESS, Colors.MAX_LIGHTNESS);
        return Colors.packJagexHsl(hue, sat, lightness);
    }

    private static int reduce(int value, int reductionPercent, int min, int max) {
        return Utils.clamp((int) Math.ceil(((100 - reductionPercent) / 100.0) * value), min, max);
    }
}
