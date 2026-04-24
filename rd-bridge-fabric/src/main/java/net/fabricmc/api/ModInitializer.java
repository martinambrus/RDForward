// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.api;

/**
 * Stub of Fabric's {@code ModInitializer} interface. Mods bundled as
 * Fabric plugins implement this on their {@code "main"} entrypoint; the
 * RDForward Fabric bridge calls {@link #onInitialize()} in response to
 * the mod being enabled by the mod loader.
 */
public interface ModInitializer {
    void onInitialize();
}
