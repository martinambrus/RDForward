package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, S2C packet 0x02: Chat Message.
 *
 * Wire format: [String] jsonMessage
 */
public class NettyChatS2CPacket implements Packet {

    private String jsonMessage;

    public NettyChatS2CPacket() {}

    public NettyChatS2CPacket(String jsonMessage) {
        this.jsonMessage = jsonMessage;
    }

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, jsonMessage);
    }

    @Override
    public void read(ByteBuf buf) {
        jsonMessage = McDataTypes.readVarIntString(buf);
    }

    public String getJsonMessage() { return jsonMessage; }
}
