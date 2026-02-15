package com.github.martinambrus.rdforward.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CapabilityTest {

    @Test
    void rubyDungCapabilitiesAvailableInRubyDung() {
        assertTrue(Capability.BLOCK_PLACE.isAvailableIn(ProtocolVersion.RUBYDUNG));
        assertTrue(Capability.BLOCK_BREAK.isAvailableIn(ProtocolVersion.RUBYDUNG));
        assertTrue(Capability.PLAYER_POSITION.isAvailableIn(ProtocolVersion.RUBYDUNG));
        assertTrue(Capability.CHUNK_DATA.isAvailableIn(ProtocolVersion.RUBYDUNG));
        assertTrue(Capability.CHAT_MESSAGE.isAvailableIn(ProtocolVersion.RUBYDUNG));
    }

    @Test
    void alphaCapabilitiesNotInRubyDung() {
        assertFalse(Capability.PLAYER_HEALTH.isAvailableIn(ProtocolVersion.RUBYDUNG));
        assertFalse(Capability.INVENTORY.isAvailableIn(ProtocolVersion.RUBYDUNG));
        assertFalse(Capability.ENTITY_SPAWN.isAvailableIn(ProtocolVersion.RUBYDUNG));
        assertFalse(Capability.BLOCK_METADATA.isAvailableIn(ProtocolVersion.RUBYDUNG));
        assertFalse(Capability.MINING_PROGRESS.isAvailableIn(ProtocolVersion.RUBYDUNG));
        assertFalse(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.RUBYDUNG));
    }

    @Test
    void alphaCapabilitiesNotInClassic() {
        assertFalse(Capability.PLAYER_HEALTH.isAvailableIn(ProtocolVersion.CLASSIC));
        assertFalse(Capability.ENTITY_SPAWN.isAvailableIn(ProtocolVersion.CLASSIC));
        assertFalse(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.CLASSIC));
    }

    @Test
    void alpha1015CapabilitiesAvailableInAlpha1015() {
        assertTrue(Capability.PLAYER_HEALTH.isAvailableIn(ProtocolVersion.ALPHA_1_0_15));
        assertTrue(Capability.INVENTORY.isAvailableIn(ProtocolVersion.ALPHA_1_0_15));
        assertTrue(Capability.ENTITY_SPAWN.isAvailableIn(ProtocolVersion.ALPHA_1_0_15));
        assertTrue(Capability.BLOCK_METADATA.isAvailableIn(ProtocolVersion.ALPHA_1_0_15));
        assertTrue(Capability.MINING_PROGRESS.isAvailableIn(ProtocolVersion.ALPHA_1_0_15));
    }

    @Test
    void dayNightCycleNotInAlpha1015() {
        // DAY_NIGHT_CYCLE was introduced in Alpha 1.2.x (v6), not 1.0.15 (v13)
        assertFalse(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.ALPHA_1_0_15));
    }

    @Test
    void dayNightCycleNotInAlpha1016() {
        // DAY_NIGHT_CYCLE was introduced in Alpha 1.2.3 (v5), not 1.0.16 (v14)
        assertFalse(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.ALPHA_1_0_16));
    }

    @Test
    void dayNightCycleNotInAlpha1017() {
        // DAY_NIGHT_CYCLE was introduced in Alpha 1.2.0 (v3), not 1.0.17 (v1)
        assertFalse(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.ALPHA_1_0_17));
    }

    @Test
    void dayNightCycleNotInAlpha110() {
        // DAY_NIGHT_CYCLE was introduced in Alpha 1.2.0 (v3), not 1.1.0 (v2)
        assertFalse(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.ALPHA_1_1_0));
    }

    @Test
    void alpha1017HasHealthAndInventory() {
        // v1 inherits capabilities from v13+ (health, inventory, entities, etc.)
        assertTrue(Capability.PLAYER_HEALTH.isAvailableIn(ProtocolVersion.ALPHA_1_0_17));
        assertTrue(Capability.INVENTORY.isAvailableIn(ProtocolVersion.ALPHA_1_0_17));
        assertTrue(Capability.ENTITY_SPAWN.isAvailableIn(ProtocolVersion.ALPHA_1_0_17));
        assertTrue(Capability.BLOCK_METADATA.isAvailableIn(ProtocolVersion.ALPHA_1_0_17));
        assertTrue(Capability.MINING_PROGRESS.isAvailableIn(ProtocolVersion.ALPHA_1_0_17));
    }

    @Test
    void alpha110HasHealthAndInventory() {
        // v2 inherits capabilities from v13+ (health, inventory, entities, etc.)
        assertTrue(Capability.PLAYER_HEALTH.isAvailableIn(ProtocolVersion.ALPHA_1_1_0));
        assertTrue(Capability.INVENTORY.isAvailableIn(ProtocolVersion.ALPHA_1_1_0));
        assertTrue(Capability.ENTITY_SPAWN.isAvailableIn(ProtocolVersion.ALPHA_1_1_0));
        assertTrue(Capability.BLOCK_METADATA.isAvailableIn(ProtocolVersion.ALPHA_1_1_0));
        assertTrue(Capability.MINING_PROGRESS.isAvailableIn(ProtocolVersion.ALPHA_1_1_0));
    }

    @Test
    void dayNightCycleAvailableInAlpha120() {
        // DAY_NIGHT_CYCLE was introduced in Alpha 1.2.0 (v3)
        assertTrue(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.ALPHA_1_2_0));
    }

    @Test
    void dayNightCycleAvailableInAlpha122() {
        // DAY_NIGHT_CYCLE was introduced in Alpha 1.2.0 (v3), so v4 should have it
        assertTrue(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.ALPHA_1_2_2));
    }

    @Test
    void dayNightCycleAvailableInAlpha123() {
        // DAY_NIGHT_CYCLE was introduced in Alpha 1.2.0 (v3), so v5 should have it
        assertTrue(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.ALPHA_1_2_3));
    }

    @Test
    void allCapabilitiesAvailableInAlpha120() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.ALPHA_1_2_0),
                    cap.name() + " should be available in Alpha 1.2.0 (v3)");
        }
    }

    @Test
    void allCapabilitiesAvailableInAlpha122() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.ALPHA_1_2_2),
                    cap.name() + " should be available in Alpha 1.2.2 (v4)");
        }
    }

    @Test
    void allCapabilitiesAvailableInAlpha123() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.ALPHA_1_2_3),
                    cap.name() + " should be available in Alpha 1.2.3 (v5)");
        }
    }

    @Test
    void allCapabilitiesAvailableInAlpha125() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.ALPHA_1_2_5),
                    cap.name() + " should be available in Alpha 1.2.x (v6)");
        }
    }

    @Test
    void allCapabilitiesAvailableInBeta10() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.BETA_1_0),
                    cap.name() + " should be available in Beta 1.0 (v7)");
        }
    }

    @Test
    void allCapabilitiesAvailableInBeta12() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.BETA_1_2),
                    cap.name() + " should be available in Beta 1.2 (v8)");
        }
    }

    @Test
    void allCapabilitiesAvailableInBeta13() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.BETA_1_3),
                    cap.name() + " should be available in Beta 1.3 (v9)");
        }
    }

    @Test
    void allCapabilitiesAvailableInBeta14() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.BETA_1_4),
                    cap.name() + " should be available in Beta 1.4 (v10)");
        }
    }

    @Test
    void allCapabilitiesAvailableInBeta15() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.BETA_1_5),
                    cap.name() + " should be available in Beta 1.5 (v11)");
        }
    }

    @Test
    void allCapabilitiesAvailableInBeta16() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.BETA_1_6),
                    cap.name() + " should be available in Beta 1.6 (v12)");
        }
    }

    @Test
    void allCapabilitiesAvailableInBeta17() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.BETA_1_7),
                    cap.name() + " should be available in Beta 1.7 (v13)");
        }
    }

    @Test
    void allCapabilitiesAvailableInBeta173() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.BETA_1_7_3),
                    cap.name() + " should be available in Beta 1.7.3 (v14)");
        }
    }

    @Test
    void allCapabilitiesAvailableInBedrock() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.BEDROCK),
                    cap.name() + " should be available in Bedrock");
        }
    }

    @Test
    void rubyDungCapabilitiesInheritedByLaterVersions() {
        // RubyDung capabilities should be available in all versions
        for (ProtocolVersion version : ProtocolVersion.values()) {
            assertTrue(Capability.BLOCK_PLACE.isAvailableIn(version),
                    "BLOCK_PLACE should be available in " + version.name());
            assertTrue(Capability.CHAT_MESSAGE.isAvailableIn(version),
                    "CHAT_MESSAGE should be available in " + version.name());
        }
    }

    @Test
    void fromNumberWithFamilyDisambiguatesV7() {
        // v7 is shared by Classic and Beta 1.0 — family filter picks the right one
        assertEquals(ProtocolVersion.CLASSIC,
                ProtocolVersion.fromNumber(7, ProtocolVersion.Family.CLASSIC));
        assertEquals(ProtocolVersion.BETA_1_0,
                ProtocolVersion.fromNumber(7, ProtocolVersion.Family.BETA));
        assertEquals(ProtocolVersion.BETA_1_0,
                ProtocolVersion.fromNumber(7, ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA));
        assertNull(ProtocolVersion.fromNumber(7, ProtocolVersion.Family.ALPHA));
    }

    @Test
    void fromNumberResolvesV9ToBeta13() {
        // v9 is Beta 1.3 — resolves correctly with BETA family filter
        assertEquals(ProtocolVersion.BETA_1_3,
                ProtocolVersion.fromNumber(9, ProtocolVersion.Family.BETA));
        assertEquals(ProtocolVersion.BETA_1_3,
                ProtocolVersion.fromNumber(9, ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA));
        // No Alpha version uses protocol number 9
        assertNull(ProtocolVersion.fromNumber(9, ProtocolVersion.Family.ALPHA));
    }

    @Test
    void fromNumberResolvesV10ToBeta14() {
        // v10 is Beta 1.4 — resolves correctly with BETA family filter
        assertEquals(ProtocolVersion.BETA_1_4,
                ProtocolVersion.fromNumber(10, ProtocolVersion.Family.BETA));
        assertEquals(ProtocolVersion.BETA_1_4,
                ProtocolVersion.fromNumber(10, ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA));
        // No Alpha version uses protocol number 10 (pre-rewrite v10 is not in our enum)
        assertNull(ProtocolVersion.fromNumber(10, ProtocolVersion.Family.ALPHA));
    }

    @Test
    void fromNumberResolvesV11ToBeta15() {
        // v11 is Beta 1.5 — resolves correctly with BETA family filter
        assertEquals(ProtocolVersion.BETA_1_5,
                ProtocolVersion.fromNumber(11, ProtocolVersion.Family.BETA));
        assertEquals(ProtocolVersion.BETA_1_5,
                ProtocolVersion.fromNumber(11, ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA));
        // No Alpha version uses protocol number 11
        assertNull(ProtocolVersion.fromNumber(11, ProtocolVersion.Family.ALPHA));
    }

    @Test
    void fromNumberResolvesV12ToBeta16() {
        // v12 is Beta 1.6 — resolves correctly with BETA family filter
        assertEquals(ProtocolVersion.BETA_1_6,
                ProtocolVersion.fromNumber(12, ProtocolVersion.Family.BETA));
        assertEquals(ProtocolVersion.BETA_1_6,
                ProtocolVersion.fromNumber(12, ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA));
        // No Alpha version uses protocol number 12
        assertNull(ProtocolVersion.fromNumber(12, ProtocolVersion.Family.ALPHA));
    }

    @Test
    void fromNumberResolvesV13WithFamilyFilter() {
        // v13 is shared by Alpha 1.0.15 and Beta 1.7 — family filter disambiguates
        assertEquals(ProtocolVersion.ALPHA_1_0_15,
                ProtocolVersion.fromNumber(13, ProtocolVersion.Family.ALPHA));
        assertEquals(ProtocolVersion.BETA_1_7,
                ProtocolVersion.fromNumber(13, ProtocolVersion.Family.BETA));
        // With both families, Alpha is returned first (enum order)
        assertEquals(ProtocolVersion.ALPHA_1_0_15,
                ProtocolVersion.fromNumber(13, ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA));
    }

    @Test
    void fromNumberResolvesV14WithFamilyFilter() {
        // v14 is shared by Alpha 1.0.16 and Beta 1.7.3 — family filter disambiguates
        assertEquals(ProtocolVersion.ALPHA_1_0_16,
                ProtocolVersion.fromNumber(14, ProtocolVersion.Family.ALPHA));
        assertEquals(ProtocolVersion.BETA_1_7_3,
                ProtocolVersion.fromNumber(14, ProtocolVersion.Family.BETA));
        // With both families, Alpha is returned first (enum order)
        assertEquals(ProtocolVersion.ALPHA_1_0_16,
                ProtocolVersion.fromNumber(14, ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA));
    }

    @Test
    void fromNumberResolvesV8ToBeta12() {
        // v8 is Beta 1.2 — resolves correctly with BETA family filter
        assertEquals(ProtocolVersion.BETA_1_2,
                ProtocolVersion.fromNumber(8, ProtocolVersion.Family.BETA));
        assertEquals(ProtocolVersion.BETA_1_2,
                ProtocolVersion.fromNumber(8, ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA));
        // No Alpha version uses protocol number 8
        assertNull(ProtocolVersion.fromNumber(8, ProtocolVersion.Family.ALPHA));
    }

    @Test
    void eachCapabilityHasUniqueId() {
        Capability[] caps = Capability.values();
        for (int i = 0; i < caps.length; i++) {
            for (int j = i + 1; j < caps.length; j++) {
                assertNotEquals(caps[i].getId(), caps[j].getId(),
                        caps[i].name() + " and " + caps[j].name() + " share ID " + caps[i].getId());
            }
        }
    }

    @Test
    void sortOrderIsChronological() {
        // Verify that sortOrder reflects chronological order, not protocol number
        assertTrue(ProtocolVersion.RUBYDUNG.isAtLeast(ProtocolVersion.RUBYDUNG));
        assertTrue(ProtocolVersion.CLASSIC.isAtLeast(ProtocolVersion.RUBYDUNG));
        assertTrue(ProtocolVersion.ALPHA_1_0_15.isAtLeast(ProtocolVersion.CLASSIC));
        assertTrue(ProtocolVersion.ALPHA_1_0_16.isAtLeast(ProtocolVersion.ALPHA_1_0_15));
        assertTrue(ProtocolVersion.ALPHA_1_0_17.isAtLeast(ProtocolVersion.ALPHA_1_0_16));
        assertTrue(ProtocolVersion.ALPHA_1_1_0.isAtLeast(ProtocolVersion.ALPHA_1_0_17));
        assertTrue(ProtocolVersion.ALPHA_1_2_0.isAtLeast(ProtocolVersion.ALPHA_1_1_0));
        assertTrue(ProtocolVersion.ALPHA_1_2_2.isAtLeast(ProtocolVersion.ALPHA_1_2_0));
        assertTrue(ProtocolVersion.ALPHA_1_2_3.isAtLeast(ProtocolVersion.ALPHA_1_2_2));
        assertTrue(ProtocolVersion.ALPHA_1_2_5.isAtLeast(ProtocolVersion.ALPHA_1_2_3));
        assertTrue(ProtocolVersion.BETA_1_0.isAtLeast(ProtocolVersion.ALPHA_1_2_5));
        assertTrue(ProtocolVersion.BETA_1_2.isAtLeast(ProtocolVersion.BETA_1_0));
        assertTrue(ProtocolVersion.BETA_1_3.isAtLeast(ProtocolVersion.BETA_1_2));
        assertTrue(ProtocolVersion.BETA_1_4.isAtLeast(ProtocolVersion.BETA_1_3));
        assertTrue(ProtocolVersion.BETA_1_5.isAtLeast(ProtocolVersion.BETA_1_4));
        assertTrue(ProtocolVersion.BETA_1_6.isAtLeast(ProtocolVersion.BETA_1_5));
        assertTrue(ProtocolVersion.BETA_1_7.isAtLeast(ProtocolVersion.BETA_1_6));
        assertTrue(ProtocolVersion.BETA_1_7_3.isAtLeast(ProtocolVersion.BETA_1_7));
        assertTrue(ProtocolVersion.BEDROCK.isAtLeast(ProtocolVersion.BETA_1_7_3));

        // v6 is chronologically AFTER v14 (post-rewrite), even though 6 < 14
        assertTrue(ProtocolVersion.ALPHA_1_2_5.isAtLeast(ProtocolVersion.ALPHA_1_0_16));
        assertFalse(ProtocolVersion.ALPHA_1_0_16.isAtLeast(ProtocolVersion.ALPHA_1_2_5));
    }
}
