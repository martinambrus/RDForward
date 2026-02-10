package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x01 (Server -> Client): Ping.
 *
 * Sent periodically by the server to keep the connection alive.
 * No payload — just the packet ID byte.
 *
 * Wire format (0 bytes payload):
 *   (empty)
 */
public class PingPacket implements Packet {

    @Override
    public int getPacketId() {
        return 0x01;
    }

    @Override
    public void write(ByteBuf buf) {
        // No payload
    }

    @Override
    public void read(ByteBuf buf) {
        // No payload
    }
}
