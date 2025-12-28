package com.github.cubeee.worldrecolor;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
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
	String TILE_RECOLORS_SECTION = "Tiles";

	@ConfigSection(
		name = "Regions",
		description = "",
		position = ConfigKeys.REGIONS_SECTION_POSITION,
		closedByDefault = true
	)
	String REGIONS_SECTION = "Regions";

	// ---
	// --- TILES
	// ---

	@ConfigItem(
		keyName = ConfigKeys.RECOLOR_TILES,
		name = "Recolor tiles",
		description = "",
		section = TILE_RECOLORS_SECTION
	)
	default boolean isRecolorTiles() {
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.TILE_HUE_REDUCTION,
		name = "Hue reduction",
		description = "",
		position = 0,
		section = TILE_RECOLORS_SECTION
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getTileHueReduction() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.TILE_SATURATION_REDUCTION,
		name = "Saturation reduction",
		description = "",
		position = 1,
		section = TILE_RECOLORS_SECTION
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getTileSaturationReduction() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.TILE_LIGHTNESS_REDUCTION,
		name = "Lightness reduction",
		description = "",
		position = 2,
		section = TILE_RECOLORS_SECTION
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getTileLightnessReduction() {
		return 80;
	}

	// ---
	// --- REGIONS
	// ---

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_MENU_OPTIONS,
		name = "Enable menu options",
		description = "",
		section = REGIONS_SECTION,
		position = 0
	)
	default boolean enableMenuOptions() {
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.MENU_OPTIONS_HOTKEY,
		name = "Menu options keybind",
		description = "Show menu options when this key is held down",
		section = REGIONS_SECTION,
		position = 1
	)
	default Keybind getMenuOptionsKeybind() {
		return Keybind.CTRL;
	}

	@ConfigItem(
		keyName = ConfigKeys.INCLUDED_REGION_IDS,
		name = "Included region ids",
		description = "Only recolor inside these regions. Anything added here will always be used instead of excluded regions! Separated by commas and/or new lines.",
		position = 2,
		section = REGIONS_SECTION
	)
	default String getIncludedRegionIds() {
		return "";
	}

	@ConfigItem(
		keyName = ConfigKeys.INCLUDED_REGION_IDS,
		name = "",
		description = ""
	)
	void setIncludedRegionIds(String ids);

	@ConfigItem(
		keyName = ConfigKeys.EXCLUDED_REGION_IDS,
		name = "Excluded region ids",
		description = "Recolor everywhere but inside these regions. Used when included regions is empty. Separated by commas and/or new lines.",
		position = 3,
		section = REGIONS_SECTION
	)
	default String getExcludedRegionIds() {
		return COX_REGIONS + "," + TOB_REGIONS + "," + TOA_REGIONS;
	}

	@ConfigItem(
		keyName = ConfigKeys.EXCLUDED_REGION_IDS,
		name = "",
		description = ""
	)
	void setExcludedRegionIds(String ids);
}
