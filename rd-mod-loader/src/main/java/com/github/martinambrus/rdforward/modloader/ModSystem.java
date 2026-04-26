package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.modloader.admin.AdminCommands;
import com.github.martinambrus.rdforward.modloader.admin.CommandConflictResolver;
import com.github.martinambrus.rdforward.modloader.admin.EventManager;
import com.github.martinambrus.rdforward.modloader.impl.RDServer;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * Bootstrap entry point invoked reflectively by rd-server. Wraps the
 * running rd-server instance in the api-facing {@link RDServer} adapter,
 * discovers and resolves mods from {@code modsDir}, then enables them.
 *
 * <p>The instance returned acts as the handle that rd-server holds to
 * trigger {@link #stop()} during shutdown.
 */
public final class ModSystem {

    private static final Logger LOG = Logger.getLogger(ModSystem.class.getName());

    /** Bridge installer FQCNs invoked reflectively before mod enable so plugin
     *  bytecode that calls {@code Bukkit.getServer()} or
     *  {@code Bukkit.getPluginManager()} during {@code onEnable} sees a live
     *  facade. Each entry must declare a public static
     *  {@code install(com.github.martinambrus.rdforward.api.server.Server)} and
     *  matching {@code uninstall()}. Missing classes are tolerated so a
     *  stripped build without bridge modules still boots. */
    private static final String[] BRIDGE_INSTALLERS = {
            "com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge",
    };

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
        return boot(rawServer, modsDir, null);
    }

    /**
     * Two-directory overload: native rd-api / Fabric / Forge / NeoForge mods
     * are scanned from {@code modsDir}, Bukkit / Paper / PocketMine plugins
     * from {@code pluginsDir}. Pass {@code null} for {@code pluginsDir} to
     * scan {@code modsDir} only (legacy behaviour).
     */
    public static ModSystem boot(Object rawServer, Path modsDir, Path pluginsDir) throws Exception {
        com.github.martinambrus.rdforward.server.RDServer rdServer =
                (com.github.martinambrus.rdforward.server.RDServer) rawServer;
        RDServer apiServer = new RDServer(rdServer);
        Path configDir = modsDir.resolveSibling("mod-config");
        EventManager.install(List.of(ServerEvents.class), configDir.resolve("event-overrides.json"));
        AdminCommands.register();
        CommandConflictResolver.install(configDir.resolve("command-overrides.json"));
        List<Path> dirs = pluginsDir == null ? List.of(modsDir) : List.of(modsDir, pluginsDir);
        List<ModContainer> containers = ModLoader.load(dirs, ModSystem.class.getClassLoader());
        ModManager manager = new ModManager(apiServer);
        manager.setContainers(containers);
        apiServer.setModManager(manager);
        AdminCommands.bindManager(manager);
        installBridges(apiServer);
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
        uninstallBridges();
    }

    /** Reflectively call each bridge's {@code install(Server)} so plugin code
     *  that invokes {@code Bukkit.getServer()} during {@code onEnable} resolves
     *  to a live facade. Missing bridge classes are silently skipped — a
     *  stripped build without {@code rd-bridge-bukkit} still boots.
     *
     *  <p>The parameter type is the api-level
     *  {@link com.github.martinambrus.rdforward.api.server.Server} so unit
     *  tests can drive the install hook with a stub server without booting
     *  a full {@code rd-server}. */
    static void installBridges(com.github.martinambrus.rdforward.api.server.Server apiServer) {
        for (String fqcn : BRIDGE_INSTALLERS) {
            try {
                Class<?> cls = Class.forName(fqcn, true, ModSystem.class.getClassLoader());
                cls.getMethod("install", com.github.martinambrus.rdforward.api.server.Server.class)
                        .invoke(null, apiServer);
            } catch (ClassNotFoundException ignored) {
                // bridge module absent — skip
            } catch (ReflectiveOperationException e) {
                LOG.warning("[ModSystem] failed to install " + fqcn + ": " + e);
            }
        }
    }

    /** Symmetric counterpart to {@link #installBridges}. Calls
     *  {@code uninstall()} so subsequent server boots in the same JVM (e.g.
     *  test suites) start with a clean facade. */
    static void uninstallBridges() {
        for (String fqcn : BRIDGE_INSTALLERS) {
            try {
                Class<?> cls = Class.forName(fqcn, true, ModSystem.class.getClassLoader());
                cls.getMethod("uninstall").invoke(null);
            } catch (ClassNotFoundException ignored) {
                // skip
            } catch (ReflectiveOperationException e) {
                LOG.warning("[ModSystem] failed to uninstall " + fqcn + ": " + e);
            }
        }
    }
}
