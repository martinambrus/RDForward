package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x33 (Server -> Client): Map Chunk.
 *
 * Sends compressed chunk data for a rectangular region of blocks.
 * A PreChunk (0x32) with mode=true must be sent first for the same
 * chunk column.
 *
 * The compressed data contains block IDs, then metadata nibbles, then
 * block light nibbles, then sky light nibbles — all zlib-compressed.
 *
 * Wire format:
 *   [int]    x (block coordinate of region origin)
 *   [short]  y (block coordinate, can be negative in theory)
 *   [int]    z (block coordinate of region origin)
 *   [byte]   sizeX (width - 1, so 0 = 1 block wide, 15 = 16 blocks)
 *   [byte]   sizeY (height - 1)
 *   [byte]   sizeZ (depth - 1)
 *   [int]    compressed data length
 *   [byte[]] compressed data (zlib/deflate)
 */
public class MapChunkPacket implements Packet {

    private int x;
    private short y;
    private int z;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private byte[] compressedData;

    public MapChunkPacket() {}

    public MapChunkPacket(int x, short y, int z, int sizeX, int sizeY, int sizeZ, byte[] compressedData) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.compressedData = compressedData;
    }

    @Override
    public int getPacketId() {
        return 0x33;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeShort(y);
        buf.writeInt(z);
        buf.writeByte(sizeX);
        buf.writeByte(sizeY);
        buf.writeByte(sizeZ);
        buf.writeInt(compressedData.length);
        buf.writeBytes(compressedData);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readShort();
        z = buf.readInt();
        sizeX = buf.readUnsignedByte();
        sizeY = buf.readUnsignedByte();
        sizeZ = buf.readUnsignedByte();
        int len = buf.readInt();
        compressedData = new byte[len];
        buf.readBytes(compressedData);
    }

    public int getX() { return x; }
    public short getY() { return y; }
    public int getZ() { return z; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }
    public byte[] getCompressedData() { return compressedData; }
}
