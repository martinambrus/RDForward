package com.github.martinambrus.rdforward.api.mod;

/**
 * Client-side entrypoint. {@link #onClientReady()} fires once the game
 * window and render loop are up; {@link #onClientStop()} fires on game
 * exit or hot-reload.
 *
 * <p>Registrations made against {@code ClientEvents}, {@code OverlayRegistry},
 * {@code KeyBindingRegistry} etc. during {@code onClientReady} are tagged
 * with the owning mod id and removed on {@code onClientStop}.
 */
public interface ClientMod extends Mod {

    void onClientReady();

    void onClientStop();
}
