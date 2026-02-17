package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, S2C packet 0x1D: Unload Chunk.
 *
 * New packet in 1.9. Replaces the MapChunk-with-bitmask-0 trick for chunk unloading.
 *
 * Wire format:
 *   [int] chunkX
 *   [int] chunkZ
 */
public class UnloadChunkPacketV109 implements Packet {

    private int chunkX;
    private int chunkZ;

    public UnloadChunkPacketV109() {}

    public UnloadChunkPacketV109(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public int getPacketId() { return 0x1D; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
    }
}
