package net.fabricmc.api;

/**
 * Stub of Fabric's {@code ClientModInitializer}. Mods bundled as Fabric
 * plugins implement this on their {@code "client"} entrypoint; the
 * RDForward Fabric bridge calls {@link #onInitializeClient()} on the
 * client side after any plain {@link ModInitializer#onInitialize()}
 * main entrypoints finish.
 *
 * <p>Only instantiated when the bridge is booted on a client — on a
 * dedicated server the client entrypoints are skipped entirely.
 */
public interface ClientModInitializer {
    void onInitializeClient();
}
