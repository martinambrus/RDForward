package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0x33 (Server -> Client): Map Chunk (section-based).
 *
 * Same as V28 but WITHOUT the unused int(0) field.
 *
 * Wire format:
 *   [int]     chunk X (chunk coordinate, not block)
 *   [int]     chunk Z (chunk coordinate, not block)
 *   [boolean] ground-up continuous (true = full column including biome data)
 *   [short]   primary bit mask (bit set = section present)
 *   [short]   add bit mask (bit set = section has add data, always 0 for us)
 *   [int]     compressed data length
 *   [byte[]]  compressed data (zlib/deflate)
 */
public class MapChunkPacketV39 implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean groundUpContinuous;
    private short primaryBitMask;
    private short addBitMask;
    private byte[] compressedData;

    public MapChunkPacketV39() {}

    public MapChunkPacketV39(int chunkX, int chunkZ, boolean groundUpContinuous,
                              short primaryBitMask, short addBitMask, byte[] compressedData) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.groundUpContinuous = groundUpContinuous;
        this.primaryBitMask = primaryBitMask;
        this.addBitMask = addBitMask;
        this.compressedData = compressedData;
    }

    @Override
    public int getPacketId() {
        return 0x33;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(groundUpContinuous);
        buf.writeShort(primaryBitMask);
        buf.writeShort(addBitMask);
        buf.writeInt(compressedData.length);
        // No unused int(0) — removed in v39
        buf.writeBytes(compressedData);
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        groundUpContinuous = buf.readBoolean();
        primaryBitMask = buf.readShort();
        addBitMask = buf.readShort();
        int len = buf.readInt();
        compressedData = new byte[len];
        buf.readBytes(compressedData);
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public boolean isGroundUpContinuous() { return groundUpContinuous; }
    public short getPrimaryBitMask() { return primaryBitMask; }
    public short getAddBitMask() { return addBitMask; }
    public byte[] getCompressedData() { return compressedData; }
}
