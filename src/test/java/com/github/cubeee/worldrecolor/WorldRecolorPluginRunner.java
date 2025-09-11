package com.github.cubeee.worldrecolor;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class WorldRecolorPluginRunner {
	private WorldRecolorPluginRunner() {}

	@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "unchecked"})
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(WorldRecolorPlugin.class);
		RuneLite.main(args);
	}
}
