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
        // DAY_NIGHT_CYCLE was introduced in Alpha 1.2.6 (version 14), not 1.0.15 (version 10)
        assertFalse(Capability.DAY_NIGHT_CYCLE.isAvailableIn(ProtocolVersion.ALPHA_1_0_15));
    }

    @Test
    void allCapabilitiesAvailableInAlpha126() {
        for (Capability cap : Capability.values()) {
            assertTrue(cap.isAvailableIn(ProtocolVersion.ALPHA_1_2_6),
                    cap.name() + " should be available in Alpha 1.2.6");
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
    void eachCapabilityHasUniqueId() {
        Capability[] caps = Capability.values();
        for (int i = 0; i < caps.length; i++) {
            for (int j = i + 1; j < caps.length; j++) {
                assertNotEquals(caps[i].getId(), caps[j].getId(),
                        caps[i].name() + " and " + caps[j].name() + " share ID " + caps[i].getId());
            }
        }
    }
}
