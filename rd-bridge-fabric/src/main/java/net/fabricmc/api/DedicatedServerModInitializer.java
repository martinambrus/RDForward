package net.fabricmc.api;

/**
 * Stub of Fabric's {@code DedicatedServerModInitializer}. Used as the
 * {@code "server"} entrypoint for mods that only want to run on the
 * dedicated server. The bridge calls {@link #onInitializeServer()} after
 * any plain {@link ModInitializer#onInitialize()} main entrypoints.
 */
public interface DedicatedServerModInitializer {
    void onInitializeServer();
}
