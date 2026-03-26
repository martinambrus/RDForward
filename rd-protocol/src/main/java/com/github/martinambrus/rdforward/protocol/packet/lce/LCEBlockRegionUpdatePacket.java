package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE BlockRegionUpdate packet (ID 51/0x33, S2C).
 *
 * Sends chunk block data with RLE+zlib compression.
 * This is LCE's equivalent of Java's MapChunkPacket.
 *
 * Wire format:
 *   [byte]  chunkFlags (bit 0 = fullChunk, bit 1 = zeroHeight)
 *   [int]   x (block coord)
 *   [short] y (block coord)
 *   [int]   z (block coord)
 *   [byte]  xs - 1 (size X minus 1)
 *   [byte]  ys - 1 (size Y minus 1)
 *   [byte]  zs - 1 (size Z minus 1)
 *   [int]   sizeAndLevel (bits 30-31 = dimensionIndex, bits 0-29 = compressed data size)
 *   [byte[]] compressedData
 */
public class LCEBlockRegionUpdatePacket implements Packet {

    private boolean fullChunk;
    private int x, z;
    private short y;
    private int xs, ys, zs;
    private int dimensionIndex;
    private byte[] compressedData;

    public LCEBlockRegionUpdatePacket() {}

    public LCEBlockRegionUpdatePacket(int x, short y, int z, int xs, int ys, int zs,
                                       boolean fullChunk, int dimensionIndex,
                                       byte[] compressedData) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xs = xs;
        this.ys = ys;
        this.zs = zs;
        this.fullChunk = fullChunk;
        this.dimensionIndex = dimensionIndex;
        this.compressedData = compressedData;
    }

    @Override
    public int getPacketId() { return 0x33; }

    @Override
    public void write(ByteBuf buf) {
        byte chunkFlags = 0;
        if (fullChunk) chunkFlags |= 0x01;
        if (ys == 0) chunkFlags |= 0x02;

        buf.writeByte(chunkFlags);
        buf.writeInt(x);
        buf.writeShort(y);
        buf.writeInt(z);
        buf.writeByte(xs - 1);
        buf.writeByte(ys - 1);
        buf.writeByte(zs - 1);

        int sizeAndLevel = (compressedData != null ? compressedData.length : 0);
        sizeAndLevel |= (dimensionIndex << 30);
        buf.writeInt(sizeAndLevel);
        if (compressedData != null && compressedData.length > 0) {
            buf.writeBytes(compressedData);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        byte chunkFlags = buf.readByte();
        fullChunk = (chunkFlags & 0x01) != 0;

        x = buf.readInt();
        y = buf.readShort();
        z = buf.readInt();
        xs = buf.readUnsignedByte() + 1;
        ys = buf.readUnsignedByte() + 1;
        zs = buf.readUnsignedByte() + 1;

        if ((chunkFlags & 0x02) != 0) ys = 0;

        int sizeAndLevel = buf.readInt();
        dimensionIndex = (sizeAndLevel >> 30) & 3;
        int size = sizeAndLevel & 0x3FFFFFFF;
        if (size > 0 && size <= 5 * 1024 * 1024) {
            compressedData = new byte[size];
            buf.readBytes(compressedData);
        }
    }
}
