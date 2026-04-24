package com.github.martinambrus.rdforward.api.mod;

/**
 * Optional interface signalling that a mod supports hot-reload. Mods that
 * do not implement {@code Reloadable} can still be reloaded — their state
 * is simply discarded and rebuilt.
 *
 * <p>Reload sequence when the mod opts in:
 * <ol>
 *   <li>{@link #onSaveState()} captures in-memory state.</li>
 *   <li>{@code ServerMod.onDisable()} / {@code ClientMod.onClientStop()} runs.</li>
 *   <li>A fresh classloader loads the updated mod jar.</li>
 *   <li>{@code ServerMod.onEnable(Server)} / {@code ClientMod.onClientReady()} runs.</li>
 *   <li>{@link #onRestoreState(Object)} receives the saved state.</li>
 * </ol>
 *
 * <p>The saved state object crosses classloader boundaries — use only
 * standard library types (primitives, {@code String}, {@code Map}, serialized
 * bytes) or types from {@code rd-api}.
 */
public interface Reloadable {

    /** @return opaque state to carry across the reload, or {@code null} if none. */
    Object onSaveState();

    /** Receives the value returned by {@link #onSaveState()}, or {@code null} on first load. */
    void onRestoreState(Object savedState);
}
