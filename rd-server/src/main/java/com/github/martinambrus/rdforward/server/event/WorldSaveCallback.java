package com.github.martinambrus.rdforward.server.event;

/**
 * Called before the world is saved to disk.
 * Mods can use this to persist their own data alongside the world.
 */
@FunctionalInterface
public interface WorldSaveCallback {
    void onWorldSave();
}
