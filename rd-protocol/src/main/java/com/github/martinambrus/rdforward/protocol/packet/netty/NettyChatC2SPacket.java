package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x01: Chat Message.
 *
 * Wire format: [String] message (VarInt + UTF-8)
 */
public class NettyChatC2SPacket implements Packet {

    private String message;

    public NettyChatC2SPacket() {}

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, message);
    }

    @Override
    public void read(ByteBuf buf) {
        message = McDataTypes.readVarIntString(buf);
    }

    public String getMessage() { return message; }
}
