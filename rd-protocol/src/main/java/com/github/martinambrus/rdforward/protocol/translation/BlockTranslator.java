package com.github.martinambrus.rdforward.protocol.translation;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates block IDs between protocol versions.
 *
 * When a server running at protocol version N sends block data
 * to a client at protocol version M (where M < N), the block IDs
 * must be translated to the closest equivalent in the client's version.
 *
 * This follows the same architecture as ViaVersion/ViaBackwards:
 * translators are defined between adjacent versions and can be chained.
 */
public class BlockTranslator {

    /**
     * Map from (sourceVersion, targetVersion) -> block ID mapping.
     * The block ID mapping maps sourceBlockId -> targetBlockId.
     */
    private static final Map<String, Map<Integer, Integer>> TRANSLATION_TABLES = new HashMap<String, Map<Integer, Integer>>();

    static {
        // Alpha -> RubyDung block translations
        // RubyDung only has: 0=Air, 2=Grass, 4=Cobblestone
        Map<Integer, Integer> alphaToRubyDung = new HashMap<Integer, Integer>();
        alphaToRubyDung.put(0, 0);   // Air -> Air
        alphaToRubyDung.put(1, 4);   // Stone -> Cobblestone
        alphaToRubyDung.put(2, 2);   // Grass -> Grass
        alphaToRubyDung.put(3, 2);   // Dirt -> Grass (closest match)
        alphaToRubyDung.put(4, 4);   // Cobblestone -> Cobblestone
        alphaToRubyDung.put(5, 4);   // Planks -> Cobblestone (solid block)
        alphaToRubyDung.put(6, 0);   // Sapling -> Air (no plants in RD)
        alphaToRubyDung.put(7, 4);   // Bedrock -> Cobblestone
        alphaToRubyDung.put(8, 0);   // Flowing Water -> Air
        alphaToRubyDung.put(9, 0);   // Still Water -> Air
        alphaToRubyDung.put(10, 0);  // Flowing Lava -> Air
        alphaToRubyDung.put(11, 0);  // Still Lava -> Air
        alphaToRubyDung.put(12, 2);  // Sand -> Grass
        alphaToRubyDung.put(13, 4);  // Gravel -> Cobblestone
        alphaToRubyDung.put(14, 4);  // Gold Ore -> Cobblestone
        alphaToRubyDung.put(15, 4);  // Iron Ore -> Cobblestone
        alphaToRubyDung.put(16, 4);  // Coal Ore -> Cobblestone
        alphaToRubyDung.put(17, 4);  // Log -> Cobblestone
        alphaToRubyDung.put(18, 2);  // Leaves -> Grass
        alphaToRubyDung.put(19, 4);  // Sponge -> Cobblestone
        alphaToRubyDung.put(20, 0);  // Glass -> Air (transparent)
        // Ores and mineral blocks -> Cobblestone
        for (int i = 21; i <= 26; i++) {
            alphaToRubyDung.put(i, 4);
        }
        // Flowers/mushrooms -> Air (decorative)
        for (int i = 37; i <= 40; i++) {
            alphaToRubyDung.put(i, 0);
        }
        // Gold/Iron/Diamond blocks -> Cobblestone
        alphaToRubyDung.put(41, 4);
        alphaToRubyDung.put(42, 4);
        // Slabs -> Cobblestone
        alphaToRubyDung.put(43, 4);
        alphaToRubyDung.put(44, 4);
        // Bricks, TNT, Bookshelf, Mossy Cobble, Obsidian -> Cobblestone
        for (int i = 45; i <= 49; i++) {
            alphaToRubyDung.put(i, 4);
        }
        // Torch, Fire -> Air
        alphaToRubyDung.put(50, 0);
        alphaToRubyDung.put(51, 0);
        // Spawner -> Cobblestone
        alphaToRubyDung.put(52, 4);
        // Stairs -> Cobblestone
        alphaToRubyDung.put(53, 4);
        // Chest -> Cobblestone
        alphaToRubyDung.put(54, 4);
        // Redstone wire -> Air
        alphaToRubyDung.put(55, 0);
        // Diamond ore/block -> Cobblestone
        alphaToRubyDung.put(56, 4);
        alphaToRubyDung.put(57, 4);
        // Crafting table -> Cobblestone
        alphaToRubyDung.put(58, 4);
        // Crops -> Air
        alphaToRubyDung.put(59, 0);
        // Farmland -> Grass
        alphaToRubyDung.put(60, 2);
        // Furnace -> Cobblestone
        alphaToRubyDung.put(61, 4);
        alphaToRubyDung.put(62, 4);
        // Signs -> Air
        alphaToRubyDung.put(63, 0);
        alphaToRubyDung.put(68, 0);
        // Doors -> Air
        alphaToRubyDung.put(64, 0);
        alphaToRubyDung.put(71, 0);
        // Ladder -> Air
        alphaToRubyDung.put(65, 0);
        // Rail -> Air
        alphaToRubyDung.put(66, 0);
        // Cobblestone stairs -> Cobblestone
        alphaToRubyDung.put(67, 4);
        // Lever, pressure plates, buttons -> Air
        alphaToRubyDung.put(69, 0);
        alphaToRubyDung.put(70, 0);
        alphaToRubyDung.put(72, 0);
        alphaToRubyDung.put(77, 0);
        // Redstone ore -> Cobblestone
        alphaToRubyDung.put(73, 4);
        alphaToRubyDung.put(74, 4);
        // Redstone torch -> Air
        alphaToRubyDung.put(75, 0);
        alphaToRubyDung.put(76, 0);
        // Snow/Ice -> Air/Cobblestone
        alphaToRubyDung.put(78, 0);  // Snow layer -> Air
        alphaToRubyDung.put(79, 0);  // Ice -> Air (transparent)
        alphaToRubyDung.put(80, 4);  // Snow block -> Cobblestone
        // Cactus -> Cobblestone
        alphaToRubyDung.put(81, 4);
        // Clay -> Cobblestone
        alphaToRubyDung.put(82, 4);
        // Sugar cane -> Air
        alphaToRubyDung.put(83, 0);
        // Jukebox -> Cobblestone
        alphaToRubyDung.put(84, 4);
        // Fence -> Cobblestone
        alphaToRubyDung.put(85, 4);
        // Pumpkin/Netherrack/Soul Sand/Glowstone -> Cobblestone
        for (int i = 86; i <= 89; i++) {
            alphaToRubyDung.put(i, 4);
        }
        // Nether portal -> Air
        alphaToRubyDung.put(90, 0);
        // Jack o'Lantern -> Cobblestone
        alphaToRubyDung.put(91, 4);

        // Wool colors (35 with metadata 0-15) all map to Cobblestone
        alphaToRubyDung.put(35, 4);

        TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_2, ProtocolVersion.RUBYDUNG_1), alphaToRubyDung);
    }

    private static String makeKey(ProtocolVersion from, ProtocolVersion to) {
        return from.getVersionNumber() + "->" + to.getVersionNumber();
    }

    /**
     * Translate a block ID from one protocol version to another.
     *
     * @param blockId the block ID in the source version
     * @param from    the source protocol version
     * @param to      the target protocol version
     * @return the translated block ID, or 0 (Air) if no mapping exists
     */
    public static int translate(int blockId, ProtocolVersion from, ProtocolVersion to) {
        if (from == to) {
            return blockId;
        }

        String key = makeKey(from, to);
        Map<Integer, Integer> table = TRANSLATION_TABLES.get(key);
        if (table == null) {
            // No translation table for this version pair — return as-is
            // (same version or forward-compatible)
            return blockId;
        }

        Integer translated = table.get(blockId);
        return translated != null ? translated : 0; // default to Air if unknown
    }

    /**
     * Register a new translation table between two protocol versions.
     * This allows mods or future versions to add their own mappings.
     */
    public static void registerTranslation(ProtocolVersion from, ProtocolVersion to, Map<Integer, Integer> mapping) {
        TRANSLATION_TABLES.put(makeKey(from, to), mapping);
    }
}
