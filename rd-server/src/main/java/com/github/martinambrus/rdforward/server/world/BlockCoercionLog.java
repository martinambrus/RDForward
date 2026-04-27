package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.api.world.BlockType;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * One-shot warning sink for block coercions performed by
 * {@link com.github.martinambrus.rdforward.api.world.BlockPolicy}. The
 * first time a given source block is coerced in a given world, this
 * emits a WARNING — subsequent coercions of the same source block in
 * the same world stay silent so a plugin in a tight placement loop
 * doesn't drown the log.
 *
 * <p>Mirrors the dedup pattern in {@code rd-api/.../api/stub/StubCallLog}.
 *
 * <p>State is global (per-JVM), keyed by world name. Cleared via
 * {@link #resetForTests()} between unit tests.
 */
public final class BlockCoercionLog {

    private static final Logger LOG = Logger.getLogger("RDForward/BlockCoerce");

    /** {@code worldName -> set of source block names already logged in that world}. */
    private static final ConcurrentHashMap<String, Set<String>> SEEN = new ConcurrentHashMap<>();

    private BlockCoercionLog() {}

    /** Log the first coercion of {@code requested -> coerced} in
     *  {@code worldName}. Subsequent calls with the same
     *  {@code (worldName, requested.getName())} pair are silent. */
    public static void logOnce(String worldName, BlockType requested, BlockType coerced) {
        if (requested == null || coerced == null) return;
        String wn = (worldName == null || worldName.isBlank()) ? "<unknown>" : worldName;
        Set<String> seen = SEEN.computeIfAbsent(wn, k -> ConcurrentHashMap.newKeySet());
        String key = requested.getName();
        if (key == null) key = "<id:" + requested.getId() + ">";
        if (seen.add(key)) {
            LOG.warning("[BlockCoerce] World '" + wn + "': '" + key
                    + "' is unsupported here; coerced to '" + coerced.getName()
                    + "'. Subsequent placements of this block in this world will be silent.");
        }
    }

    /** Test-only: returns true if {@code requested} has already been
     *  coerced (and logged) in {@code worldName}. */
    public static boolean hasLogged(String worldName, BlockType requested) {
        if (requested == null) return false;
        String wn = (worldName == null || worldName.isBlank()) ? "<unknown>" : worldName;
        Set<String> seen = SEEN.get(wn);
        return seen != null && seen.contains(requested.getName());
    }

    /** Test-only hook to wipe dedup state between runs. */
    public static void resetForTests() {
        SEEN.clear();
    }
}
