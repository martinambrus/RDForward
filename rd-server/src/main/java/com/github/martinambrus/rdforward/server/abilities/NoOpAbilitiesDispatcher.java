package com.github.martinambrus.rdforward.server.abilities;

import com.github.martinambrus.rdforward.server.ConnectedPlayer;

/** Selected for sessions whose protocol has no runtime fly-speed field
 *  (Classic, RubyDung, Beta, MCPE legacy, Alpha pre-1.3.1). */
public final class NoOpAbilitiesDispatcher implements AbilitiesSpeedDispatcher {

    public static final NoOpAbilitiesDispatcher INSTANCE = new NoOpAbilitiesDispatcher();

    private NoOpAbilitiesDispatcher() {}

    @Override
    public void push(ConnectedPlayer player) {
        // intentional no-op
    }
}
