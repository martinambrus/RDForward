package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockReplacementRegistryTest {

    @BeforeEach
    @AfterEach
    void wipeCaches() {
        BlockReplacementRegistry.resetForTests();
    }

    @Test
    void shippedB173FileLoadsAndReturnsKnownReplacement() {
        // The b1.7.3.yml resource shipped in rd-server has piston ->
        // cobblestone. Confirm the lazy load picks it up.
        assertEquals("minecraft:cobblestone",
                BlockReplacementRegistry.getReplacement(
                        ProtocolVersion.BETA_1_7_3, "minecraft:piston"));
    }

    @Test
    void missingResourceFileReturnsEmpty() {
        // RUBYDUNG has no shipped file; getReplacement returns null,
        // and a second call should not throw or repeat the missing-file
        // load (cached in NO_FILE).
        assertNull(BlockReplacementRegistry.getReplacement(
                ProtocolVersion.RUBYDUNG, "minecraft:anything"));
        assertNull(BlockReplacementRegistry.getReplacement(
                ProtocolVersion.RUBYDUNG, "minecraft:anything"));
    }

    @Test
    void introducedAfterIsTrueForBlockFromNewerVersionFile() {
        // piston introduced in b1.7.3; ALPHA_1_2_5 has lower sortOrder.
        assertTrue(BlockReplacementRegistry.introducedAfter(
                "minecraft:piston", ProtocolVersion.ALPHA_1_2_5));
    }

    @Test
    void introducedAfterIsFalseForBlockNotInAnyFile() {
        // No file mentions stone — heuristic treats as "always there".
        assertFalse(BlockReplacementRegistry.introducedAfter(
                "minecraft:stone", ProtocolVersion.ALPHA_1_2_5));
    }

    @Test
    void introducedAfterIsFalseWhenTargetMatchesIntroducingVersion() {
        // piston introduced in b1.7.3 — for a target of BETA_1_7_3
        // itself, introducedAfter must be false (block is in vocab).
        assertFalse(BlockReplacementRegistry.introducedAfter(
                "minecraft:piston", ProtocolVersion.BETA_1_7_3));
    }

    @Test
    void nullsAreSafe() {
        assertNull(BlockReplacementRegistry.getReplacement(null, "x"));
        assertNull(BlockReplacementRegistry.getReplacement(ProtocolVersion.BETA_1_7_3, null));
        assertFalse(BlockReplacementRegistry.introducedAfter(null, ProtocolVersion.BETA_1_7_3));
        assertFalse(BlockReplacementRegistry.introducedAfter("minecraft:piston", null));
    }
}
