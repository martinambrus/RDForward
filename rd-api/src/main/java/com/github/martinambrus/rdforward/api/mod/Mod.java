package com.github.martinambrus.rdforward.api.mod;

/**
 * Marker interface implemented by every mod entry class. The mod loader
 * instantiates the entrypoint reflectively and downcasts to {@link ServerMod}
 * and/or {@link ClientMod} depending on which side is active.
 */
public interface Mod {
}
