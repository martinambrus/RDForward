package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the exclusion overload of {@link PlayerManager#broadcastChat}.
 * Essentials's /ignore drives this through the bridge:
 * {@code ChatContext.excluded()} is populated from the chat event's
 * recipient diff, and the connection handler forwards that set here so
 * ignored players don't receive the message. This is the integration
 * test for the rd-server end of that pipeline (the bridge unit covers
 * the upstream wiring).
 */
class PlayerManagerBroadcastChatExclusionTest {

    @Test
    void excludedUsernamesReceiveNoPacket() {
        PlayerManager pm = new PlayerManager();
        EmbeddedChannel chAlice = new EmbeddedChannel();
        EmbeddedChannel chBob = new EmbeddedChannel();

        pm.addPlayer("Alice", null, chAlice, ProtocolVersion.RELEASE_1_2_4);
        pm.addPlayer("Bob", null, chBob, ProtocolVersion.RELEASE_1_2_4);

        pm.broadcastChat((byte) 0, "hello", Set.of("Bob"));

        assertEquals(1, chAlice.outboundMessages().size(),
                "non-excluded recipient must receive the chat packet");
        assertTrue(chAlice.outboundMessages().peek() instanceof MessagePacket);

        assertEquals(0, chBob.outboundMessages().size(),
                "excluded recipient must receive no chat packet");
    }

    @Test
    void emptyExclusionDeliversToEveryone() {
        PlayerManager pm = new PlayerManager();
        EmbeddedChannel ch1 = new EmbeddedChannel();
        EmbeddedChannel ch2 = new EmbeddedChannel();

        pm.addPlayer("Alice", null, ch1, ProtocolVersion.RELEASE_1_2_4);
        pm.addPlayer("Bob", null, ch2, ProtocolVersion.RELEASE_1_2_4);

        pm.broadcastChat((byte) 0, "hi", Set.of());

        assertEquals(1, ch1.outboundMessages().size());
        assertEquals(1, ch2.outboundMessages().size());
    }

    @Test
    void nullExclusionDeliversToEveryone() {
        // Defensive: existing 2-arg broadcastChat call sites pass null
        // (or invoke the no-exclusion overload). Pinning so we don't
        // regress to NPE on null.
        PlayerManager pm = new PlayerManager();
        EmbeddedChannel ch1 = new EmbeddedChannel();
        pm.addPlayer("Solo", null, ch1, ProtocolVersion.RELEASE_1_2_4);

        pm.broadcastChat((byte) 0, "hi", null);

        assertEquals(1, ch1.outboundMessages().size());
    }

    @Test
    void backCompatNoExclusionOverloadStillBroadcastsToAll() {
        // The 2-arg overload is the one every legacy call site uses
        // (kick announce, server-message bcast). It must keep behaving
        // exactly like the prior single broadcast loop.
        PlayerManager pm = new PlayerManager();
        EmbeddedChannel ch1 = new EmbeddedChannel();
        EmbeddedChannel ch2 = new EmbeddedChannel();

        pm.addPlayer("Alice", null, ch1, ProtocolVersion.RELEASE_1_2_4);
        pm.addPlayer("Bob", null, ch2, ProtocolVersion.RELEASE_1_2_4);

        pm.broadcastChat((byte) 0, "global");

        assertEquals(1, ch1.outboundMessages().size());
        assertEquals(1, ch2.outboundMessages().size());
    }

    @Test
    void multipleExcludedNamesAllSkip() {
        PlayerManager pm = new PlayerManager();
        EmbeddedChannel ch1 = new EmbeddedChannel();
        EmbeddedChannel ch2 = new EmbeddedChannel();
        EmbeddedChannel ch3 = new EmbeddedChannel();

        pm.addPlayer("Alice", null, ch1, ProtocolVersion.RELEASE_1_2_4);
        pm.addPlayer("Bob", null, ch2, ProtocolVersion.RELEASE_1_2_4);
        pm.addPlayer("Carol", null, ch3, ProtocolVersion.RELEASE_1_2_4);

        pm.broadcastChat((byte) 0, "hi", Set.of("Bob", "Carol"));

        assertEquals(1, ch1.outboundMessages().size());
        assertEquals(0, ch2.outboundMessages().size());
        assertEquals(0, ch3.outboundMessages().size());
    }

    @Test
    void longMessageSplitChunksAllReachNonExcluded() {
        // splitChatMessage shards long lines into multiple MessagePackets
        // — verify exclusion still applies per-chunk. Build a string long
        // enough to force at least two chunks (legacy chat cap is 64).
        PlayerManager pm = new PlayerManager();
        EmbeddedChannel chA = new EmbeddedChannel();
        EmbeddedChannel chB = new EmbeddedChannel();

        pm.addPlayer("Alice", null, chA, ProtocolVersion.RELEASE_1_2_4);
        pm.addPlayer("Bob", null, chB, ProtocolVersion.RELEASE_1_2_4);

        String longMsg = "x".repeat(150);
        pm.broadcastChat((byte) 0, longMsg, Set.of("Bob"));

        Queue<Object> aOut = chA.outboundMessages();
        assertTrue(aOut.size() >= 1, "Alice must receive at least one chunk");
        assertEquals(0, chB.outboundMessages().size(),
                "Bob excluded — no chunks regardless of split count");
    }
}
