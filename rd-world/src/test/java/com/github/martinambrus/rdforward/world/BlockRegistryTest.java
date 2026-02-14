package com.github.martinambrus.rdforward.world;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockRegistryTest {

    @Test
    void rubyDungOnlyAllowsThreeBlocks() {
        assertTrue(BlockRegistry.isValidBlock(BlockRegistry.AIR, ProtocolVersion.RUBYDUNG));
        assertTrue(BlockRegistry.isValidBlock(BlockRegistry.GRASS, ProtocolVersion.RUBYDUNG));
        assertTrue(BlockRegistry.isValidBlock(BlockRegistry.COBBLESTONE, ProtocolVersion.RUBYDUNG));
        assertFalse(BlockRegistry.isValidBlock(BlockRegistry.STONE, ProtocolVersion.RUBYDUNG));
        assertFalse(BlockRegistry.isValidBlock(BlockRegistry.DIRT, ProtocolVersion.RUBYDUNG));
        assertFalse(BlockRegistry.isValidBlock(50, ProtocolVersion.RUBYDUNG));
    }

    @Test
    void classicAllowsBlocksUpTo49() {
        assertTrue(BlockRegistry.isValidBlock(0, ProtocolVersion.CLASSIC));
        assertTrue(BlockRegistry.isValidBlock(49, ProtocolVersion.CLASSIC));
        assertFalse(BlockRegistry.isValidBlock(50, ProtocolVersion.CLASSIC));
        assertFalse(BlockRegistry.isValidBlock(-1, ProtocolVersion.CLASSIC));
    }

    @Test
    void allAlphaVersionsAllowBlocksUpTo91() {
        ProtocolVersion[] alphaVersions = {
                ProtocolVersion.ALPHA_1_0_15,
                ProtocolVersion.ALPHA_1_0_16,
                ProtocolVersion.ALPHA_1_2_0,
                ProtocolVersion.ALPHA_1_2_2,
                ProtocolVersion.ALPHA_1_2_3,
                ProtocolVersion.ALPHA_1_2_5
        };
        for (ProtocolVersion v : alphaVersions) {
            assertTrue(BlockRegistry.isValidBlock(0, v), v.name() + " should allow air");
            assertTrue(BlockRegistry.isValidBlock(91, v), v.name() + " should allow block 91");
            assertFalse(BlockRegistry.isValidBlock(92, v), v.name() + " should reject block 92");
            assertFalse(BlockRegistry.isValidBlock(-1, v), v.name() + " should reject block -1");
        }
    }

    @Test
    void bedrockAllowsAnyNonNegativeBlock() {
        assertTrue(BlockRegistry.isValidBlock(0, ProtocolVersion.BEDROCK));
        assertTrue(BlockRegistry.isValidBlock(91, ProtocolVersion.BEDROCK));
        assertTrue(BlockRegistry.isValidBlock(200, ProtocolVersion.BEDROCK));
    }

    @Test
    void knownBlocksHaveNames() {
        assertNotNull(BlockRegistry.getName(BlockRegistry.STONE));
        assertFalse(BlockRegistry.getName(BlockRegistry.STONE).startsWith("Unknown"));
        assertNotNull(BlockRegistry.getName(BlockRegistry.GRASS));
        assertFalse(BlockRegistry.getName(BlockRegistry.GRASS).startsWith("Unknown"));
    }

    @Test
    void unknownBlockIdReturnsUnknown() {
        assertTrue(BlockRegistry.getName(999).startsWith("Unknown"));
    }
}
