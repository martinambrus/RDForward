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

    /**
     * Set the player's flight-speed multiplier and push the change to the
     * client. Bukkit semantics — value range -1.0 to 1.0, API default
     * 0.1 (the on-wire {@code flyingSpeed} field is value/2, matching
     * CraftPlayer.setFlySpeed). Negative values reverse the flight
     * direction. Implementations should throw
     * {@link IllegalArgumentException} for values outside the range.
     * Older protocols that have no runtime fly-speed field silently no-op.
     */
    default void setFlySpeed(float speed) {}

    /**
     * Set the player's walk-speed multiplier and push the change to the
     * client. Bukkit semantics — value range -1.0 to 1.0, API default
     * 0.2 (on-wire walk + movement-speed attribute are both value/2,
     * matching CraftPlayer.setWalkSpeed). Implementations should throw
     * {@link IllegalArgumentException} for values outside the range.
     * Older protocols that have no runtime walk-speed field silently no-op.
     */
    default void setWalkSpeed(float speed) {}

    /** @return the last value set via {@link #setFlySpeed}, or 0.1f
     *  (Bukkit's API default) if never set. */
    default float getFlySpeed() { return 0.1f; }

    /** @return the last value set via {@link #setWalkSpeed}, or 0.2f
     *  (Bukkit's API default) if never set. */
    default float getWalkSpeed() { return 0.2f; }

    /**
     * Set the player's gamemode (0=survival, 1=creative, 2=adventure,
     * 3=spectator) and push the change to the client. The implementation
     * clamps the value down to what the connecting client's wire format
     * supports — a Beta 1.8 client receives {@code adventure} as
     * {@code survival}, a 1.7.x client receives {@code spectator} as
     * {@code survival}, and Classic / RubyDung / Indev / pre-Beta-1.8
     * sessions silently no-op (no runtime-gamemode-change packet exists
     * on the wire).
     */
    default void setGameMode(int gameMode) {}

    /** @return the player's current gamemode int. Defaults to 1
     *  (creative) so callers that read before {@link #setGameMode} has
     *  ever been invoked see the conventional sandbox-server starting
     *  mode rather than null/zero. Implementations may seed from
     *  server.properties at session start. */
    default int getGameMode() { return 1; }
}
