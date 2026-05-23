package com.github.cubeee.worldrecolor;

import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.ColorUtil;

import java.awt.Color;
import java.util.*;

public class VersionMessageManager {
    private static final String LAST_SEEN_MESSAGE_CONFIG_KEY = "lastSeenMessage";

    private static final String[] MESSAGES = {
            "Tiles can now be recolored with a flat color.",
            "Tile colors can now be adjusted with much larger percentages."
    };

    public void sendUnseenVersionMessages(ChatMessageManager chatMessageManager, ConfigManager configManager) {
        Integer lastSeenMessageIndex = getLastSeenMessageIndex(configManager);

        // Do nothing when message index is saved and we have seen everything already
        if (lastSeenMessageIndex != null && lastSeenMessageIndex >= MESSAGES.length - 1) {
            return;
        }

        boolean isFresh = isAssumedFreshInstall(configManager);

        // Skip showing messages for fresh installs, but save latest message index so only later ones will be shown
        if (lastSeenMessageIndex == null && isFresh) {
            saveLastSeenMessageIndex(configManager);
            return;
        }

        // Migrate previous users by showing them all the messages from the time of the implementation
        if (lastSeenMessageIndex == null) {
            lastSeenMessageIndex = 0;
        }

        String messagePrefix = ColorUtil.wrapWithColorTag("[World Recolor] ", Color.GREEN);
        for (int i = lastSeenMessageIndex; i < MESSAGES.length; i++) {
            String message = MESSAGES[i];

            chatMessageManager.queue(QueuedMessage.builder()
                            .type(ChatMessageType.GAMEMESSAGE)
                            .runeLiteFormattedMessage(messagePrefix + message)
                    .build());
        }

        saveLastSeenMessageIndex(configManager);
    }

    private Integer getLastSeenMessageIndex(ConfigManager configManager) {
        return configManager.getConfiguration(ConfigKeys.PLUGIN_CONFIG_GROUP_NAME, LAST_SEEN_MESSAGE_CONFIG_KEY, Integer.class);
    }

    private void saveLastSeenMessageIndex(ConfigManager configManager) {
        configManager.setConfiguration(ConfigKeys.PLUGIN_CONFIG_GROUP_NAME, LAST_SEEN_MESSAGE_CONFIG_KEY, MESSAGES.length - 1);
    }

    private boolean isAssumedFreshInstall(ConfigManager configManager) {
        String groupName = ConfigKeys.PLUGIN_CONFIG_GROUP_NAME;
        List<String> configKeys = configManager.getConfigurationKeys(groupName);
        for (String key : configKeys) {
            String configKey = key.substring(groupName.length() + 1);
            Object config = configManager.getConfiguration(groupName, configKey);
            if (config != null) {
                return false;
            }
        }
        return true;
    }
}
