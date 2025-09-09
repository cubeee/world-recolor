package com.github.cubeee.worldrecolor;

public class Utils {
    private Utils() {}

    public static int clamp(int v, int min, int max) {
        return Math.min(Math.max(v, min), max);
    }
}
