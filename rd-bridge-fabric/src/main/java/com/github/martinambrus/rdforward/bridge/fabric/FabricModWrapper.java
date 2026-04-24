package com.github.martinambrus.rdforward.bridge.fabric;

import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.server.Server;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

import java.util.List;

/**
 * Adapts a loaded Fabric mod to RDForward's {@link ServerMod} lifecycle.
 * On {@link #onEnable(Server)} it calls every {@code main} entrypoint's
 * {@link ModInitializer#onInitialize()} followed by every {@code server}
 * entrypoint's {@link DedicatedServerModInitializer#onInitializeServer()}.
 *
 * <p>Fabric does not define a {@code onDisable} counterpart for mod
 * initializers — listeners, commands and scheduler tasks registered
 * during init are automatically swept by {@code ModManager} via the
 * EventOwnership scope.
 */
public final class FabricModWrapper implements ServerMod {

    private final List<ModInitializer> mainInitializers;
    private final List<DedicatedServerModInitializer> serverInitializers;

    public FabricModWrapper(List<ModInitializer> mainInitializers,
                             List<DedicatedServerModInitializer> serverInitializers) {
        this.mainInitializers = List.copyOf(mainInitializers);
        this.serverInitializers = List.copyOf(serverInitializers);
    }

    @Override
    public void onEnable(Server server) {
        for (ModInitializer init : mainInitializers) init.onInitialize();
        for (DedicatedServerModInitializer init : serverInitializers) init.onInitializeServer();
    }

    @Override
    public void onDisable() {
        // Fabric mods have no standard disable hook; rely on EventOwnership sweep.
    }
}
