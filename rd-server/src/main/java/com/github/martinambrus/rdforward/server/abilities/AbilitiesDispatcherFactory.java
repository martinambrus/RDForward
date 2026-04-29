package com.github.martinambrus.rdforward.server.abilities;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;

/**
 * Selects the right {@link AbilitiesSpeedDispatcher} for a given session
 * once at first use. Branching happens here, not on every push, so each
 * session subsequently runs only its own codec's code path.
 */
public final class AbilitiesDispatcherFactory {

    private AbilitiesDispatcherFactory() {}

    public static AbilitiesSpeedDispatcher forSession(ConnectedPlayer player) {
        if (player.getBedrockSession() != null) {
            return BedrockAbilitiesDispatcher.INSTANCE;
        }
        if (player.getMcpeSession() != null) {
            // Legacy MCPE has no runtime fly/walk speed field exposed.
            return NoOpAbilitiesDispatcher.INSTANCE;
        }
        ProtocolVersion v = player.getProtocolVersion();
        if (v == null) {
            return NoOpAbilitiesDispatcher.INSTANCE;
        }
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_6_1)) {
            return V73FloatAbilitiesDispatcher.INSTANCE;
        }
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            return V39ByteAbilitiesDispatcher.INSTANCE;
        }
        return NoOpAbilitiesDispatcher.INSTANCE;
    }
}
