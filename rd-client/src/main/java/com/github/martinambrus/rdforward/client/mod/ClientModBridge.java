package com.github.martinambrus.rdforward.client.mod;

import com.github.martinambrus.rdforward.api.client.ClientEvents;
import com.github.martinambrus.rdforward.api.mod.ClientMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;
import java.util.logging.Logger;

/**
 * Fabric client-side initializer that bridges the RDForward mod API to the
 * running RubyDung client. Each external client mod declares the
 * {@code rdforward-client} entrypoint in its {@code fabric.mod.json} pointing
 * at a class that implements {@link ClientMod}; on startup this bridge
 * discovers those entrypoints through Fabric Loader and drives their
 * {@code onClientReady()} / {@code onClientStop()} callbacks.
 *
 * <p>External client mods are therefore packaged as Fabric mods (per the
 * Q2 architectural decision), but the API they implement is RDForward's
 * own {@link ClientMod} — there is no direct dependency on Fabric API
 * classes in the mod's compiled code beyond the entrypoint wiring.
 */
public final class ClientModBridge implements ClientModInitializer {

    private static final Logger LOG = Logger.getLogger(ClientModBridge.class.getName());
    private static final String ENTRYPOINT = "rdforward-client";

    private static volatile List<ClientMod> loaded = List.of();

    @Override
    public void onInitializeClient() {
        loaded = FabricLoader.getInstance().getEntrypoints(ENTRYPOINT, ClientMod.class);
        LOG.info("[ClientModBridge] discovered " + loaded.size() + " rdforward-client mod(s)");
        for (ClientMod m : loaded) {
            try {
                m.onClientReady();
            } catch (Throwable t) {
                LOG.warning("[ClientModBridge] " + m.getClass().getName()
                        + ".onClientReady() threw: " + t);
            }
        }
        ClientEvents.CLIENT_READY.invoker().onReady();
        Runtime.getRuntime().addShutdownHook(new Thread(ClientModBridge::shutdown,
                "rdforward-client-shutdown"));
    }

    private static void shutdown() {
        ClientEvents.CLIENT_STOPPING.invoker().onStopping();
        for (ClientMod m : loaded) {
            try {
                m.onClientStop();
            } catch (Throwable t) {
                LOG.warning("[ClientModBridge] " + m.getClass().getName()
                        + ".onClientStop() threw: " + t);
            }
        }
    }

    /** @return every rdforward-client mod instance Fabric Loader surfaced at boot. */
    public static List<ClientMod> loadedMods() {
        return loaded;
    }
}
