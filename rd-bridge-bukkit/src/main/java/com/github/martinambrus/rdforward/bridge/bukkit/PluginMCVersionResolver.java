// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves the Minecraft version string returned by
 * {@link org.bukkit.Server#getVersion()} and
 * {@link org.bukkit.Server#getBukkitVersion()} on a per-plugin basis,
 * so that legacy plugins which hardcode-check the MC version
 * (e.g. Citizens 2.0.13 only accepts {@code 1.7.9}) and modern
 * plugins (Essentials 2.21 targets 1.21.x) can coexist on the
 * same server.
 *
 * <p>Resolution priority for a plugin:
 * <ol>
 *   <li>{@code plugin-mc-version-overrides.yml} entry by plugin name</li>
 *   <li>plugin.yml {@code api-version} mapped through {@link #API_VERSION_TO_MC}</li>
 *   <li>{@link #DEFAULT} — the bridge's modern target ({@code 1.21.4})</li>
 * </ol>
 *
 * <p>Caller detection walks the current thread's stack via
 * {@link StackWalker} and returns the tuned tuple registered for the
 * first frame whose declaring class chain ends at a known plugin
 * classloader. Server-internal frames (no matching loader) yield
 * {@link #DEFAULT}. Cross-plugin calls: the leaf-most plugin frame wins.
 *
 * <p>NOT in scope: NMS package-name spoofing
 * ({@code org.bukkit.craftbukkit.v1_8_R3} etc.) — that requires
 * per-plugin proxy classes and is a separate effort.
 */
public final class PluginMCVersionResolver {

    private static final Logger LOG = Logger.getLogger("RDForward/PluginMCVersionResolver");

    /** Default tuple — matches the bridge's pre-resolver hardcoded values
     *  verbatim so plugins that do not opt into a different MC version
     *  observe identical behaviour to the previous build. */
    public static final MCVersionTuple DEFAULT = MCVersionTuple.of("1.21.4");

    /** api-version (from plugin.yml) → representative full MC version.
     *  Both bare ({@code 1.21}) and dotted ({@code 1.21.0}) keys are
     *  accepted: {@link #lookupApiVersion} normalises both before
     *  lookup. Values pick the latest stable patch within each minor
     *  line so that newer-but-compatible features still resolve. */
    private static final Map<String, MCVersionTuple> API_VERSION_TO_MC = Map.ofEntries(
            Map.entry("1.13", MCVersionTuple.of("1.13.2")),
            Map.entry("1.14", MCVersionTuple.of("1.14.4")),
            Map.entry("1.15", MCVersionTuple.of("1.15.2")),
            Map.entry("1.16", MCVersionTuple.of("1.16.5")),
            Map.entry("1.17", MCVersionTuple.of("1.17.1")),
            Map.entry("1.18", MCVersionTuple.of("1.18.2")),
            Map.entry("1.19", MCVersionTuple.of("1.19.4")),
            Map.entry("1.20", MCVersionTuple.of("1.20.6")),
            Map.entry("1.21", MCVersionTuple.of("1.21.4"))
    );

    /** Plugin name (plugin.yml {@code name}) → MC version override.
     *  Loaded once at bridge install from the YAML override file
     *  next to {@code server.properties}. Empty when no file exists. */
    private static final Map<String, String> NAME_OVERRIDES = new ConcurrentHashMap<>();

    /** Plugin classloader → resolved tuple. Computed at registration
     *  time so {@link #currentVersion()} only walks the stack and
     *  hashes; no override / api-version reparse on the hot path. */
    private static final Map<ClassLoader, MCVersionTuple> CACHE = new ConcurrentHashMap<>();

    private static final StackWalker WALKER =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private PluginMCVersionResolver() {}

    /** Load {@code plugin-mc-version-overrides.yml} from {@code path}.
     *  Top-level YAML map of plugin-name → mc-version (string). Missing
     *  file is a silent no-op. Parse error is logged at WARNING but
     *  does not abort install — bridge install must always succeed. */
    @SuppressWarnings("unchecked")
    public static void loadOverrides(Path path) {
        if (path == null || !Files.exists(path)) return;
        try (InputStream in = Files.newInputStream(path)) {
            Object root = new Yaml().load(in);
            if (root == null) return;
            if (!(root instanceof Map<?, ?> map)) {
                LOG.warning("plugin-mc-version-overrides.yml: top-level must be a map of plugin-name -> mc-version");
                return;
            }
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                String name = String.valueOf(e.getKey()).trim();
                String mc = String.valueOf(e.getValue()).trim();
                if (name.isEmpty() || mc.isEmpty()) continue;
                NAME_OVERRIDES.put(name, mc);
            }
            LOG.info("Loaded " + NAME_OVERRIDES.size() + " plugin MC-version override(s) from " + path);
        } catch (IOException | RuntimeException ex) {
            LOG.log(Level.WARNING, "Failed to read " + path + " — proceeding with no overrides", ex);
        }
    }

    /** Compute and cache the tuned tuple for a freshly-loaded plugin.
     *  Resolution order: name override > api-version table > default. */
    public static void register(ClassLoader loader, String pluginName, String apiVersion) {
        if (loader == null) return;
        CACHE.put(loader, resolveTuple(pluginName, apiVersion));
    }

    /** Drop the mapping for a plugin classloader. Idempotent. */
    public static void unregister(ClassLoader loader) {
        if (loader == null) return;
        CACHE.remove(loader);
    }

    /** @return the tuple to use for the current call site. Walks the
     *  stack; first frame whose declaring class chain ends at a
     *  registered plugin loader wins. Falls back to {@link #DEFAULT}
     *  when no plugin frame is on the stack (server-internal call,
     *  pre-load probe, etc.). Frames inside this class are skipped
     *  so the walker doesn't pick up its own implementation. */
    public static MCVersionTuple currentVersion() {
        if (CACHE.isEmpty()) return DEFAULT;
        MCVersionTuple hit = WALKER.walk(stream -> stream
                .filter(f -> f.getDeclaringClass() != PluginMCVersionResolver.class)
                .map(StackWalker.StackFrame::getDeclaringClass)
                .map(PluginMCVersionResolver::lookupClass)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null));
        return hit != null ? hit : DEFAULT;
    }

    /** Convenience for {@link org.bukkit.Server#getVersion()}. */
    public static String currentGetVersion() {
        return currentVersion().getVersion();
    }

    /** Convenience for {@link org.bukkit.Server#getBukkitVersion()}. */
    public static String currentGetBukkitVersion() {
        return currentVersion().getBukkitVersion();
    }

    /** Test hook: clear all state. */
    public static void resetForTests() {
        NAME_OVERRIDES.clear();
        CACHE.clear();
    }

    /** Test hook: install an override programmatically without a YAML file. */
    public static void putOverrideForTests(String pluginName, String mcVersion) {
        if (pluginName == null || mcVersion == null) return;
        NAME_OVERRIDES.put(pluginName, mcVersion);
    }

    /** Walk the parent-classloader chain on {@code cls} until either a
     *  cached tuple is found or the chain ends. Mirrors the pattern in
     *  {@code StubCallLog.resolveLoader} so that plugins which shade
     *  libraries via a child loader still resolve to the parent
     *  plugin's tuned version. */
    private static MCVersionTuple lookupClass(Class<?> cls) {
        ClassLoader cl = cls.getClassLoader();
        while (cl != null) {
            MCVersionTuple t = CACHE.get(cl);
            if (t != null) return t;
            cl = cl.getParent();
        }
        return null;
    }

    private static MCVersionTuple resolveTuple(String pluginName, String apiVersion) {
        if (pluginName != null) {
            String mc = NAME_OVERRIDES.get(pluginName);
            if (mc != null && !mc.isEmpty()) return MCVersionTuple.of(mc);
        }
        if (apiVersion != null) {
            MCVersionTuple t = lookupApiVersion(apiVersion);
            if (t != null) return t;
        }
        return DEFAULT;
    }

    /** Normalise {@code apiVersion} (strip trailing {@code .0}, trim,
     *  lowercase) and look up. Returns {@code null} for unknown keys
     *  so the caller can fall through to the default. */
    private static MCVersionTuple lookupApiVersion(String raw) {
        String key = raw.trim();
        if (key.isEmpty()) return null;
        MCVersionTuple t = API_VERSION_TO_MC.get(key);
        if (t != null) return t;
        // Strip a trailing ".0" — "1.21.0" → "1.21".
        if (key.endsWith(".0")) {
            String trimmed = key.substring(0, key.length() - 2);
            t = API_VERSION_TO_MC.get(trimmed);
            if (t != null) return t;
        }
        // Try the major.minor prefix — "1.21.3" → "1.21".
        int firstDot = key.indexOf('.');
        if (firstDot > 0) {
            int secondDot = key.indexOf('.', firstDot + 1);
            if (secondDot > 0) {
                t = API_VERSION_TO_MC.get(key.substring(0, secondDot));
                if (t != null) return t;
            }
        }
        return null;
    }

    /** Pre-formatted version strings for one MC release. Both fields
     *  are computed once at construction so the hot path is a map
     *  lookup + field read. */
    public record MCVersionTuple(String mc, String getVersion, String getBukkitVersion) {
        public static MCVersionTuple of(String mc) {
            return new MCVersionTuple(
                    mc,
                    "RDForward-bridge-1.0 (MC: " + mc + ")",
                    mc + "-R0.1-SNAPSHOT"
            );
        }
    }
}
