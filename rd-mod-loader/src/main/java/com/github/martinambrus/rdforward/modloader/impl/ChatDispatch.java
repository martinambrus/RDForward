package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.ChatPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChatS2CPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChatS2CPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChatS2CPacketV735;
import com.github.martinambrus.rdforward.protocol.packet.netty.SystemChatPacketV759;
import com.github.martinambrus.rdforward.protocol.packet.netty.SystemChatPacketV760;
import com.github.martinambrus.rdforward.protocol.packet.netty.SystemChatPacketV765;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;

/**
 * Build a version-appropriate chat packet for the given recipient. Mirrors
 * the dispatch ladder used by the command reply sender in
 * {@code NettyConnectionHandler} so mod-initiated chat reaches every
 * supported client type.
 */
final class ChatDispatch {

    private ChatDispatch() {}

    static Packet buildChat(ConnectedPlayer player, String plainText) {
        ProtocolVersion v = player.getProtocolVersion();
        if (v == null) return new ChatPacket(plainText);
        if (v.isBedrock()) {
            // Bedrock chat uses its own codec path; mod-initiated broadcasts
            // to Bedrock clients are handled by the Bedrock bridge, not this helper.
            return null;
        }
        if (v.isClassicFormat()) {
            // RubyDung / Classic family use Classic 0x0D MessagePacket.
            // Sending Alpha ChatPacket (0x03) here would be decoded as
            // LevelDataChunk by the Classic client and the framing
            // mismatch closes the channel — i.e. typing any command
            // disconnects the player back to the title screen.
            return new MessagePacket(0, plainText);
        }
        if (!v.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
            // Pre-Netty (Alpha pipeline): the on-wire packet is still
            // ChatPacket (Alpha 0x03), but Release 1.6.1 introduced the
            // JSON-text-component payload — the 1.6.x client deserializes
            // every chat string via Gson and crashes with
            // "JsonPrimitive cannot be cast to JsonObject" if it gets a
            // raw §-coded string. Mirror the wrap that
            // ClassicToAlphaTranslator already does for MessagePacket.
            if (v.isAtLeast(ProtocolVersion.RELEASE_1_6_1)) {
                return new ChatPacket("{\"text\":\"" + escapeJson(plainText) + "\"}");
            }
            return new ChatPacket(plainText);
        }
        String json = "{\"text\":\"" + escapeJson(plainText) + "\"}";
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_20_3)) {
            return new SystemChatPacketV765(plainText, false);
        }
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_19_1)) {
            return new SystemChatPacketV760(json, false);
        }
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_19)) {
            return new SystemChatPacketV759(json, 0);
        }
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_16)) {
            return new NettyChatS2CPacketV735(json, (byte) 0, 0L, 0L);
        }
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            return new NettyChatS2CPacketV47(json, (byte) 0);
        }
        return new NettyChatS2CPacket(json);
    }

    static void send(ConnectedPlayer player, String plainText) {
        // Split long messages so pre-Netty clients (Beta 1.7.3 cap = 119
        // UTF-16 chars; 1.0–1.6.4 use the same packet shape) don't throw
        // "Received string length longer than maximum allowed" and drop
        // the connection. PlayerManager.splitChatMessage breaks at the
        // last full-stop within the limit, falling back to a hard cut.
        // Modern clients (1.7.2+) are unaffected by the split — they
        // receive a sequence of system chat lines instead of one.
        if (plainText == null) return;
        for (String chunk :
                com.github.martinambrus.rdforward.server.PlayerManager.splitChatMessage(plainText)) {
            Packet p = buildChat(player, chunk);
            if (p != null) player.sendPacket(p);
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
