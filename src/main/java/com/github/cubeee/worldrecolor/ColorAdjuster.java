package com.github.cubeee.worldrecolor;

import net.runelite.api.SceneTileModel;

public class ColorAdjuster {
    private ColorAdjuster() {}

    public static void adjustSceneTileModel(SceneTileModel model, ColorMap colorMap) {
        adjustColors(model.getTriangleColorA(), model.getTriangleTextureId(), colorMap);
        adjustColors(model.getTriangleColorB(), model.getTriangleTextureId(), colorMap);
        adjustColors(model.getTriangleColorC(), model.getTriangleTextureId(), colorMap);
    }

    public static void adjustColors(int[] colors, int[] textures, ColorMap colorMap) {
        for (int i = 0; i < colors.length; i++) {
            if (textures != null && textures.length >= i && textures[i] != -1) {
                continue;
            }
            colors[i] = colorMap.getModifiedHsl(colors[i]);
        }
    }
}
