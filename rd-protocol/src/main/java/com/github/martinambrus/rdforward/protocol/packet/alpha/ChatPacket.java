package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x03 (bidirectional): Chat Message.
 *
 * Uses string16 encoding (vs Classic's 64-byte fixed ASCII).
 * Max 119 characters in Alpha.
 *
 * Wire format:
 *   [string16] message
 */
public class ChatPacket implements Packet {

    private String message;

    public ChatPacket() {}

    public ChatPacket(String message) {
        this.message = message;
    }

    @Override
    public int getPacketId() {
        return 0x03;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeString16(buf, message);
    }

    @Override
    public void read(ByteBuf buf) {
        message = McDataTypes.readString16(buf);
    }

    public String getMessage() { return message; }
}
