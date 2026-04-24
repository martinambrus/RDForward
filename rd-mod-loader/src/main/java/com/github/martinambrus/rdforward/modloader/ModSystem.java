package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.modloader.admin.AdminCommands;
import com.github.martinambrus.rdforward.modloader.admin.CommandConflictResolver;
import com.github.martinambrus.rdforward.modloader.admin.EventManager;
import com.github.martinambrus.rdforward.modloader.impl.RDServer;

import java.nio.file.Path;
import java.util.List;

/**
 * Bootstrap entry point invoked reflectively by rd-server. Wraps the
 * running rd-server instance in the api-facing {@link RDServer} adapter,
 * discovers and resolves mods from {@code modsDir}, then enables them.
 *
 * <p>The instance returned acts as the handle that rd-server holds to
 * trigger {@link #stop()} during shutdown.
 */
public final class ModSystem {

    private final RDServer apiServer;
    private final ModManager manager;

    private ModSystem(RDServer apiServer, ModManager manager) {
        this.apiServer = apiServer;
        this.manager = manager;
    }

    /**
     * Discover mods in {@code modsDir}, resolve their dependency graph,
     * and enable them against the given rd-server instance. Called by
     * {@code RDServer.bootModSystem()} via reflection.
     *
     * @param rawServer the {@code com.github.martinambrus.rdforward.server.RDServer}
     *     instance, passed as {@link Object} because the rd-server module
     *     has no compile-time dependency on this one
     * @param modsDir directory holding mod {@code *.jar} files; created if absent
     */
    public static ModSystem boot(Object rawServer, Path modsDir) throws Exception {
        com.github.martinambrus.rdforward.server.RDServer rdServer =
                (com.github.martinambrus.rdforward.server.RDServer) rawServer;
        RDServer apiServer = new RDServer(rdServer);
        Path configDir = modsDir.resolveSibling("mod-config");
        EventManager.install(List.of(ServerEvents.class), configDir.resolve("event-overrides.json"));
        AdminCommands.register();
        CommandConflictResolver.install(configDir.resolve("command-overrides.json"));
        List<ModContainer> containers = ModLoader.load(modsDir, ModSystem.class.getClassLoader());
        ModManager manager = new ModManager(apiServer);
        manager.setContainers(containers);
        apiServer.setModManager(manager);
        AdminCommands.bindManager(manager);
        manager.enableAll();
        java.util.function.Predicate<String> isModPresent = id -> manager.get(id) != null;
        EventManager.applyOverrides(isModPresent);
        CommandConflictResolver.reconcile(isModPresent);
        return new ModSystem(apiServer, manager);
    }

    /** @return the api-facing server adapter wrapping the rd-server delegate. */
    public RDServer getApiServer() { return apiServer; }

    /** @return the mod manager tracking every loaded mod. */
    public ModManager getManager() { return manager; }

    /** Disable every enabled mod in reverse load order. */
    public void stop() {
        manager.disableAll();
    }
}
