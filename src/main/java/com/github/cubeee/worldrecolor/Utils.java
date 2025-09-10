package com.github.cubeee.worldrecolor;

public final class Utils {
    private Utils() {}

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
