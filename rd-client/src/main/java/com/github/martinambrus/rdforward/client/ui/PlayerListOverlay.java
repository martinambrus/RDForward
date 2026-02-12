package com.github.martinambrus.rdforward.client.ui;

import com.github.martinambrus.rdforward.multiplayer.MultiplayerState;
import com.github.martinambrus.rdforward.multiplayer.RemotePlayer;

import java.util.Collection;

/**
 * Overlay for the in-game player list, shown while the Tab key is held.
 *
 * Displays a list of all connected players (names, optionally ping).
 * The overlay is transparent and does not capture input — the player
 * can continue moving while viewing the list.
 *
 * Data source: {@link MultiplayerState#getRemotePlayers()} provides
 * all remote players. The local player name comes from
 * {@link com.github.martinambrus.rdforward.client.RDClient#getUsername()}.
 *
 * This is a skeleton for the Alpha stage. The actual rendering will
 * be implemented when the Alpha client UI is built.
 */
public abstract class PlayerListOverlay implements GameOverlay {

    /**
     * Get the list of player names to display.
     * Includes both remote players and the local player.
     */
    protected Collection<RemotePlayer> getRemotePlayers() {
        return MultiplayerState.getInstance().getRemotePlayers();
    }

    /**
     * Whether the Tab key is currently held (overlay should be visible).
     * Implementations should poll GLFW key state.
     */
    @Override
    public abstract boolean isVisible();
}
