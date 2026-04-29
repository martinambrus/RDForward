package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.ChatPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChatS2CPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChatS2CPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChatS2CPacketV735;
import com.github.martinambrus.rdforward.protocol.packet.netty.SystemChatPacketV760;
import com.github.martinambrus.rdforward.protocol.packet.netty.SystemChatPacketV765;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins the ChatDispatch version dispatch ladder. The 1.6.x branch is the
 * load-bearing case: the 1.6 client deserializes every chat string via
 * Gson and crashes with "JsonPrimitive cannot be cast to JsonObject" if
 * the payload is a raw legacy §-coded string. Pre-1.6.1 clients keep
 * the legacy behavior.
 */
class ChatDispatchVersionWrappingTest {

    private static ConnectedPlayer player(ProtocolVersion v) {
        return new ConnectedPlayer((byte) 1, "tester", "00000000-0000-0000-0000-000000000001", null, v);
    }

    @Test
    void rubyDungUsesClassicMessagePacketNotAlphaChatPacket() {
        // Sending Alpha ChatPacket (0x03) to a Classic-family client is
        // decoded as LevelDataChunk by the client framer — typing any
        // command would disconnect the player back to the title screen.
        Packet p = ChatDispatch.buildChat(player(ProtocolVersion.RUBYDUNG), "hi");
        MessagePacket mp = assertInstanceOf(MessagePacket.class, p);
        assertEquals("hi", mp.getMessage());
    }

    @Test
    void classicUsesClassicMessagePacket() {
        Packet p = ChatDispatch.buildChat(player(ProtocolVersion.CLASSIC), "hi");
        assertInstanceOf(MessagePacket.class, p);
    }

    @Test
    void release16xWrapsInJsonOnAlphaPipeline() {
        Packet p = ChatDispatch.buildChat(player(ProtocolVersion.RELEASE_1_6_4),
                "§r§cWelcome, ZathrusW§c!");
        ChatPacket cp = assertInstanceOf(ChatPacket.class, p);
        assertEquals("{\"text\":\"§r§cWelcome, ZathrusW§c!\"}", cp.getMessage(),
                "1.6.4 client deserializes chat via Gson and requires a JSON object payload");
    }

    @Test
    void release161JustOverThresholdAlsoWraps() {
        Packet p = ChatDispatch.buildChat(player(ProtocolVersion.RELEASE_1_6_1), "hello");
        ChatPacket cp = assertInstanceOf(ChatPacket.class, p);
        assertEquals("{\"text\":\"hello\"}", cp.getMessage());
    }

    @Test
    void release152AndOlderKeepLegacyPlainString() {
        for (ProtocolVersion v : new ProtocolVersion[] {
                ProtocolVersion.RELEASE_1_5_2,
                ProtocolVersion.RELEASE_1_4_2,
                ProtocolVersion.BETA_1_8,
                ProtocolVersion.ALPHA_1_2_5}) {
            Packet p = ChatDispatch.buildChat(player(v), "hello");
            ChatPacket cp = assertInstanceOf(ChatPacket.class, p,
                    "expected ChatPacket for " + v);
            assertEquals("hello", cp.getMessage(),
                    "pre-1.6.1 clients still receive the legacy plain-string chat for " + v);
        }
    }

    @Test
    void nettyDispatchLadderUnchanged() {
        // Spot-check the modern branches still pick the right packet class
        // so the 1.6.x fix doesn't regress them.
        assertInstanceOf(NettyChatS2CPacket.class,
                ChatDispatch.buildChat(player(ProtocolVersion.RELEASE_1_7_2), "x"));
        assertInstanceOf(NettyChatS2CPacketV47.class,
                ChatDispatch.buildChat(player(ProtocolVersion.RELEASE_1_8), "x"));
        assertInstanceOf(NettyChatS2CPacketV735.class,
                ChatDispatch.buildChat(player(ProtocolVersion.RELEASE_1_16), "x"));
        assertInstanceOf(SystemChatPacketV760.class,
                ChatDispatch.buildChat(player(ProtocolVersion.RELEASE_1_19_1), "x"));
        assertInstanceOf(SystemChatPacketV765.class,
                ChatDispatch.buildChat(player(ProtocolVersion.RELEASE_1_20_3), "x"));
    }

    @Test
    void nullProtocolVersionFallsBackToPlainChatPacket() {
        Packet p = ChatDispatch.buildChat(player(null), "hello");
        ChatPacket cp = assertInstanceOf(ChatPacket.class, p);
        assertEquals("hello", cp.getMessage());
    }

    @Test
    void escapesQuotesAndBackslashesInJsonWrap() {
        Packet p = ChatDispatch.buildChat(player(ProtocolVersion.RELEASE_1_6_4),
                "say \"hi\" \\path");
        assertNotNull(p);
        ChatPacket cp = assertInstanceOf(ChatPacket.class, p);
        assertEquals("{\"text\":\"say \\\"hi\\\" \\\\path\"}", cp.getMessage(),
                "JSON escaping must round-trip quotes + backslashes so the client parses cleanly");
    }
}
