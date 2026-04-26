package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.mod.ClientMod;
import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.api.mod.ServerMod;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Entry point for mod discovery. Scans a directory for {@code *.jar}
 * files, reads their {@code rdmod.json} descriptors, constructs
 * {@link ModContainer}s, wires classloaders in dependency order, and
 * instantiates each mod's entrypoint class.
 *
 * <p>Actual lifecycle transitions (onEnable/onDisable) are delegated to
 * {@link ModManager} so they can be driven in response to server boot,
 * shutdown, and {@code /reload}.
 */
public final class ModLoader {

    private static final Logger LOG = Logger.getLogger(ModLoader.class.getName());
    /** Candidate descriptor files probed in order. JSON is the canonical form; YAML/TOML are plan §1 alternatives. */
    private static final String[] DESCRIPTOR_ENTRIES = {
            "rdmod.json",
            "rdmod.yaml",
            "rdmod.yml",
            "rdmod.toml"
    };

    private static final Map<String, ModContainer> globalContainersById = new HashMap<>();
    private static ClassLoader apiParent;

    private ModLoader() {}

    /**
     * Discover and instantiate every mod in {@code modsDir}.
     *
     * @param modsDir        directory to scan; created if missing
     * @param apiClassLoader the classloader exposing {@code rd-api} interfaces;
     *                       each mod classloader uses this as its parent
     * @return containers in load order, already bound to classloaders and
     *     entrypoint instances; lifecycle transitions still pending
     */
    public static List<ModContainer> load(Path modsDir, ClassLoader apiClassLoader)
            throws IOException, DescriptorParser.ModDescriptorException, DependencyResolver.ResolutionException {
        return load(List.of(modsDir), apiClassLoader);
    }

    /**
     * Multi-directory overload. Each directory is scanned in order; jars from
     * all directories are then resolved together as a single dependency graph,
     * so a mod in {@code mods/} can depend on a plugin in {@code plugins/} or
     * vice-versa. Per-platform convention is enforced as a warning only —
     * Bukkit/Paper jars belong in {@code plugins/}, Fabric/Forge/NeoForge/native
     * jars belong in {@code mods/} — but misplaced jars still load.
     */
    public static List<ModContainer> load(List<Path> dirs, ClassLoader apiClassLoader)
            throws IOException, DescriptorParser.ModDescriptorException, DependencyResolver.ResolutionException {
        apiParent = apiClassLoader;

        List<ModContainer> discovered = new ArrayList<>();
        Map<String, BridgeRegistry.Loaded> preloadedBridges = new HashMap<>();
        for (Path dir : dirs) {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
                for (Path jar : stream) {
                    ModDescriptor desc = readDescriptor(jar);
                    if (desc != null) {
                        warnIfMisplaced(BridgeKind.NATIVE, dir, jar);
                        discovered.add(new ModContainer(desc, jar));
                        continue;
                    }
                    BridgeKind kind = BridgeRegistry.detect(jar);
                    if (kind == null) {
                        LOG.warning("[ModLoader] " + jar.getFileName()
                                + " has no rdmod descriptor and no bridge manifest - skipping");
                        continue;
                    }
                    warnIfMisplaced(kind, dir, jar);
                    try {
                        BridgeRegistry.Loaded loaded = BridgeRegistry.dispatch(kind, jar, apiParent);
                        ModContainer c = new ModContainer(loaded.descriptor(), jar);
                        c.setBridgeKind(kind);
                        discovered.add(c);
                        preloadedBridges.put(loaded.descriptor().id(), loaded);
                    } catch (Exception e) {
                        LOG.severe("[ModLoader] " + kind + " bridge failed to load "
                                + jar.getFileName() + ": " + e.getMessage());
                    }
                }
            }
        }

        List<ModContainer> ordered = DependencyResolver.resolve(discovered);

        globalContainersById.clear();
        for (ModContainer c : ordered) {
            try {
                BridgeRegistry.Loaded preloaded = preloadedBridges.get(c.id());
                if (preloaded != null) {
                    c.setClassLoader(preloaded.classLoader());
                    c.setServerInstance(preloaded.serverMod());
                } else {
                    bindClassLoader(c);
                    instantiate(c);
                }
                globalContainersById.put(c.id(), c);
            } catch (Exception e) {
                c.fail(e);
                LOG.severe("[ModLoader] failed to load " + c.id() + ": " + e.getMessage());
            }
        }

        return ordered;
    }

    /** Recreate the classloader and entrypoint instances for a single mod. Used by hot-reload. */
    static void rebind(ModContainer c) throws IOException, ReflectiveOperationException {
        java.net.URLClassLoader old = c.classLoader();
        if (old != null) {
            try { old.close(); } catch (IOException ignored) {}
        }
        c.setClassLoader(null);
        c.setServerInstance(null);
        c.setClientInstance(null);
        c.setState(ModState.DISCOVERED);

        if (c.bridgeKind() == BridgeKind.NATIVE) {
            bindClassLoader(c);
            instantiate(c);
        } else {
            BridgeRegistry.Loaded loaded = BridgeRegistry.dispatch(c.bridgeKind(), c.jarPath(), apiParent);
            c.setClassLoader(loaded.classLoader());
            c.setServerInstance(loaded.serverMod());
        }
    }

    /**
     * Log a one-line warning if {@code jar} sits in the wrong directory for
     * its platform. Bukkit/Paper jars belong in {@code plugins/}; native
     * rd-api / Fabric / Forge / NeoForge jars belong in {@code mods/}. The
     * jar still loads — convention is a nudge, not a hard rule.
     */
    private static void warnIfMisplaced(BridgeKind kind, Path dir, Path jar) {
        Path nameP = dir.getFileName();
        if (nameP == null) return;
        String dirName = nameP.toString();
        boolean pluginStyle = (kind == BridgeKind.BUKKIT || kind == BridgeKind.PAPER || kind == BridgeKind.POCKETMINE);
        boolean modStyle = (kind == BridgeKind.NATIVE || kind == BridgeKind.FABRIC
                || kind == BridgeKind.FORGE || kind == BridgeKind.NEOFORGE);
        if (pluginStyle && "mods".equals(dirName)) {
            LOG.warning("[ModLoader] " + jar.getFileName() + " is a " + kind
                    + " plugin but lives in mods/. Convention: plugins/. Loading anyway.");
        } else if (modStyle && "plugins".equals(dirName)) {
            LOG.warning("[ModLoader] " + jar.getFileName() + " is a " + kind
                    + " mod but lives in plugins/. Convention: mods/. Loading anyway.");
        }
    }

    private static ModDescriptor readDescriptor(Path jar) throws IOException, DescriptorParser.ModDescriptorException {
        try (ZipFile zf = new ZipFile(jar.toFile())) {
            for (String entryName : DESCRIPTOR_ENTRIES) {
                ZipEntry entry = zf.getEntry(entryName);
                if (entry == null) continue;
                try (InputStream in = zf.getInputStream(entry)) {
                    if (entryName.endsWith(".json")) return DescriptorParser.parseJson(in);
                    if (entryName.endsWith(".toml")) return DescriptorParser.parseToml(in);
                    return DescriptorParser.parseYaml(in); // .yaml or .yml
                }
            }
            return null;
        }
    }

    private static void bindClassLoader(ModContainer c) throws IOException {
        List<ModClassLoader> depLoaders = new ArrayList<>();
        for (String depId : c.descriptor().dependencies().keySet()) {
            ModContainer dep = globalContainersById.get(depId);
            if (dep != null && dep.classLoader() instanceof ModClassLoader mcl) {
                depLoaders.add(mcl);
            }
        }
        URL[] urls = { c.jarPath().toUri().toURL() };
        ModClassLoader loader = new ModClassLoader(c.id(), urls, apiParent, depLoaders);
        c.setClassLoader(loader);
    }

    private static void instantiate(ModContainer c) throws ReflectiveOperationException {
        ModDescriptor d = c.descriptor();
        ModClassLoader loader = (ModClassLoader) c.classLoader();

        String serverClass = d.serverEntrypoint();
        String clientClass = d.clientEntrypoint();

        Object serverInstance = serverClass == null ? null : newInstance(loader, serverClass);
        Object clientInstance;
        if (clientClass == null) {
            clientInstance = null;
        } else if (clientClass.equals(serverClass)) {
            clientInstance = serverInstance;
        } else {
            clientInstance = newInstance(loader, clientClass);
        }

        if (serverInstance != null && !(serverInstance instanceof ServerMod)) {
            throw new ReflectiveOperationException(
                    d.id() + ": server entrypoint " + serverClass + " does not implement ServerMod");
        }
        if (clientInstance != null && !(clientInstance instanceof ClientMod)) {
            throw new ReflectiveOperationException(
                    d.id() + ": client entrypoint " + clientClass + " does not implement ClientMod");
        }

        c.setServerInstance(serverInstance);
        c.setClientInstance(clientInstance);
    }

    private static Object newInstance(ModClassLoader loader, String className) throws ReflectiveOperationException {
        Class<?> cls = Class.forName(className, true, loader);
        return cls.getDeclaredConstructor().newInstance();
    }
}
