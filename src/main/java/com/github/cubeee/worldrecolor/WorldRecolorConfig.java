package com.github.cubeee.worldrecolor;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(ConfigKeys.PLUGIN_CONFIG_GROUP_NAME)
public interface WorldRecolorConfig extends Config {
	String COX_REGIONS = "13136,13137,13393,13138,13394,13139,13395,13140,13396,13141,13397,13145,13401,12889";
	String TOB_REGIONS = "12613,13125,13122,13123,13379,12612,12611";
	String TOA_REGIONS = "14160,15698,15700,14162,14164,15186,15188,14674,14676,15184,15696";

	@ConfigSection(
		name = "Tiles",
		description = "",
		position = ConfigKeys.TILES_SECTION_POSITION
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

	// ---
	// --- ADVANCED
	// ---
	@ConfigSection(
		name = "Advanced",
		description = "",
		position = ConfigKeys.ADVANCED_SECTION_POSITION,
		closedByDefault = true
	)
	String advancedSection = "Advanced";

	@ConfigItem(
		keyName = ConfigKeys.INCLUDED_REGION_IDS,
		name = "Included region ids",
		description = "Only recolor inside these regions. Anything added here will always be used instead of excluded regions! Separated by commas and/or new lines.",
		position = 1,
		section = advancedSection
	)
	default String getIncludedRegionIds() {
		return "";
	}

	@ConfigItem(
		keyName = ConfigKeys.EXCLUDED_REGION_IDS,
		name = "Excluded region ids",
		description = "Recolor everywhere but inside these regions. Used when included regions is empty. Separated by commas and/or new lines.",
		position = 2,
		section = advancedSection
	)
	default String getExcludedRegionIds() {
		return COX_REGIONS + "," + TOB_REGIONS + "," + TOA_REGIONS;
	}
}
