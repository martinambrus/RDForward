package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x00 (bidirectional): Keep Alive.
 *
 * Empty packet — just the packet ID byte, no payload.
 * In Alpha this replaces Classic's 0x01 Ping.
 *
 * Wire format (0 bytes payload):
 *   (empty)
 */
public class KeepAlivePacket implements Packet {

    public KeepAlivePacket() {}

    @Override
    public int getPacketId() {
        return 0x00;
    }

    @Override
    public void write(ByteBuf buf) {
        // empty
    }

    @Override
    public void read(ByteBuf buf) {
        // empty
    }
}
