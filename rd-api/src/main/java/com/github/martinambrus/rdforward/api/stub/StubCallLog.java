package com.github.martinambrus.rdforward.api.stub;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Shared sink for one-time warnings emitted by auto-generated bridge
 * stubs when a plugin invokes an unsupported write-path method.
 *
 * <p>Stub code emitted by the {@code codegen} module calls
 * {@link #logOnce(String, String)} from every {@code setX} / {@code addX} /
 * {@code registerX} / {@code broadcastX} etc. The first call for a
 * given {@code (pluginId, signature)} pair logs a warning at
 * {@link java.util.logging.Level#WARNING}; subsequent calls with the
 * same key pair are silent.
 *
 * <p>The intent is to give plugin authors and server operators a clear
 * signal that a feature is not implemented, without drowning logs when
 * a plugin calls the same method in a tight loop. Read-path methods
 * (plain getters) do not log; they return sensible defaults silently.
 *
 * <p>Plugin identity is provided by the calling bridge. Pass the
 * plugin name (from {@code plugin.yml}, {@code fabric.mod.json}, etc.)
 * if known; pass {@code null} or an empty string for "unknown" and a
 * single fallback entry will be used for aggregation.
 */
public final class StubCallLog {

    private static final Logger LOG = Logger.getLogger("RDForward/StubCall");

    private static final String UNKNOWN_PLUGIN = "<unknown>";

    private static final ConcurrentHashMap<String, Set<String>> SEEN = new ConcurrentHashMap<>();

    /** Broadcast sink installed by the host (Bukkit bridge) so plugin
     *  authors and operators see the same warning in-game without
     *  having to tail server logs. Active for first-time hits only —
     *  same dedup as the JUL line. */
    private static volatile java.util.function.Consumer<String> broadcastSink;

    /** Maps a plugin's ClassLoader to its name. Populated by the host
     *  bridge as it loads each plugin jar; consulted by {@link #logOnce}
     *  to resolve the calling plugin when the stub site passes a null
     *  {@code pluginId}. ClassLoader identity (==) is the lookup key —
     *  every plugin gets its own URLClassLoader, so the mapping is
     *  unambiguous. */
    private static final Map<ClassLoader, String> PLUGINS_BY_LOADER = new ConcurrentHashMap<>();

    private static final StackWalker WALKER =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private StubCallLog() {}

    /** Install (or replace) the broadcast sink. {@code null} disables
     *  in-game broadcasting and reverts to log-only behaviour. The
     *  sink runs on the calling thread; implementations must dispatch
     *  to a server thread internally if their broadcast API requires
     *  it. */
    public static void setBroadcastSink(java.util.function.Consumer<String> sink) {
        broadcastSink = sink;
    }

    /** Register a plugin's ClassLoader so {@link #logOnce} can resolve
     *  the plugin name from the call stack when the stub site passes
     *  {@code null}. Idempotent — re-registering with the same name
     *  is a no-op. Bridges should call this immediately after creating
     *  the per-plugin classloader. */
    public static void registerPluginLoader(ClassLoader loader, String pluginName) {
        if (loader == null || pluginName == null || pluginName.isBlank()) return;
        PLUGINS_BY_LOADER.put(loader, pluginName);
    }

    /** Drop the mapping for a plugin classloader. Bridges should call
     *  this when unloading a plugin so subsequent stub calls (e.g. from
     *  a daemon thread that outlived the plugin) fall back to
     *  {@link #UNKNOWN_PLUGIN} rather than reporting a stale name. */
    public static void unregisterPluginLoader(ClassLoader loader) {
        if (loader == null) return;
        PLUGINS_BY_LOADER.remove(loader);
    }

    public static void logOnce(String pluginId, String signature) {
        if (signature == null || signature.isEmpty()) return;
        String effectiveId = (pluginId == null || pluginId.isBlank()) ? resolveCallerPlugin() : pluginId;
        if (effectiveId == null || effectiveId.isBlank()) effectiveId = UNKNOWN_PLUGIN;
        Set<String> seen = SEEN.computeIfAbsent(effectiveId, k -> ConcurrentHashMap.newKeySet());
        if (seen.add(signature)) {
            String msg = "[StubCall] Plugin '" + effectiveId + "' called "
                    + signature
                    + " - unsupported in RDForward, ignored. Further calls from this plugin to this method will be silent.";
            LOG.warning(msg);
            java.util.function.Consumer<String> sink = broadcastSink;
            if (sink != null) {
                try { sink.accept(msg); } catch (Throwable ignored) {}
            }
        }
    }

    /** Walk the stack and return the name of the first frame whose
     *  declaring class was loaded by a registered plugin classloader.
     *  {@code null} when no plugin is on the stack — caller should
     *  fall back to {@link #UNKNOWN_PLUGIN}. Skips frames inside this
     *  class so the walker doesn't pick up its own implementation. */
    private static String resolveCallerPlugin() {
        if (PLUGINS_BY_LOADER.isEmpty()) return null;
        return WALKER.walk(stream -> stream
                .filter(f -> f.getDeclaringClass() != StubCallLog.class)
                .map(StackWalker.StackFrame::getDeclaringClass)
                .map(StubCallLog::resolveLoader)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null));
    }

    /** @return the registered plugin name for {@code cls}'s classloader,
     *  walking parent classloaders (some plugins shade libraries via a
     *  child loader) until either a registered name is found or the
     *  chain ends. {@code null} when no loader in the chain belongs to
     *  a registered plugin. */
    private static String resolveLoader(Class<?> cls) {
        ClassLoader cl = cls.getClassLoader();
        while (cl != null) {
            String name = PLUGINS_BY_LOADER.get(cl);
            if (name != null) return name;
            cl = cl.getParent();
        }
        return null;
    }

    public static boolean hasLogged(String pluginId, String signature) {
        String effectiveId = (pluginId == null || pluginId.isBlank()) ? UNKNOWN_PLUGIN : pluginId;
        Set<String> seen = SEEN.get(effectiveId);
        return seen != null && seen.contains(signature);
    }

    public static void resetForTests() {
        SEEN.clear();
        PLUGINS_BY_LOADER.clear();
    }
}
