package com.github.martinambrus.rdforward.protocol.translation;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Translates block IDs between protocol versions.
 *
 * When a server running at protocol version N sends block data
 * to a client at protocol version M (where M != N), the block IDs
 * must be translated to the closest equivalent in the client's version.
 *
 * Translation tables are loaded from .properties resource files under
 * {@code block-mappings/}. Each file contains one mapping per line in
 * {@code sourceId=targetId} format. This makes the tables data-driven
 * and editable without recompiling.
 *
 * Supported translation pairs:
 * - Classic -> RubyDung (50 -> 3 blocks)
 * - Alpha -> RubyDung (82 -> 3 blocks)
 * - Alpha -> Classic (82 -> 50 blocks)
 * - RubyDung -> Classic (3 -> 50, pass-through)
 */
public class BlockTranslator {

    private static final Map<String, Map<Integer, Integer>> TRANSLATION_TABLES = new HashMap<>();

    static {
        // Load all translation tables from resource files
        loadTable(ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG, "classic-to-rubydung.properties");
        loadTable(ProtocolVersion.RUBYDUNG, ProtocolVersion.CLASSIC, "rubydung-to-classic.properties");

        // Alpha tables — all Alpha versions share the same block mappings
        Map<Integer, Integer> alphaToRd = loadProperties("alpha-to-rubydung.properties");
        if (alphaToRd != null) {
            TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.RUBYDUNG), alphaToRd);
            TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_0_16, ProtocolVersion.RUBYDUNG), alphaToRd);
            TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_2_3, ProtocolVersion.RUBYDUNG), alphaToRd);
            TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_2_5, ProtocolVersion.RUBYDUNG), alphaToRd);
        }

        Map<Integer, Integer> alphaToClassic = loadProperties("alpha-to-classic.properties");
        if (alphaToClassic != null) {
            TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.CLASSIC), alphaToClassic);
            TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_0_16, ProtocolVersion.CLASSIC), alphaToClassic);
            TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_2_3, ProtocolVersion.CLASSIC), alphaToClassic);
            TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_2_5, ProtocolVersion.CLASSIC), alphaToClassic);
        }
    }

    private static String makeKey(ProtocolVersion from, ProtocolVersion to) {
        return from.name() + "->" + to.name();
    }

    /**
     * Load a translation table from a resource file and register it.
     */
    private static void loadTable(ProtocolVersion from, ProtocolVersion to, String resourceName) {
        Map<Integer, Integer> table = loadProperties(resourceName);
        if (table != null) {
            TRANSLATION_TABLES.put(makeKey(from, to), table);
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

            Map<Integer, Integer> table = new HashMap<>();
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

        String key = makeKey(from, to);
        Map<Integer, Integer> table = TRANSLATION_TABLES.get(key);
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

        String key = makeKey(from, to);
        Map<Integer, Integer> table = TRANSLATION_TABLES.get(key);
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
        return TRANSLATION_TABLES.containsKey(makeKey(from, to));
    }
}
