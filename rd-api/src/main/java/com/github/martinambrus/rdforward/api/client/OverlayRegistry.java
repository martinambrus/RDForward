package com.github.martinambrus.rdforward.api.client;

/**
 * Registers {@link GameOverlay} instances for each-frame rendering.
 * Registrations are tagged with the owning mod id; unregistering by mod
 * happens automatically on mod unload.
 */
public interface OverlayRegistry {

    void register(String modId, GameOverlay overlay);

    void unregister(GameOverlay overlay);
}
