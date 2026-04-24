package com.github.martinambrus.rdforward.bridge.fabric.client;

import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.server.Server;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import java.util.List;

/**
 * Adapts a Fabric mod loaded for client deployment to RDForward's
 * {@link ServerMod} lifecycle. On {@link #onEnable(Server)} it calls every
 * {@code main} entrypoint's {@link ModInitializer#onInitialize()} followed
 * by every {@code client} entrypoint's
 * {@link ClientModInitializer#onInitializeClient()}.
 *
 * <p>Implements {@code ServerMod} rather than a hypothetical {@code ClientMod}
 * because rd-mod-loader only knows the {@code ServerMod} lifecycle shape.
 * The {@code Server} argument is ignored on a client host — passing
 * {@code null} is fine.
 *
 * <p>Before any initializer runs, the host must have called
 * {@link FabricClientBridge#install()} so Fabric callback classes forward
 * correctly from rd-client's dispatch.
 */
public final class FabricClientModWrapper implements ServerMod {

    private final List<ModInitializer> mainInitializers;
    private final List<ClientModInitializer> clientInitializers;

    public FabricClientModWrapper(List<ModInitializer> mainInitializers,
                                  List<ClientModInitializer> clientInitializers) {
        this.mainInitializers = List.copyOf(mainInitializers);
        this.clientInitializers = List.copyOf(clientInitializers);
    }

    @Override
    public void onEnable(Server server) {
        for (ModInitializer init : mainInitializers) init.onInitialize();
        for (ClientModInitializer init : clientInitializers) init.onInitializeClient();
    }

    @Override
    public void onDisable() {
        // Fabric does not define a per-mod disable hook — the EventOwnership
        // sweep in rd-mod-loader handles teardown of anything registered
        // during onInitialize / onInitializeClient.
    }
}
