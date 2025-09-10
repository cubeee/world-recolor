package com.github.cubeee.worldrecolor;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PreMapLoad;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

@Slf4j
@PluginDescriptor(
	name = "World Recolor"
)
public class WorldRecolorPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private WorldRecolorConfig config;

	private Integer nextReloadTick = null;
	private final ColorMap tileColorMap = new ColorMap();

	private final List<Integer> includedRegionIds = new ArrayList<>();
	private final List<Integer> excludedRegionIds = new ArrayList<>();

	@Override
	protected void startUp() {
		loadRegionIds();
		reloadMap();
	}

	@Override
	protected void shutDown() {
		reloadMap();
	}

	public void reloadMap() {
		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN) {
				client.setGameState(GameState.LOADING);
			}
		});
	}

    @Subscribe
	@SuppressWarnings("unused")
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(ConfigKeys.PLUGIN_CONFIG_GROUP_NAME)) {
			return;
		}

		String key = event.getKey();

		boolean triggerUpdate = true;

		if (key.equals(ConfigKeys.INCLUDED_REGION_IDS) || key.equals(ConfigKeys.EXCLUDED_REGION_IDS)) {
			loadRegionIds();
		}

		// Trigger map reloads on tile color adjustments only if tile recoloring is enabled
		if (!config.isRecolorTiles()
				&& (key.equals(ConfigKeys.TILE_HUE)
				|| key.equals(ConfigKeys.TILE_SATURATION)
				|| key.equals(ConfigKeys.TILE_LIGHTNESS_REDUCTION))) {
			triggerUpdate = false;
		}

		// Prevent excessive map reloads when config changes are spammed by running them on the next game tick
		if (nextReloadTick == null && triggerUpdate) {
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
		if (nextReloadTick != null && client.getTickCount() >= nextReloadTick) {
			reloadMap();
			nextReloadTick = null;
		}
	}

    @Provides
	@SuppressWarnings("unused")
	WorldRecolorConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(WorldRecolorConfig.class);
	}

	private void recolorMap(Scene scene) {
		boolean recolorTiles = config.isRecolorTiles();
		int tileHueShift = config.getAdjustedTileHue();
		int tileSaturationShift = config.getAdjustedTileSaturation();
		int tileLightnessReduction = config.getLightnessReduction();

		// Cache the whole new adjusted color palette at once before recoloring the map
		long colorMapsStart = System.nanoTime();
		tileColorMap.updateColors(tileHueShift, tileSaturationShift, tileLightnessReduction);
		long colorMapsEnd = System.nanoTime();

		log.debug("Recolor map... tiles={}", recolorTiles);
		Tile[][][] tiles = scene.getExtendedTiles();

		long tilesDuration = 0;

        for (Tile[][] zTiles : tiles) {
            for (Tile[] xTiles : zTiles) {
                for (Tile tile : xTiles) {
                    if (tile == null || !canRecolorRegion(scene, tile)) {
                        continue;
                    }

                    tilesDuration += recolorMap(tile, recolorTiles);
                }
            }
        }

		log.debug("Color maps updated in {}ms, tiles colored in {}ms",
			(colorMapsEnd - colorMapsStart) / 1_000_000,
			tilesDuration / 1_000_000
		);
	}

	private long recolorMap(Tile tile, boolean recolor) {
		if (!recolor) {
			return 0;
		}
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

	public boolean canRecolorRegion(Scene scene, Tile tile) {
		if (includedRegionIds.isEmpty() && excludedRegionIds.isEmpty()) {
			return true;
		}
		if (tile.getPlane() < 0) {
			return true;
		}
		WorldPoint wp = WorldPoint.fromLocalInstance(scene, tile.getLocalLocation(), tile.getPlane());
		int regionId = wp.getRegionID();

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

		log.debug("Included region ids: {}, excluded region ids: {}", includedRegionIds.size(), excludedRegionIds.size());
	}

}
