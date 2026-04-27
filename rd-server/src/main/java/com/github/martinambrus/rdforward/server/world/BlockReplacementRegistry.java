package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Lazy registry of per-version block replacement maps. Each
 * {@link ProtocolVersion} that introduced new blocks ships a YAML file
 * at {@code /replacements/<launcherId>.yml} on the rd-server classpath
 * containing only the blocks introduced in that version, mapped to
 * their immediate-prior-version visual equivalent (single-hop).
 *
 * <p>Files are loaded on demand: a version is read at most once per
 * JVM, the first time a {@link com.github.martinambrus.rdforward.server.world.VersionedBlockPolicy}
 * (or any other caller) asks about it. Versions with no shipped file
 * are cached as "no-file" so repeated lookups are cheap.
 *
 * <p>Mirrors the synchronized lazy-getter pattern used by
 * {@code BedrockProtocolConstants} — see CLAUDE.md "Lazy Loading and
 * Code Decoupling" for the project-wide convention.
 */
public final class BlockReplacementRegistry {

    private static final Logger LOG = Logger.getLogger("RDForward/Replacements");

    /** {@code blockName -> ProtocolVersion that first introduced it}. Built
     *  lazily as {@link #introducedAfter(String, ProtocolVersion)} walks
     *  the version table on cache misses. */
    private static final ConcurrentHashMap<String, ProtocolVersion> INTRODUCED_IN =
            new ConcurrentHashMap<>();

    /** {@code ProtocolVersion -> single-hop replacement map (block -> prior block)}.
     *  Loaded on demand. */
    private static final ConcurrentHashMap<ProtocolVersion, Map<String, String>> DELTAS =
            new ConcurrentHashMap<>();

    /** Versions whose YAML file is missing on the classpath. Cached so
     *  the missing-file decision is paid once, not on every lookup. */
    private static final Set<ProtocolVersion> NO_FILE = ConcurrentHashMap.newKeySet();

    private BlockReplacementRegistry() {}

    /** @return the immediate-prior-version replacement registered for
     *  {@code blockName} in {@code v}'s delta file, or {@code null} if
     *  this version did not introduce the block. */
    public static String getReplacement(ProtocolVersion v, String blockName) {
        if (v == null || blockName == null) return null;
        return loadIfNeeded(v).get(blockName);
    }

    /** @return {@code true} if {@code blockName} was introduced in any
     *  version with sortOrder strictly greater than {@code v} — i.e. the
     *  block is NOT in {@code v}'s native vocabulary and a coercion
     *  walk is required. {@code false} for blocks always present (e.g.
     *  {@code minecraft:stone}) or introduced in {@code v} or earlier. */
    public static boolean introducedAfter(String blockName, ProtocolVersion v) {
        if (blockName == null || v == null) return false;
        ProtocolVersion vi = INTRODUCED_IN.get(blockName);
        if (vi != null) return vi.getSortOrder() > v.getSortOrder();
        // Not yet seen — walk every known version's delta file looking
        // for the first one that introduces this block. Each file is
        // loaded at most once thanks to loadIfNeeded's cache.
        for (ProtocolVersion candidate : ProtocolVersion.values()) {
            Map<String, String> d = loadIfNeeded(candidate);
            if (d.containsKey(blockName)) {
                INTRODUCED_IN.put(blockName, candidate);
                return candidate.getSortOrder() > v.getSortOrder();
            }
        }
        // Block name appears nowhere in any replacement file. Treat as
        // "always there" (e.g. minecraft:stone has been present since
        // RubyDung, no file should mention it). Caller's chain walk
        // ends with this name considered in-vocabulary.
        return false;
    }

    /** @return the replacement-map for {@code v}, loaded from the
     *  classpath if needed. Empty map (immutable) when no file is
     *  shipped for that version. */
    private static Map<String, String> loadIfNeeded(ProtocolVersion v) {
        Map<String, String> d = DELTAS.get(v);
        if (d != null) return d;
        if (NO_FILE.contains(v)) return Collections.emptyMap();
        synchronized (BlockReplacementRegistry.class) {
            d = DELTAS.get(v);
            if (d != null) return d;
            if (NO_FILE.contains(v)) return Collections.emptyMap();
            d = loadYaml("/replacements/" + v.getLauncherId() + ".yml");
            if (d == null) {
                NO_FILE.add(v);
                return Collections.emptyMap();
            }
            DELTAS.put(v, Collections.unmodifiableMap(d));
            return d;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> loadYaml(String resourcePath) {
        try (InputStream is = BlockReplacementRegistry.class.getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            Object parsed = new Yaml().load(is);
            if (!(parsed instanceof Map<?, ?> raw)) return null;
            Map<String, String> out = new HashMap<>(raw.size() * 2);
            for (Map.Entry<?, ?> e : raw.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                out.put(e.getKey().toString(), e.getValue().toString());
            }
            return out;
        } catch (IOException e) {
            LOG.warning("Failed to load " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    /** Test-only hook to wipe all caches. Useful when a test needs to
     *  observe lazy-load behaviour from a known empty state. */
    public static void resetForTests() {
        DELTAS.clear();
        INTRODUCED_IN.clear();
        NO_FILE.clear();
    }
}
