package com.github.cubeee.worldrecolor;

import com.google.common.base.Joiner;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.PreMapLoad;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

@Slf4j
@PluginDescriptor(
	name = "World Recolor"
)
public class WorldRecolorPlugin extends Plugin {
	public static final int NEXT_REFRESH_UNSET = -1;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private WorldRecolorConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ConfigManager configManager;

    private boolean showMenuOptions;
	private int nextReloadTick = NEXT_REFRESH_UNSET;
	private final ColorMap tileColorMap = new ColorMap();

	private final List<Integer> includedRegionIds;
	private final List<Integer> excludedRegionIds;

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.getMenuOptionsKeybind()) {
		@Override
		public void hotkeyPressed() {
			showMenuOptions = true;
		}

		@Override
		public void hotkeyReleased() {
			showMenuOptions = false;
		}
	};

	public WorldRecolorPlugin() {
		super();
		this.includedRegionIds = new ArrayList<>();
		this.excludedRegionIds = new ArrayList<>();
	}

	@Override
	protected void startUp() {
		loadRegionIds();
		reloadMap();
		keyManager.registerKeyListener(hotkeyListener);
	}

	@Override
	protected void shutDown() {
		reloadMap();
		keyManager.unregisterKeyListener(hotkeyListener);
	}

	public void reloadMap() {
		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN) {
				client.setGameState(GameState.LOADING);
			}
		});
	}

    @Subscribe
	@SuppressWarnings({"unused", "PMD.CyclomaticComplexity"})
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(ConfigKeys.PLUGIN_CONFIG_GROUP_NAME)) {
			return;
		}

		String key = event.getKey();

		boolean triggerUpdate = true;

		loadRegionIds();

		// No map reload on irrelevant configs
		if (key.equals(ConfigKeys.ENABLE_MENU_OPTIONS)
			|| key.equals(ConfigKeys.MENU_OPTIONS_HOTKEY)) {
			triggerUpdate = false;
		}

		// Trigger map reloads on tile color adjustments and only if tile recoloring is enabled
		if (!config.isRecolorTiles()
				&& (key.equals(ConfigKeys.TILE_HUE_REDUCTION)
				|| key.equals(ConfigKeys.TILE_SATURATION_REDUCTION)
				|| key.equals(ConfigKeys.TILE_LIGHTNESS_REDUCTION))) {
			triggerUpdate = false;
		}

		// Prevent excessive map reloads when config changes are spammed by running it on the next game tick
		if (triggerUpdate) {
			nextReloadTick = client.getTickCount() + 1;
		}
	}

    @Subscribe
	@SuppressWarnings("unused")
	public void onPreMapLoad(PreMapLoad preMapLoad) {
		long start = System.nanoTime();
		recolorMap(preMapLoad.getScene());
		long end = System.nanoTime();
		long duration = end - start;
		log.debug("Map recolor done in {}ms", duration / 1_000_000);
	}

    @Subscribe
	@SuppressWarnings("unused")
	public void onGameTick(GameTick gameTick) {
		if (nextReloadTick != NEXT_REFRESH_UNSET && client.getTickCount() >= nextReloadTick) {
			reloadMap();
			nextReloadTick = NEXT_REFRESH_UNSET;
		}
	}

	@Subscribe
	@SuppressWarnings("unused")
	public void onMenuOpened(MenuOpened event) {
		MenuEntry[] menuEntries = event.getMenuEntries();
		if (config.isMenuOptionsEnabled() && showMenuOptions && hasWalkHereOption(menuEntries)) {
			addContextMenuEntry(menuEntries.length - 1);
		}
	}

	private boolean hasWalkHereOption(MenuEntry... menuEntries) {
		if (menuEntries == null) {
			return false;
		}
		for (MenuEntry menuEntry : menuEntries) {
			if (menuEntry.getType() == MenuAction.WALK) {
				return true;
			}
		}
		return false;
	}

	private void addContextMenuEntry(int index) {
		Tile selectedTile = client.getTopLevelWorldView().getSelectedSceneTile();
		if (selectedTile == null) {
			return;
		}

		WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, selectedTile.getLocalLocation());
		if (worldPoint == null) {
			return;
		}

		int regionId = worldPoint.getRegionID();
		boolean isIncluded = includedRegionIds.contains(regionId);
		boolean isExcluded = excludedRegionIds.contains(regionId);
		String includeText = isIncluded ? "Remove from included regions": "Add to included regions";
		String excludeText = isExcluded ? "Remove from excluded regions" : "Add to excluded regions";

		MenuEntry groupEntry = client.getMenu().createMenuEntry(index)
			.setOption("World Recolor")
			.setType(MenuAction.RUNELITE);

		Menu subMenu = groupEntry.createSubMenu();

		subMenu.createMenuEntry(0)
			.setOption(includeText)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> {
				if (isIncluded) {
					includedRegionIds.removeIf(i -> i == regionId);
				} else {
					includedRegionIds.add(regionId);
				}
				configManager.setConfiguration(
						ConfigKeys.PLUGIN_CONFIG_GROUP_NAME,
						ConfigKeys.INCLUDED_REGION_IDS,
						Joiner.on(",").join(includedRegionIds));
			});

		subMenu.createMenuEntry(1)
			.setOption(excludeText)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> {
				if (isExcluded) {
					excludedRegionIds.removeIf(i -> i == regionId);
				} else {
					excludedRegionIds.add(regionId);
				}
				configManager.setConfiguration(
						ConfigKeys.PLUGIN_CONFIG_GROUP_NAME,
						ConfigKeys.EXCLUDED_REGION_IDS,
						Joiner.on(",").join(excludedRegionIds));
			});
	}

    @Provides
	@SuppressWarnings({"unused", "PMD.CommentDefaultAccessModifier"})
	WorldRecolorConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(WorldRecolorConfig.class);
	}

	private void recolorMap(Scene scene) {
		boolean recolorTiles = config.isRecolorTiles();

		if (!recolorTiles) {
			return;
		}

		int tileHueReduction = config.getTileHueReduction();
		int tileSaturationReduction = config.getTileSaturationReduction();
		int tileLightnessReduction = config.getTileLightnessReduction();

		// Cache the whole new adjusted color palette at once before recoloring the map
		long colorMapsStart = System.nanoTime();
		tileColorMap.updateColors(tileHueReduction, tileSaturationReduction, tileLightnessReduction);
		long colorMapsEnd = System.nanoTime();

		boolean isInstance = scene.isInstance();
		log.debug("Recolor map... instance={}, tiles={}", isInstance, recolorTiles);
		Tile[][][] tiles = isInstance ? scene.getTiles() : scene.getExtendedTiles();

		long tilesDuration = 0;

		for (Tile[][] zTiles : tiles) {
			for (Tile[] xTiles : zTiles) {
				for (Tile tile : xTiles) {
					if (canRecolorTile(scene, tile)) {
						tilesDuration += recolorTile(tile);
					}
				}
			}
		}

		log.debug("Color maps updated in {}ms, tiles colored in {}ms",
			(colorMapsEnd - colorMapsStart) / 1_000_000,
			tilesDuration / 1_000_000
		);
	}

	private long recolorTile(Tile tile) {
		long start = System.nanoTime();
		SceneTilePaint paint = tile.getSceneTilePaint();
		if (paint != null && paint.getTexture() == -1) {
			int newNw = tileColorMap.getModifiedHsl(paint.getNwColor());
			int newNe = tileColorMap.getModifiedHsl(paint.getNeColor());
			int newSw = tileColorMap.getModifiedHsl(paint.getSwColor());
			int newSe = tileColorMap.getModifiedHsl(paint.getSeColor());

			paint.setNwColor(newNw);
			paint.setNeColor(newNe);
			paint.setSwColor(newSw);
			paint.setSeColor(newSe);

			tile.setSceneTilePaint(paint);
		}

		SceneTileModel model = tile.getSceneTileModel();
		if (model != null) {
			ColorAdjuster.adjustSceneTileModel(model, tileColorMap);
			tile.setSceneTileModel(model);
		}

		long end = System.nanoTime();
		return end - start;
	}

	private boolean canRecolorTile(Scene scene, Tile tile) {
		if (tile == null) {
			return false;
		}
		if (includedRegionIds.isEmpty() && excludedRegionIds.isEmpty()) {
			return true;
		}
		WorldPoint worldPoint = WorldPoint.fromLocalInstance(scene, tile.getLocalLocation(), tile.getPlane());
		return canRecolorRegion(worldPoint.getRegionID());
	}

	private boolean canRecolorRegion(int regionId) {
		if (!includedRegionIds.isEmpty()) {
			return includedRegionIds.contains(regionId);
		}
		return !excludedRegionIds.contains(regionId);
	}

	private void loadRegionIds() {
		includedRegionIds.clear();
		excludedRegionIds.clear();

		String includedRegionIdsString = config.getIncludedRegionIds();
		String excludedRegionIdsString = config.getExcludedRegionIds();
		if (includedRegionIdsString.isEmpty() && excludedRegionIdsString.isEmpty()) {
			return;
		}

		includedRegionIds.addAll(
			Pattern.compile("[,\\n]")
				.splitAsStream(includedRegionIdsString)
				.filter(Predicate.not(String::isEmpty))
				.map(Integer::valueOf)
				.collect(Collectors.toList()));

		excludedRegionIds.addAll(
			Pattern.compile("[,\\n]")
				.splitAsStream(excludedRegionIdsString)
					.filter(Predicate.not(String::isEmpty))
					.map(Integer::valueOf)
					.collect(Collectors.toList()));

		if (log.isDebugEnabled()) {
			log.debug("Included region ids: {}, excluded region ids: {}", includedRegionIds.size(), excludedRegionIds.size());
		}
	}

}
