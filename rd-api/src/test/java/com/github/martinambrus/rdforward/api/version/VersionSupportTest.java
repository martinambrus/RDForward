package com.github.martinambrus.rdforward.api.version;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionSupportTest {

    private static ProtocolVersion v(int sortOrder) {
        return new ProtocolVersion() {
            @Override public String name() { return "FAKE_" + sortOrder; }
            @Override public int getVersionNumber() { return sortOrder; }
            @Override public int getSortOrder() { return sortOrder; }
            @Override public String getDisplayName() { return "Fake " + sortOrder; }
            @Override public boolean isBedrock() { return false; }
            @Override public boolean isAtLeast(ProtocolVersion other) {
                return getSortOrder() >= other.getSortOrder();
            }
        };
    }

    @Test
    void nullCapabilityReturnsFalse() {
        assertFalse(VersionSupport.isSupported(null, v(10)));
    }

    @Test
    void nullVersionReturnsFalse() {
        assertFalse(VersionSupport.isSupported(VersionCapability.BLOCK_PLACEMENT, null));
    }

    @Test
    void alwaysSupportedCapabilityIsTrueForEveryVersion() {
        assertTrue(VersionSupport.isSupported(VersionCapability.BLOCK_PLACEMENT, v(0)));
        assertTrue(VersionSupport.isSupported(VersionCapability.CHAT, v(500)));
    }

    @Test
    void weatherGatedAtSortOrder11() {
        assertFalse(VersionSupport.isSupported(VersionCapability.WEATHER, v(10)));
        assertTrue(VersionSupport.isSupported(VersionCapability.WEATHER, v(11)));
        assertTrue(VersionSupport.isSupported(VersionCapability.WEATHER, v(12)));
    }

    @Test
    void tabListGatedAtSortOrder17() {
        assertFalse(VersionSupport.isSupported(VersionCapability.TAB_LIST, v(16)));
        assertTrue(VersionSupport.isSupported(VersionCapability.TAB_LIST, v(17)));
    }

    @Test
    void bossBarGatedAt1_9() {
        assertFalse(VersionSupport.isSupported(VersionCapability.BOSS_BAR, v(106)));
        assertTrue(VersionSupport.isSupported(VersionCapability.BOSS_BAR, v(107)));
    }

    @Test
    void customCapabilityRegistersAndEvaluates() {
        VersionCapability cap = VersionCapability.register(
                "test-mod:shiny", v -> v.getSortOrder() % 2 == 0);
        try {
            assertTrue(VersionSupport.isSupported(cap, v(4)));
            assertFalse(VersionSupport.isSupported(cap, v(3)));
        } finally {
            // Registry is static — leave the entry; duplicates throw on re-register,
            // but this test runs once per JVM.
        }
    }
}
