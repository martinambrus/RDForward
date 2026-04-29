package com.github.martinambrus.rdforward.server.abilities;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies {@link AbilitiesDispatcherFactory} routes each protocol bucket
 * to the correct dispatcher, so a {@code setFlySpeed} call ends up
 * writing the codec-appropriate packet (or correctly no-ops on protocols
 * with no runtime fly-speed field).
 */
class AbilitiesDispatcherFactoryTest {

    private static ConnectedPlayer playerWithVersion(ProtocolVersion v) {
        return new ConnectedPlayer((byte) 1, "tester", "00000000-0000-0000-0000-000000000001", null, v);
    }

    @Test
    void release16AndLaterUseV73FloatDispatcher() {
        for (ProtocolVersion v : new ProtocolVersion[] {
                ProtocolVersion.RELEASE_1_6_1,
                ProtocolVersion.RELEASE_1_7_2,
                ProtocolVersion.RELEASE_1_8,
                ProtocolVersion.RELEASE_1_16,
                ProtocolVersion.RELEASE_1_21_5}) {
            assertSame(V73FloatAbilitiesDispatcher.INSTANCE,
                    AbilitiesDispatcherFactory.forSession(playerWithVersion(v)),
                    "expected V73Float dispatcher for " + v);
        }
    }

    @Test
    void release131To152UseV39ByteDispatcher() {
        for (ProtocolVersion v : new ProtocolVersion[] {
                ProtocolVersion.RELEASE_1_3_1,
                ProtocolVersion.RELEASE_1_4_2,
                ProtocolVersion.RELEASE_1_5,
                ProtocolVersion.RELEASE_1_5_2}) {
            assertSame(V39ByteAbilitiesDispatcher.INSTANCE,
                    AbilitiesDispatcherFactory.forSession(playerWithVersion(v)),
                    "expected V39Byte dispatcher for " + v);
        }
    }

    @Test
    void preReleaseAndClassicResolveToNoOp() {
        for (ProtocolVersion v : new ProtocolVersion[] {
                ProtocolVersion.RUBYDUNG,
                ProtocolVersion.CLASSIC,
                ProtocolVersion.ALPHA_1_2_5,
                ProtocolVersion.BETA_1_8,
                ProtocolVersion.RELEASE_1_2_4}) {
            assertSame(NoOpAbilitiesDispatcher.INSTANCE,
                    AbilitiesDispatcherFactory.forSession(playerWithVersion(v)),
                    "expected NoOp dispatcher for " + v);
        }
    }

    @Test
    void nullProtocolVersionResolvesToNoOp() {
        ConnectedPlayer p = new ConnectedPlayer(
                (byte) 1, "tester", "00000000-0000-0000-0000-000000000001", null, null);
        assertSame(NoOpAbilitiesDispatcher.INSTANCE,
                AbilitiesDispatcherFactory.forSession(p));
    }
}
