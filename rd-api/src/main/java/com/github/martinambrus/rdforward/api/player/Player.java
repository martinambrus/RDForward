package com.github.martinambrus.rdforward.api.player;

import com.github.martinambrus.rdforward.api.inventory.PlayerInventoryView;
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

    /**
     * @return the remote socket address of this player's connection, or
     *         {@code null} if unavailable (e.g. test fixtures, Bedrock
     *         sessions without a Netty channel).
     */
    default java.net.InetSocketAddress getAddress() { return null; }

    /** True if this player's protocol supports the given capability. */
    default boolean supportsCapability(VersionCapability capability) {
        return capability != null && capability.isSupported(getProtocolVersion());
    }

    /**
     * @return mutable view onto this player's server-side inventory, or
     *         {@code null} if the implementation does not back inventory
     *         state (test stubs, sessions before login completes).
     *         Mutations dispatch the resulting state to the client.
     */
    default PlayerInventoryView getInventory() { return null; }

    // --- Version-conditional methods. Default: no-op. ---
    default void sendActionBar(String message) {}
    default void setTabListHeader(String header) {}
    default void setTabListFooter(String footer) {}
    default void sendTitle(String title, String subtitle) {}
}
