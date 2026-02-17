package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x02: Chat Message.
 *
 * 1.8 added a byte position field (0=chat, 1=system, 2=hotbar).
 *
 * Wire format:
 *   [String] jsonMessage
 *   [byte]   position
 */
public class NettyChatS2CPacketV47 implements Packet {

    private String jsonMessage;
    private byte position;

    public NettyChatS2CPacketV47() {}

    public NettyChatS2CPacketV47(String jsonMessage, byte position) {
        this.jsonMessage = jsonMessage;
        this.position = position;
    }

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, jsonMessage);
        buf.writeByte(position);
    }

    @Override
    public void read(ByteBuf buf) {
        jsonMessage = McDataTypes.readVarIntString(buf);
        position = buf.readByte();
    }

    public String getJsonMessage() { return jsonMessage; }
}
