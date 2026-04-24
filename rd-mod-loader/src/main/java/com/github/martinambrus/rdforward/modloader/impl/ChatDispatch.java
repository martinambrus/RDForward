package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.ChatPacket;
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
        if (!v.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
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
        Packet p = buildChat(player, plainText);
        if (p != null) player.sendPacket(p);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
