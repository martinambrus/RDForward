package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19 Play state, S2C packet 0x5F: System Chat Message.
 *
 * Replaces the old Chat Message packet for server-originated messages.
 * Player chat uses a separate PLAYER_CHAT packet (not implemented here).
 *
 * Wire format:
 *   [String] jsonMessage (chat component JSON)
 *   [VarInt] type (1 = system/chat, 2 = game_info/action_bar)
 */
public class SystemChatPacketV759 implements Packet {

    private String jsonMessage;
    private int type;

    public SystemChatPacketV759() {}

    public SystemChatPacketV759(String jsonMessage, int type) {
        this.jsonMessage = jsonMessage;
        this.type = type;
    }

    @Override
    public int getPacketId() { return 0x5F; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, jsonMessage);
        McDataTypes.writeVarInt(buf, type);
    }

    @Override
    public void read(ByteBuf buf) {
        jsonMessage = McDataTypes.readVarIntString(buf);
        type = McDataTypes.readVarInt(buf);
    }

    public String getJsonMessage() { return jsonMessage; }
}
