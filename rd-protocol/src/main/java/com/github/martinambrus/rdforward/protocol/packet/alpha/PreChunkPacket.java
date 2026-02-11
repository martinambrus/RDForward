package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x32 (Server -> Client): Pre-Chunk.
 *
 * Sent before MapChunk to tell the client to allocate/deallocate
 * a 16x128x16 chunk column. Must be sent with mode=true before
 * sending the corresponding MapChunk data.
 *
 * Wire format (9 bytes payload):
 *   [int]     chunk X (chunk coordinates, not block)
 *   [int]     chunk Z (chunk coordinates, not block)
 *   [boolean] mode (true = load/initialize, false = unload)
 */
public class PreChunkPacket implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean load;

    public PreChunkPacket() {}

    public PreChunkPacket(int chunkX, int chunkZ, boolean load) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.load = load;
    }

    @Override
    public int getPacketId() {
        return 0x32;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(load);
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        load = buf.readBoolean();
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public boolean isLoad() { return load; }
}
