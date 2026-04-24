package com.github.martinambrus.rdforward.api.stub;

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

    private StubCallLog() {}

    public static void logOnce(String pluginId, String signature) {
        if (signature == null || signature.isEmpty()) return;
        String effectiveId = (pluginId == null || pluginId.isBlank()) ? UNKNOWN_PLUGIN : pluginId;
        Set<String> seen = SEEN.computeIfAbsent(effectiveId, k -> ConcurrentHashMap.newKeySet());
        if (seen.add(signature)) {
            LOG.warning("[StubCall] Plugin '" + effectiveId + "' called "
                    + signature
                    + " - unsupported in RDForward, ignored. Further calls from this plugin to this method will be silent.");
        }
    }

    public static boolean hasLogged(String pluginId, String signature) {
        String effectiveId = (pluginId == null || pluginId.isBlank()) ? UNKNOWN_PLUGIN : pluginId;
        Set<String> seen = SEEN.get(effectiveId);
        return seen != null && seen.contains(signature);
    }

    public static void resetForTests() {
        SEEN.clear();
    }
}
