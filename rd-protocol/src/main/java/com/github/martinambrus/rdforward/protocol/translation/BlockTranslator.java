package com.github.martinambrus.rdforward.protocol.translation;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Translates block IDs between protocol versions.
 *
 * When a server running at protocol version N sends block data
 * to a client at protocol version M (where M != N), the block IDs
 * must be translated to the closest equivalent in the client's version.
 *
 * Translation tables are loaded lazily from .properties resource files under
 * {@code block-mappings/} on first access for each version pair.
 *
 * Supported translation pairs:
 * - Classic -> RubyDung (50 -> 3 blocks)
 * - Classic -> Classic 0.0.20a (50 -> 41 blocks)
 * - Alpha -> RubyDung (82 -> 3 blocks)
 * - Alpha -> Classic (82 -> 50 blocks)
 * - RubyDung -> Classic (3 -> 50, pass-through)
 */
public class BlockTranslator {

    private static final ConcurrentHashMap<String, Map<Integer, Integer>> TRANSLATION_TABLES =
            new ConcurrentHashMap<String, Map<Integer, Integer>>();

    /** Sentinel value for keys that have no translation file. */
    private static final Map<Integer, Integer> NO_TABLE = Collections.emptyMap();

    private static String makeKey(ProtocolVersion from, ProtocolVersion to) {
        return from.name() + "->" + to.name();
    }

    /**
     * Get (or lazily load) the translation table for a version pair.
     * Returns null if no table exists for this pair.
     */
    private static Map<Integer, Integer> getTable(ProtocolVersion from, ProtocolVersion to) {
        String key = makeKey(from, to);
        Map<Integer, Integer> table = TRANSLATION_TABLES.get(key);
        if (table != null) {
            return table == NO_TABLE ? null : table;
        }
        // Lazy-load: determine the resource file for this version pair
        table = loadTableForPair(from, to);
        Map<Integer, Integer> existing = TRANSLATION_TABLES.putIfAbsent(key, table != null ? table : NO_TABLE);
        if (existing != null) {
            return existing == NO_TABLE ? null : existing;
        }
        return table;
    }

    /**
     * Determine which resource file to load for a given version pair, and load it.
     * Returns null if no mapping exists.
     */
    private static Map<Integer, Integer> loadTableForPair(ProtocolVersion from, ProtocolVersion to) {
        // Classic v7 -> Classic 0.0.15a
        if (from == ProtocolVersion.CLASSIC && to == ProtocolVersion.CLASSIC_0_0_15A) {
            return loadProperties("classic-to-classic015a.properties");
        }

        // Classic v7 -> Classic 0.0.20a
        if (from == ProtocolVersion.CLASSIC && to == ProtocolVersion.CLASSIC_0_0_20A) {
            return loadProperties("classic-to-classic020a.properties");
        }

        // Classic <-> RubyDung
        if (from == ProtocolVersion.CLASSIC && to == ProtocolVersion.RUBYDUNG) {
            return loadProperties("classic-to-rubydung.properties");
        }
        if (from == ProtocolVersion.RUBYDUNG && to == ProtocolVersion.CLASSIC) {
            return loadProperties("rubydung-to-classic.properties");
        }

        // All Alpha versions share the same mappings
        if (isAlpha(from)) {
            if (to == ProtocolVersion.RUBYDUNG) {
                return getSharedAlphaTable("alpha-to-rubydung.properties");
            }
            if (to == ProtocolVersion.CLASSIC) {
                return getSharedAlphaTable("alpha-to-classic.properties");
            }
        }

        return null;
    }

    /** Cache for shared Alpha translation tables (all Alpha versions use the same file). */
    private static final ConcurrentHashMap<String, Map<Integer, Integer>> ALPHA_TABLE_CACHE =
            new ConcurrentHashMap<String, Map<Integer, Integer>>();

    private static Map<Integer, Integer> getSharedAlphaTable(String resourceName) {
        Map<Integer, Integer> table = ALPHA_TABLE_CACHE.get(resourceName);
        if (table != null) {
            return table == NO_TABLE ? null : table;
        }
        table = loadProperties(resourceName);
        Map<Integer, Integer> existing = ALPHA_TABLE_CACHE.putIfAbsent(resourceName, table != null ? table : NO_TABLE);
        if (existing != null) {
            return existing == NO_TABLE ? null : existing;
        }
        return table;
    }

    private static boolean isAlpha(ProtocolVersion pv) {
        switch (pv) {
            case ALPHA_1_0_15:
            case ALPHA_1_0_16:
            case ALPHA_1_0_17:
            case ALPHA_1_1_0:
            case ALPHA_1_2_0:
            case ALPHA_1_2_2:
            case ALPHA_1_2_3:
            case ALPHA_1_2_5:
                return true;
            default:
                return false;
        }
    }

    /**
     * Load a block mapping from a .properties resource file.
     * Returns null if the resource is not found.
     */
    private static Map<Integer, Integer> loadProperties(String resourceName) {
        String path = "block-mappings/" + resourceName;
        try (InputStream is = BlockTranslator.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[BlockTranslator] Resource not found: " + path);
                return null;
            }
            Properties props = new Properties();
            props.load(is);

            Map<Integer, Integer> table = new HashMap<Integer, Integer>();
            for (String key : props.stringPropertyNames()) {
                try {
                    int sourceId = Integer.parseInt(key.trim());
                    int targetId = Integer.parseInt(props.getProperty(key).trim());
                    table.put(sourceId, targetId);
                } catch (NumberFormatException e) {
                    // Skip malformed entries
                }
            }
            return table;
        } catch (IOException e) {
            System.err.println("[BlockTranslator] Failed to load " + path + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Translate a block ID from one protocol version to another.
     *
     * @param blockId the block ID in the source version
     * @param from    the source protocol version
     * @param to      the target protocol version
     * @return the translated block ID, or the original ID if no mapping exists
     */
    public static int translate(int blockId, ProtocolVersion from, ProtocolVersion to) {
        if (from == to) {
            return blockId;
        }

        Map<Integer, Integer> table = getTable(from, to);
        if (table == null) {
            // No translation table for this version pair — return as-is
            return blockId;
        }

        Integer translated = table.get(blockId);
        // If no explicit mapping, return original (pass-through for identical IDs)
        // or 0 (Air) for blocks that don't exist in the target version
        return translated != null ? translated : 0;
    }

    /**
     * Translate an entire block array in-place.
     * Used for chunk data translation.
     */
    public static void translateArray(byte[] blocks, ProtocolVersion from, ProtocolVersion to) {
        if (from == to) return;

        Map<Integer, Integer> table = getTable(from, to);
        if (table == null) return;

        for (int i = 0; i < blocks.length; i++) {
            int blockId = blocks[i] & 0xFF;
            Integer translated = table.get(blockId);
            if (translated != null) {
                blocks[i] = (byte) (int) translated;
            }
        }
    }

    /**
     * Register a new translation table between two protocol versions.
     * This allows mods or future versions to add their own mappings.
     */
    public static void registerTranslation(ProtocolVersion from, ProtocolVersion to, Map<Integer, Integer> mapping) {
        TRANSLATION_TABLES.put(makeKey(from, to), mapping);
    }

    /**
     * Check if a translation table exists for the given version pair.
     */
    public static boolean hasTranslation(ProtocolVersion from, ProtocolVersion to) {
        return getTable(from, to) != null;
    }
}
