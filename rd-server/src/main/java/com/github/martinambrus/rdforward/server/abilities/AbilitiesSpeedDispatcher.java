package com.github.martinambrus.rdforward.server.abilities;

import com.github.martinambrus.rdforward.server.ConnectedPlayer;

/**
 * Strategy for pushing fly-speed / walk-speed updates to a connected
 * client. One implementation is selected per session at first use and
 * cached on the player; each session only ever runs its own codec's
 * code path.
 *
 * <p>Pre-1.6.1 / Beta / Classic / RubyDung sessions resolve to
 * {@link NoOpAbilitiesDispatcher} since those wire formats have no
 * runtime fly-speed field.
 */
public interface AbilitiesSpeedDispatcher {

    /**
     * Push the current {@code flySpeed} and {@code walkSpeed} stored on
     * {@code player} to the client. Implementations read directly from
     * {@link ConnectedPlayer#getFlySpeed()} / {@link ConnectedPlayer#getWalkSpeed()}.
     */
    void push(ConnectedPlayer player);
}
