package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Play state, S2C packet 0x0C: Chunk Batch Finished.
 *
 * Wire format:
 *   [VarInt] batchSize - number of chunks in this batch
 */
public class ChunkBatchFinishedPacket implements Packet {

    private int batchSize;

    public ChunkBatchFinishedPacket() {}

    public ChunkBatchFinishedPacket(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public int getPacketId() { return 0x0C; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, batchSize);
    }

    @Override
    public void read(ByteBuf buf) {
        batchSize = McDataTypes.readVarInt(buf);
    }
}
