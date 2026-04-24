package com.github.martinambrus.rdforward.api.player;

import com.github.martinambrus.rdforward.api.version.ProtocolVersion;
import com.github.martinambrus.rdforward.api.version.VersionCapability;
import com.github.martinambrus.rdforward.api.world.Location;

/**
 * Connected player — abstraction over Netty, Alpha, and Bedrock sessions.
 *
 * <p>Version-conditional methods have default no-op implementations. Callers
 * may dispatch unconditionally; unsupported features silently do nothing on
 * old clients.
 */
public interface Player {

    String getName();

    Location getLocation();

    void teleport(Location location);

    void sendMessage(String message);

    ProtocolVersion getProtocolVersion();

    boolean isOp();

    void kick(String reason);

    /** True if this player's protocol supports the given capability. */
    default boolean supportsCapability(VersionCapability capability) {
        return capability != null && capability.isSupported(getProtocolVersion());
    }

    // --- Version-conditional methods. Default: no-op. ---
    default void sendActionBar(String message) {}
    default void setTabListHeader(String header) {}
    default void setTabListFooter(String footer) {}
    default void sendTitle(String title, String subtitle) {}
}
