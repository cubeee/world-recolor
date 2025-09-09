package com.github.cubeee.worldrecolor;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WorldRecolorPluginTest {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(WorldRecolorPlugin.class);
		RuneLite.main(args);
	}
}
