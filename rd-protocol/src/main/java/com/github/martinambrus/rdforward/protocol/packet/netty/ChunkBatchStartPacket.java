package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Play state, S2C packet 0x0D: Chunk Batch Start.
 *
 * Empty packet. Signals the beginning of a chunk batch.
 */
public class ChunkBatchStartPacket implements Packet {

    @Override
    public int getPacketId() { return 0x0D; }

    @Override
    public void write(ByteBuf buf) {
        // Empty packet
    }

    @Override
    public void read(ByteBuf buf) {
        // Empty packet
    }
}
