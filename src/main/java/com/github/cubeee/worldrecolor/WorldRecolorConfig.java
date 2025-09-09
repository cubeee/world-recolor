package com.github.cubeee.worldrecolor;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

import static com.github.cubeee.worldrecolor.ConfigKeys.PLUGIN_CONFIG_GROUP_NAME;

@ConfigGroup(PLUGIN_CONFIG_GROUP_NAME)
public interface WorldRecolorConfig extends Config {

	@ConfigSection(
		name = "Tiles",
		description = "",
		position = 0
	)
	String tileRecolors = "Tiles";

	@ConfigItem(
		keyName = ConfigKeys.RECOLOR_TILES,
		name = "Recolor tiles",
		description = "",
		section = tileRecolors
	)
	default boolean isRecolorTiles() {
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.TILE_HUE,
		name = "Adjusted hue",
		description = "",
		position = 0,
		section = tileRecolors
	)
	@Range(max = 63)
	default int getAdjustedTileHue() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.TILE_SATURATION,
		name = "Adjusted saturation",
		description = "",
		position = 1,
		section = tileRecolors
	)
	@Range(max = 7)
	default int getAdjustedTileSaturation() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.TILE_LIGHTNESS_REDUCTION,
		name = "Lightness reduction",
		description = "",
		position = 2,
		section = tileRecolors
	)
	@Units(Units.PERCENT)
	@Range(max = 100)
	default int getLightnessReduction() {
		return 80;
	}
}
