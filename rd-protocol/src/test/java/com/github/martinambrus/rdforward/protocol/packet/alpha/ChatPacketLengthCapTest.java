package com.github.martinambrus.rdforward.protocol.packet.alpha;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pre-Netty MC chat strings (Alpha through pre-1.7.x Release) cap at
 * 119 chars; the client throws IOException and disconnects on longer
 * strings ("Received string length longer than maximum allowed
 * (N > 119)"). Caught a real disconnect for v22 admins after /lp
 * commands generated long replies that PlayerManager.splitChatMessage
 * didn't pre-split. The encoder now truncates as a defensive backstop.
 */
class ChatPacketLengthCapTest {

    @Test
    void writesShortMessageVerbatim() {
        String input = "hello world";
        ChatPacket out = roundTrip(input);
        assertEquals(input, out.getMessage());
    }

    @Test
    void writesExactCapVerbatim() {
        String exact = "x".repeat(ChatPacket.MAX_LEGACY_CHARS);
        ChatPacket out = roundTrip(exact);
        assertEquals(ChatPacket.MAX_LEGACY_CHARS, out.getMessage().length());
        assertEquals(exact, out.getMessage());
    }

    @Test
    void truncatesAtCapWhenInputExceedsCap() {
        String over = "y".repeat(ChatPacket.MAX_LEGACY_CHARS + 50);
        ChatPacket out = roundTrip(over);
        // Encoder must truncate at exactly MAX_LEGACY_CHARS so the
        // pre-Netty client decoder accepts the wire string.
        assertEquals(ChatPacket.MAX_LEGACY_CHARS, out.getMessage().length());
        assertTrue(over.startsWith(out.getMessage()),
                "truncated message must be a prefix of the input");
    }

    @Test
    void truncatesPreservesLeadingChars() {
        // First MAX chars are an identifiable prefix, the rest is junk
        // that we must drop.
        String prefix = "ABCDEFGH-prefix-marker-";
        String over = prefix + "z".repeat(ChatPacket.MAX_LEGACY_CHARS);
        ChatPacket out = roundTrip(over);
        assertEquals(ChatPacket.MAX_LEGACY_CHARS, out.getMessage().length());
        assertTrue(out.getMessage().startsWith(prefix.substring(0,
                Math.min(prefix.length(), ChatPacket.MAX_LEGACY_CHARS))));
    }

    private ChatPacket roundTrip(String message) {
        ChatPacket in = new ChatPacket(message);
        ByteBuf buf = Unpooled.buffer();
        try {
            in.write(buf);
            ChatPacket decoded = new ChatPacket();
            decoded.read(buf);
            assertEquals(0, buf.readableBytes(), "encoder/decoder must consume the same bytes");
            return decoded;
        } finally {
            buf.release();
        }
    }
}
