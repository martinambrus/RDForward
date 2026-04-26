package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reflective dispatcher that lets {@link ModLoader} treat foreign plugin
 * formats (Bukkit, Paper, Fabric, Forge, NeoForge, PocketMine) as first-class
 * mod sources without having a compile-time dependency on any
 * {@code rd-bridge-*} module.
 *
 * <p>{@link #detect(Path)} sniffs a jar's manifest entries in priority order
 * and returns the matching {@link BridgeKind}, or {@code null} when the jar
 * carries no recognised manifest. {@link #dispatch} then invokes the
 * appropriate {@code *PluginLoader.load(Path, ClassLoader)} via reflection
 * and adapts the resulting record to a uniform {@link Loaded} view.
 *
 * <p>If a bridge module is absent from the runtime classpath (e.g. a stripped
 * build), {@code dispatch} throws {@link ReflectiveOperationException} with
 * a clear message — the loader logs it as a per-jar load failure rather than
 * killing the whole boot.
 */
public final class BridgeRegistry {

    private static final Logger LOG = Logger.getLogger(BridgeRegistry.class.getName());

    private static final Map<BridgeKind, String> LOADER_FQCN;
    static {
        LOADER_FQCN = new LinkedHashMap<>();
        LOADER_FQCN.put(BridgeKind.PAPER, "com.github.martinambrus.rdforward.bridge.paper.PaperPluginLoader");
        LOADER_FQCN.put(BridgeKind.BUKKIT, "com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginLoader");
        LOADER_FQCN.put(BridgeKind.FABRIC, "com.github.martinambrus.rdforward.bridge.fabric.FabricPluginLoader");
        LOADER_FQCN.put(BridgeKind.FORGE, "com.github.martinambrus.rdforward.bridge.forge.ForgeModLoader");
        LOADER_FQCN.put(BridgeKind.NEOFORGE, "com.github.martinambrus.rdforward.bridge.neoforge.NeoForgeModLoader");
        LOADER_FQCN.put(BridgeKind.POCKETMINE, "com.github.martinambrus.rdforward.bridge.pocketmine.PocketMinePluginLoader");
    }

    private BridgeRegistry() {}

    /** Result of a successful bridge dispatch: descriptor, owning classloader, ServerMod wrapper. */
    public record Loaded(ModDescriptor descriptor, URLClassLoader classLoader, Object serverMod) {}

    /**
     * Probe {@code jar} for known manifest entries in priority order. NeoForge
     * is checked before Forge because NeoForge jars often ship both
     * {@code mods.toml} and {@code neoforge.mods.toml}; Paper before Bukkit
     * for the same reason. Returns {@code null} if no manifest matches.
     */
    public static BridgeKind detect(Path jar) throws IOException {
        try (ZipFile zf = new ZipFile(jar.toFile())) {
            if (zf.getEntry("paper-plugin.yml") != null) return BridgeKind.PAPER;
            if (zf.getEntry("fabric.mod.json") != null) return BridgeKind.FABRIC;
            if (zf.getEntry("META-INF/neoforge.mods.toml") != null) return BridgeKind.NEOFORGE;
            if (zf.getEntry("META-INF/mods.toml") != null) return BridgeKind.FORGE;
            ZipEntry pluginYml = zf.getEntry("plugin.yml");
            if (pluginYml != null) {
                return hasPocketMineApiField(zf, pluginYml) ? BridgeKind.POCKETMINE : BridgeKind.BUKKIT;
            }
        }
        return null;
    }

    /**
     * Quick line-scan for an {@code api:} key (PocketMine-MP) vs Bukkit's
     * {@code api-version:}. Match is case-insensitive on the key, ignores
     * leading whitespace and YAML comment lines, and stops at the first
     * top-level key encountered.
     */
    private static boolean hasPocketMineApiField(ZipFile zf, ZipEntry pluginYml) throws IOException {
        try (InputStream in = zf.getInputStream(pluginYml);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                int colon = trimmed.indexOf(':');
                if (colon < 0) continue;
                String key = trimmed.substring(0, colon).trim().toLowerCase();
                if (key.equals("api")) return true;
                if (key.equals("api-version")) return false;
            }
        }
        return false;
    }

    /**
     * Reflectively call the bridge's {@code load(Path, ClassLoader)} static
     * method and adapt its return value to {@link Loaded}.
     *
     * @throws ReflectiveOperationException if the bridge module is absent or
     *     its loader signature has drifted
     * @throws IOException                  propagated from the bridge loader
     */
    public static Loaded dispatch(BridgeKind kind, Path jar, ClassLoader parent)
            throws IOException, ReflectiveOperationException {
        if (kind == null || kind == BridgeKind.NATIVE) {
            throw new IllegalArgumentException("dispatch requires a non-NATIVE BridgeKind");
        }
        String fqcn = LOADER_FQCN.get(kind);
        Class<?> loaderCls;
        try {
            loaderCls = Class.forName(fqcn, true, BridgeRegistry.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ReflectiveOperationException(
                    "Bridge module for " + kind + " is not on the classpath (missing " + fqcn + ")", e);
        }
        Method load = loaderCls.getMethod("load", Path.class, ClassLoader.class);
        Object result;
        try {
            result = load.invoke(null, jar, parent);
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof IOException io) throw io;
            if (cause instanceof ReflectiveOperationException roe) throw roe;
            if (cause instanceof RuntimeException re) throw re;
            throw new ReflectiveOperationException("Bridge loader " + fqcn + " threw: " + cause, cause);
        }
        return adapt(result, kind);
    }

    /**
     * Pull {@code descriptor() / classLoader() / serverMod()} off the bridge
     * loader's result record. Paper's {@code Result} is a sealed union — if
     * the runtime instance is a {@code BukkitFallback} we unwrap its
     * {@code inner()} field which holds the underlying {@code LoadedPlugin}.
     */
    private static Loaded adapt(Object result, BridgeKind kind) throws ReflectiveOperationException {
        if (result == null) {
            throw new ReflectiveOperationException("Bridge loader for " + kind + " returned null");
        }
        Object payload = result;
        // Paper fallback unwrap: BukkitFallback has an inner() accessor that
        // returns the underlying BukkitPluginLoader.LoadedPlugin.
        if (kind == BridgeKind.PAPER && result.getClass().getSimpleName().equals("BukkitFallback")) {
            Method inner = result.getClass().getMethod("inner");
            payload = inner.invoke(result);
            if (payload == null) {
                throw new ReflectiveOperationException("Paper BukkitFallback.inner() returned null");
            }
        }
        Object descriptor = payload.getClass().getMethod("descriptor").invoke(payload);
        Object classLoader = payload.getClass().getMethod("classLoader").invoke(payload);
        Object serverMod = payload.getClass().getMethod("serverMod").invoke(payload);
        if (!(descriptor instanceof ModDescriptor md)) {
            throw new ReflectiveOperationException(
                    "Bridge result descriptor() returned non-ModDescriptor: " + descriptor);
        }
        if (!(classLoader instanceof URLClassLoader ucl)) {
            throw new ReflectiveOperationException(
                    "Bridge result classLoader() returned non-URLClassLoader: " + classLoader);
        }
        if (serverMod == null) {
            LOG.warning("[BridgeRegistry] " + kind + " bridge returned null serverMod for " + md.id());
        }
        return new Loaded(md, ucl, serverMod);
    }
}
