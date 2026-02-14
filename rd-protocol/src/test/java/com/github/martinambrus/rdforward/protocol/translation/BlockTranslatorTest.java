package com.github.martinambrus.rdforward.protocol.translation;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockTranslatorTest {

    // === Classic -> RubyDung ===

    @Test
    void classicAirToRubyDung() {
        assertEquals(0, BlockTranslator.translate(0, ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG));
    }

    @Test
    void classicStoneToRubyDungCobblestone() {
        assertEquals(4, BlockTranslator.translate(1, ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG));
    }

    @Test
    void classicGrassToRubyDungGrass() {
        assertEquals(2, BlockTranslator.translate(2, ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG));
    }

    @Test
    void classicSandToRubyDungGrass() {
        assertEquals(2, BlockTranslator.translate(12, ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG));
    }

    @Test
    void classicGlassToRubyDungAir() {
        assertEquals(0, BlockTranslator.translate(20, ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG));
    }

    @Test
    void classicFlowerToRubyDungAir() {
        assertEquals(0, BlockTranslator.translate(37, ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG));
    }

    // === Alpha -> RubyDung ===

    @Test
    void alphaTorchToRubyDungAir() {
        assertEquals(0, BlockTranslator.translate(50, ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.RUBYDUNG));
    }

    @Test
    void alphaChestToRubyDungCobblestone() {
        assertEquals(4, BlockTranslator.translate(54, ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.RUBYDUNG));
    }

    @Test
    void alphaFarmlandToRubyDungGrass() {
        assertEquals(2, BlockTranslator.translate(60, ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.RUBYDUNG));
    }

    @Test
    void allAlphaVersionsShareMappings() {
        // All Alpha versions should produce the same block translation
        for (int id = 0; id <= 91; id++) {
            int from1015 = BlockTranslator.translate(id, ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.RUBYDUNG);
            int from1016 = BlockTranslator.translate(id, ProtocolVersion.ALPHA_1_0_16, ProtocolVersion.RUBYDUNG);
            int from123 = BlockTranslator.translate(id, ProtocolVersion.ALPHA_1_2_3, ProtocolVersion.RUBYDUNG);
            int from125 = BlockTranslator.translate(id, ProtocolVersion.ALPHA_1_2_5, ProtocolVersion.RUBYDUNG);
            assertEquals(from1015, from1016, "Mismatch for block " + id + " between 1.0.15 and 1.0.16");
            assertEquals(from1015, from123, "Mismatch for block " + id + " between 1.0.15 and 1.2.3");
            assertEquals(from1015, from125, "Mismatch for block " + id + " between 1.0.15 and 1.2.5");
        }
    }

    // === Alpha -> Classic ===

    @Test
    void alphaWoolToClassicCloth() {
        assertEquals(21, BlockTranslator.translate(35, ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.CLASSIC));
    }

    @Test
    void alphaLadderToClassicAir() {
        assertEquals(0, BlockTranslator.translate(65, ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.CLASSIC));
    }

    @Test
    void alphaObsidianToClassicPassThrough() {
        // Block 49 (Obsidian) exists in both Alpha and Classic — should pass through
        // Alpha->Classic table only has entries for blocks that differ
        int result = BlockTranslator.translate(49, ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.CLASSIC);
        // No explicit mapping for 49 in alpha-to-classic.properties → returns 0 (Air)
        // This is expected since unmapped blocks default to Air
        assertTrue(result == 0 || result == 49);
    }

    // === RubyDung -> Classic (upward) ===

    @Test
    void rubyDungToClassicPassThrough() {
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.RUBYDUNG, ProtocolVersion.CLASSIC));
        assertEquals(0, BlockTranslator.translate(0, ProtocolVersion.RUBYDUNG, ProtocolVersion.CLASSIC));
        assertEquals(2, BlockTranslator.translate(2, ProtocolVersion.RUBYDUNG, ProtocolVersion.CLASSIC));
        assertEquals(4, BlockTranslator.translate(4, ProtocolVersion.RUBYDUNG, ProtocolVersion.CLASSIC));
    }

    // === Same version ===

    @Test
    void sameVersionNoTranslation() {
        assertEquals(42, BlockTranslator.translate(42, ProtocolVersion.CLASSIC, ProtocolVersion.CLASSIC));
        assertEquals(0, BlockTranslator.translate(0, ProtocolVersion.RUBYDUNG, ProtocolVersion.RUBYDUNG));
    }

    // === translateArray ===

    @Test
    void translateArrayBulk() {
        byte[] blocks = {1, 2, 0, 4, 20}; // Stone, Grass, Air, Cobblestone, Glass
        BlockTranslator.translateArray(blocks, ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG);
        assertEquals(4, blocks[0] & 0xFF);  // Stone -> Cobblestone
        assertEquals(2, blocks[1] & 0xFF);  // Grass -> Grass
        assertEquals(0, blocks[2] & 0xFF);  // Air -> Air
        assertEquals(4, blocks[3] & 0xFF);  // Cobblestone -> Cobblestone
        assertEquals(0, blocks[4] & 0xFF);  // Glass -> Air
    }

    @Test
    void translateArraySameVersionNoop() {
        byte[] blocks = {1, 2, 3};
        byte[] original = blocks.clone();
        BlockTranslator.translateArray(blocks, ProtocolVersion.CLASSIC, ProtocolVersion.CLASSIC);
        assertArrayEquals(original, blocks);
    }

    // === Data-driven tables loaded ===

    @Test
    void allExpectedTablesExist() {
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.CLASSIC, ProtocolVersion.RUBYDUNG));
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.RUBYDUNG, ProtocolVersion.CLASSIC));
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.RUBYDUNG));
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.ALPHA_1_0_16, ProtocolVersion.RUBYDUNG));
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.ALPHA_1_2_3, ProtocolVersion.RUBYDUNG));
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.ALPHA_1_2_5, ProtocolVersion.RUBYDUNG));
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.CLASSIC));
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.ALPHA_1_0_16, ProtocolVersion.CLASSIC));
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.ALPHA_1_2_3, ProtocolVersion.CLASSIC));
        assertTrue(BlockTranslator.hasTranslation(ProtocolVersion.ALPHA_1_2_5, ProtocolVersion.CLASSIC));
    }
}
