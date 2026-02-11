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
 *
 * Translation tables:
 * - Classic -> RubyDung: Classic has 50 blocks, RubyDung has 3
 * - Alpha -> RubyDung: Alpha has ~82 blocks, RubyDung has 3
 * - Alpha -> Classic: Alpha has ~82 blocks, Classic has 50
 */
public class BlockTranslator {

    /**
     * Map from (sourceVersion, targetVersion) -> block ID mapping.
     * The block ID mapping maps sourceBlockId -> targetBlockId.
     */
    private static final Map<String, Map<Integer, Integer>> TRANSLATION_TABLES = new HashMap<String, Map<Integer, Integer>>();

    static {
        // Classic -> RubyDung block translations
        // RubyDung only has: 0=Air, 2=Grass, 4=Cobblestone
        // Classic has blocks 0-49 (Air through Obsidian)
        Map<Integer, Integer> classicToRubyDung = new HashMap<Integer, Integer>();
        classicToRubyDung.put(0, 0);   // Air -> Air
        classicToRubyDung.put(1, 4);   // Stone -> Cobblestone
        classicToRubyDung.put(2, 2);   // Grass -> Grass
        classicToRubyDung.put(3, 2);   // Dirt -> Grass
        classicToRubyDung.put(4, 4);   // Cobblestone -> Cobblestone
        classicToRubyDung.put(5, 4);   // Planks -> Cobblestone
        classicToRubyDung.put(6, 0);   // Sapling -> Air
        classicToRubyDung.put(7, 4);   // Bedrock -> Cobblestone
        classicToRubyDung.put(8, 0);   // Flowing Water -> Air
        classicToRubyDung.put(9, 0);   // Still Water -> Air
        classicToRubyDung.put(10, 0);  // Flowing Lava -> Air
        classicToRubyDung.put(11, 0);  // Still Lava -> Air
        classicToRubyDung.put(12, 2);  // Sand -> Grass
        classicToRubyDung.put(13, 4);  // Gravel -> Cobblestone
        classicToRubyDung.put(14, 4);  // Gold Ore -> Cobblestone
        classicToRubyDung.put(15, 4);  // Iron Ore -> Cobblestone
        classicToRubyDung.put(16, 4);  // Coal Ore -> Cobblestone
        classicToRubyDung.put(17, 4);  // Log -> Cobblestone
        classicToRubyDung.put(18, 2);  // Leaves -> Grass
        classicToRubyDung.put(19, 4);  // Sponge -> Cobblestone
        classicToRubyDung.put(20, 0);  // Glass -> Air
        // Cloth/Wool colors (21-36 in Classic) -> Cobblestone
        for (int i = 21; i <= 36; i++) {
            classicToRubyDung.put(i, 4);
        }
        classicToRubyDung.put(37, 0);  // Yellow Flower -> Air
        classicToRubyDung.put(38, 0);  // Red Flower -> Air
        classicToRubyDung.put(39, 0);  // Brown Mushroom -> Air
        classicToRubyDung.put(40, 0);  // Red Mushroom -> Air
        classicToRubyDung.put(41, 4);  // Gold Block -> Cobblestone
        classicToRubyDung.put(42, 4);  // Iron Block -> Cobblestone
        classicToRubyDung.put(43, 4);  // Double Slab -> Cobblestone
        classicToRubyDung.put(44, 4);  // Slab -> Cobblestone
        classicToRubyDung.put(45, 4);  // Bricks -> Cobblestone
        classicToRubyDung.put(46, 4);  // TNT -> Cobblestone
        classicToRubyDung.put(47, 4);  // Bookshelf -> Cobblestone
        classicToRubyDung.put(48, 4);  // Mossy Cobblestone -> Cobblestone
        classicToRubyDung.put(49, 4);  // Obsidian -> Cobblestone
        TRANSLATION_TABLES.put(makeKey(ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG), classicToRubyDung);

        // Alpha -> RubyDung block translations (Alpha has IDs 0-91 with gaps)
        Map<Integer, Integer> alphaToRubyDung = new HashMap<Integer, Integer>(classicToRubyDung);
        // Add Alpha-specific blocks (50-91)
        alphaToRubyDung.put(35, 4);   // Wool (Alpha uses 35 instead of 21-36)
        alphaToRubyDung.put(50, 0);   // Torch -> Air
        alphaToRubyDung.put(51, 0);   // Fire -> Air
        alphaToRubyDung.put(52, 4);   // Spawner -> Cobblestone
        alphaToRubyDung.put(53, 4);   // Stairs -> Cobblestone
        alphaToRubyDung.put(54, 4);   // Chest -> Cobblestone
        alphaToRubyDung.put(55, 0);   // Redstone Wire -> Air
        alphaToRubyDung.put(56, 4);   // Diamond Ore -> Cobblestone
        alphaToRubyDung.put(57, 4);   // Diamond Block -> Cobblestone
        alphaToRubyDung.put(58, 4);   // Crafting Table -> Cobblestone
        alphaToRubyDung.put(59, 0);   // Crops -> Air
        alphaToRubyDung.put(60, 2);   // Farmland -> Grass
        alphaToRubyDung.put(61, 4);   // Furnace -> Cobblestone
        alphaToRubyDung.put(62, 4);   // Lit Furnace -> Cobblestone
        alphaToRubyDung.put(63, 0);   // Sign (standing) -> Air
        alphaToRubyDung.put(64, 0);   // Wooden Door -> Air
        alphaToRubyDung.put(65, 0);   // Ladder -> Air
        alphaToRubyDung.put(66, 0);   // Rail -> Air
        alphaToRubyDung.put(67, 4);   // Cobblestone Stairs -> Cobblestone
        alphaToRubyDung.put(68, 0);   // Sign (wall) -> Air
        alphaToRubyDung.put(69, 0);   // Lever -> Air
        alphaToRubyDung.put(70, 0);   // Stone Pressure Plate -> Air
        alphaToRubyDung.put(71, 0);   // Iron Door -> Air
        alphaToRubyDung.put(72, 0);   // Wooden Pressure Plate -> Air
        alphaToRubyDung.put(73, 4);   // Redstone Ore -> Cobblestone
        alphaToRubyDung.put(74, 4);   // Lit Redstone Ore -> Cobblestone
        alphaToRubyDung.put(75, 0);   // Unlit Redstone Torch -> Air
        alphaToRubyDung.put(76, 0);   // Redstone Torch -> Air
        alphaToRubyDung.put(77, 0);   // Button -> Air
        alphaToRubyDung.put(78, 0);   // Snow Layer -> Air
        alphaToRubyDung.put(79, 0);   // Ice -> Air
        alphaToRubyDung.put(80, 4);   // Snow Block -> Cobblestone
        alphaToRubyDung.put(81, 4);   // Cactus -> Cobblestone
        alphaToRubyDung.put(82, 4);   // Clay -> Cobblestone
        alphaToRubyDung.put(83, 0);   // Sugar Cane -> Air
        alphaToRubyDung.put(84, 4);   // Jukebox -> Cobblestone
        alphaToRubyDung.put(85, 4);   // Fence -> Cobblestone
        alphaToRubyDung.put(86, 4);   // Pumpkin -> Cobblestone
        alphaToRubyDung.put(87, 4);   // Netherrack -> Cobblestone
        alphaToRubyDung.put(88, 4);   // Soul Sand -> Cobblestone
        alphaToRubyDung.put(89, 4);   // Glowstone -> Cobblestone
        alphaToRubyDung.put(90, 0);   // Nether Portal -> Air
        alphaToRubyDung.put(91, 4);   // Jack o'Lantern -> Cobblestone
        TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.RUBYDUNG), alphaToRubyDung);
        TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_2_6, ProtocolVersion.RUBYDUNG), alphaToRubyDung);

        // Alpha -> Classic: blocks 0-49 are identical; blocks 50+ don't exist in Classic
        Map<Integer, Integer> alphaToClassic = new HashMap<Integer, Integer>();
        for (int i = 50; i <= 91; i++) {
            // Map Alpha-only blocks to closest Classic equivalents
            if (i == 50 || i == 51 || i == 55 || i == 59 || i == 63 || i == 64 ||
                i == 65 || i == 66 || i == 68 || i == 69 || i == 70 || i == 71 ||
                i == 72 || i == 75 || i == 76 || i == 77 || i == 78 || i == 79 ||
                i == 83 || i == 90) {
                alphaToClassic.put(i, 0);  // Transparent/small -> Air
            } else {
                alphaToClassic.put(i, 4);  // Solid -> Cobblestone
            }
        }
        // Alpha uses block 35 for wool; Classic uses 21-36 for cloth colors
        // Default: Alpha wool -> Classic white cloth (21)
        alphaToClassic.put(35, 21);
        TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.CLASSIC), alphaToClassic);
        TRANSLATION_TABLES.put(makeKey(ProtocolVersion.ALPHA_1_2_6, ProtocolVersion.CLASSIC), alphaToClassic);
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
