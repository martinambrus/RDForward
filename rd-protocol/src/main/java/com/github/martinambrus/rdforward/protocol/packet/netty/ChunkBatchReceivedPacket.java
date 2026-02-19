package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Play state, C2S packet 0x07: Chunk Batch Received.
 *
 * Wire format:
 *   [Float] desiredChunksPerTick - read and discarded by the server
 */
public class ChunkBatchReceivedPacket implements Packet {

    @Override
    public int getPacketId() { return 0x07; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeFloat(0f);
    }

    @Override
    public void read(ByteBuf buf) {
        buf.readFloat(); // desiredChunksPerTick — discard
    }
}
