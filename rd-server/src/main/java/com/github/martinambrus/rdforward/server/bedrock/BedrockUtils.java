package com.github.martinambrus.rdforward.server.bedrock;

import com.github.martinambrus.rdforward.server.api.ServerProperties;
import org.cloudburstmc.protocol.bedrock.data.GameType;

/**
 * Shared utilities for Bedrock protocol handlers.
 */
final class BedrockUtils {

    private BedrockUtils() {}

    /**
     * Map the configured server game mode (int 0/1/2) to the Bedrock GameType enum.
     */
    static GameType getConfiguredGameType() {
        switch (ServerProperties.getGameMode()) {
            case 0: return GameType.SURVIVAL;
            case 2: return GameType.ADVENTURE;
            default: return GameType.CREATIVE;
        }
    }
}
