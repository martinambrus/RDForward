package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Play state, S2C: Forget Level Chunk (Unload Chunk).
 *
 * Wire format changed from two ints (1.9-1.20.1) to a single packed long.
 * The long encodes the chunk position as:
 *   chunkX in the lower 32 bits, chunkZ in the upper 32 bits.
 *
 * Format:
 *   [long] chunkPos = (chunkX & 0xFFFFFFFFL) | ((long)chunkZ << 32)
 */
public class UnloadChunkPacketV764 implements Packet {

    private int chunkX;
    private int chunkZ;

    public UnloadChunkPacketV764() {}

    public UnloadChunkPacketV764(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public int getPacketId() { return 0x1F; }

    @Override
    public void write(ByteBuf buf) {
        long packed = ((long) chunkX & 0xFFFFFFFFL) | ((long) chunkZ << 32);
        buf.writeLong(packed);
    }

    @Override
    public void read(ByteBuf buf) {
        long packed = buf.readLong();
        chunkX = (int) packed;
        chunkZ = (int) (packed >> 32);
    }
}
