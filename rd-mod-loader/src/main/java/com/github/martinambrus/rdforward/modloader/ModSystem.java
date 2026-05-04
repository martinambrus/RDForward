package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.modloader.admin.AdminCommands;
import com.github.martinambrus.rdforward.modloader.admin.CommandConflictResolver;
import com.github.martinambrus.rdforward.modloader.admin.EventManager;
import com.github.martinambrus.rdforward.modloader.impl.RDServer;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /** Per-{@link BridgeKind} installer FQCN invoked reflectively before mod
     *  enable so plugin bytecode that calls {@code Bukkit.getServer()} or
     *  {@code Bukkit.getPluginManager()} during {@code onEnable} sees a live
     *  facade. Each entry must declare a public static {@code uninstall()};
     *  {@code install} accepts either {@code (Server)} (Bukkit, Paper, Forge,
     *  NeoForge, PocketMine) or no args (Fabric server bridge wires itself
     *  against the static {@code ServerEvents} dispatcher and needs no rd-api
     *  Server reference). Probe order is {@code (Server)} first, no-arg
     *  fallback. Missing classes are tolerated so a stripped build without a
     *  given bridge module still boots. */
    private static final Map<BridgeKind, String> BRIDGE_INSTALLER_FQCN;

    static {
        Map<BridgeKind, String> map = new EnumMap<>(BridgeKind.class);
        map.put(BridgeKind.BUKKIT,     "com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge");
        map.put(BridgeKind.PAPER,      "com.github.martinambrus.rdforward.bridge.paper.PaperBridge");
        map.put(BridgeKind.FABRIC,     "com.github.martinambrus.rdforward.bridge.fabric.server.FabricServerBridge");
        map.put(BridgeKind.FORGE,      "com.github.martinambrus.rdforward.bridge.forge.ForgeBridge");
        map.put(BridgeKind.NEOFORGE,   "com.github.martinambrus.rdforward.bridge.neoforge.NeoForgeBridge");
        map.put(BridgeKind.POCKETMINE, "com.github.martinambrus.rdforward.bridge.pocketmine.PocketMineBridge");
        BRIDGE_INSTALLER_FQCN = Map.copyOf(map);
    }

    /** Tracks which kinds were actually installed so {@link #uninstallBridges()}
     *  uninstalls the same set in reverse order. Reset on every install pass. */
    private static final Deque<BridgeKind> installedKinds = new ArrayDeque<>();

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
        List<ModContainer> containers = ModLoader.load(
                dirs, ModSystem.class.getClassLoader(), bridgeProvidedIds());
        ModManager manager = new ModManager(apiServer);
        manager.setContainers(containers);
        apiServer.setModManager(manager);
        AdminCommands.bindManager(manager);
        installBridges(apiServer, activeKinds(containers));
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

    /** Legacy single-arg overload retained for the bridge install unit
     *  tests, which exercise the dispatch end-to-end without booting a
     *  full mod loader. Installs every bridge whose installer class is on
     *  the classpath — preserves the pre-lazy behaviour for callers that
     *  do not own a populated container list. Production boot uses
     *  {@link #installBridges(com.github.martinambrus.rdforward.api.server.Server, Set)}
     *  with the actual detected set. */
    static void installBridges(com.github.martinambrus.rdforward.api.server.Server apiServer) {
        installBridges(apiServer, EnumSet.complementOf(EnumSet.of(BridgeKind.NATIVE)));
    }

    /** Reflectively call {@code install} on each bridge whose
     *  {@link BridgeKind} appears in {@code activeKinds}. Skips kinds with
     *  no installer registered (e.g. {@link BridgeKind#NATIVE}) and silently
     *  ignores missing classes so a stripped build without a given bridge
     *  module still boots.
     *
     *  <p>Lazy-load semantics: a deployment that loads only Forge mods will
     *  not initialise {@code BukkitBridge} or its Material/Sound/ItemType
     *  enum stubs, and vice versa. Each bridge's static init runs only when
     *  a matching plugin or mod jar was discovered.
     *
     *  <p>Install order is fixed by enum declaration; {@link #installedKinds}
     *  records the actual order so {@link #uninstallBridges} can unwind in
     *  reverse. */
    static void installBridges(com.github.martinambrus.rdforward.api.server.Server apiServer,
                               Set<BridgeKind> activeKinds) {
        installedKinds.clear();
        for (BridgeKind kind : BridgeKind.values()) {
            if (kind == BridgeKind.NATIVE || !activeKinds.contains(kind)) continue;
            String fqcn = BRIDGE_INSTALLER_FQCN.get(kind);
            if (fqcn == null) continue;
            if (invokeInstall(fqcn, apiServer)) {
                installedKinds.push(kind);
            }
        }
    }

    /** Symmetric counterpart to {@link #installBridges}. Walks
     *  {@link #installedKinds} in reverse so dependent bridges (e.g. Paper
     *  → Bukkit) tear down in the opposite order they were brought up. */
    static void uninstallBridges() {
        while (!installedKinds.isEmpty()) {
            BridgeKind kind = installedKinds.pop();
            String fqcn = BRIDGE_INSTALLER_FQCN.get(kind);
            if (fqcn == null) continue;
            invokeUninstall(fqcn);
        }
    }

    /** Mod ids advertised as "provided by the host platform" so plugins
     *  declaring them as a hard dep resolve without a real jar. Probes
     *  bridge installer FQCNs reflectively — same one-way relationship
     *  used in {@link #BRIDGE_INSTALLER_FQCN} — so a stripped build
     *  without the corresponding bridge module contributes nothing. */
    private static Set<String> bridgeProvidedIds() {
        java.util.Set<String> ids = new java.util.HashSet<>();
        if (isClassPresent(BRIDGE_INSTALLER_FQCN.get(BridgeKind.BUKKIT))) {
            ids.add("Vault");
        }
        return ids;
    }

    private static boolean isClassPresent(String fqcn) {
        if (fqcn == null) return false;
        try {
            Class.forName(fqcn, false, ModSystem.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Set<BridgeKind> activeKinds(List<ModContainer> containers) {
        EnumSet<BridgeKind> kinds = EnumSet.noneOf(BridgeKind.class);
        for (ModContainer c : containers) {
            BridgeKind k = c.bridgeKind();
            if (k != null && k != BridgeKind.NATIVE) kinds.add(k);
        }
        return kinds;
    }

    /** @return {@code true} if the install method was located and invoked
     *  successfully (so we should record the kind for symmetric uninstall),
     *  {@code false} if the class was absent or the call failed. */
    private static boolean invokeInstall(String fqcn,
                                         com.github.martinambrus.rdforward.api.server.Server apiServer) {
        try {
            Class<?> cls = Class.forName(fqcn, true, ModSystem.class.getClassLoader());
            // Probe (Server) overload first (bukkit/paper/forge/neoforge/pocketmine);
            // fall back to no-arg (fabric server bridge wires itself against
            // the static ServerEvents dispatcher and takes no rd-api Server).
            Method install;
            try {
                install = cls.getMethod("install", com.github.martinambrus.rdforward.api.server.Server.class);
                install.invoke(null, apiServer);
            } catch (NoSuchMethodException nsme) {
                install = cls.getMethod("install");
                install.invoke(null);
            }
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        } catch (ReflectiveOperationException e) {
            LOG.warning("[ModSystem] failed to install " + fqcn + ": " + e);
            return false;
        }
    }

    private static void invokeUninstall(String fqcn) {
        try {
            Class<?> cls = Class.forName(fqcn, true, ModSystem.class.getClassLoader());
            cls.getMethod("uninstall").invoke(null);
        } catch (ClassNotFoundException ignored) {
            // bridge module absent — nothing to undo
        } catch (ReflectiveOperationException e) {
            LOG.warning("[ModSystem] failed to uninstall " + fqcn + ": " + e);
        }
    }
}
