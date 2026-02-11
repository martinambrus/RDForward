package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x02 (Server -> Client): Level Initialize.
 *
 * Signals to the client that the server is about to send world data.
 * The client should prepare to receive LevelDataChunk packets.
 * No payload — just the packet ID byte.
 *
 * Wire format (0 bytes payload):
 *   (empty)
 */
public class LevelInitializePacket implements Packet {

    @Override
    public int getPacketId() {
        return 0x02;
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
