package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.16 Play state, S2C packet 0x0E: Chat Message.
 *
 * 1.16 added a UUID sender field after the position byte.
 *
 * Wire format:
 *   [String] jsonMessage
 *   [byte]   position (0=chat, 1=system, 2=hotbar)
 *   [UUID]   sender (16 bytes: long msb + long lsb)
 */
public class NettyChatS2CPacketV735 implements Packet {

    private String jsonMessage;
    private byte position;
    private long senderMsb;
    private long senderLsb;

    public NettyChatS2CPacketV735() {}

    public NettyChatS2CPacketV735(String jsonMessage, byte position, long senderMsb, long senderLsb) {
        this.jsonMessage = jsonMessage;
        this.position = position;
        this.senderMsb = senderMsb;
        this.senderLsb = senderLsb;
    }

    @Override
    public int getPacketId() { return 0x0E; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, jsonMessage);
        buf.writeByte(position);
        buf.writeLong(senderMsb);
        buf.writeLong(senderLsb);
    }

    @Override
    public void read(ByteBuf buf) {
        jsonMessage = McDataTypes.readVarIntString(buf);
        position = buf.readByte();
        senderMsb = buf.readLong();
        senderLsb = buf.readLong();
    }

    public String getJsonMessage() { return jsonMessage; }
}
