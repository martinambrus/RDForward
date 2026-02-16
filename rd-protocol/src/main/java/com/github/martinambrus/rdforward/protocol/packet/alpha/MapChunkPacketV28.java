package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.2.1+ protocol 0x33 (Server -> Client): Map Chunk (section-based).
 *
 * Release 1.2.1 (v28) overhauled the chunk format from a flat region-based
 * layout to section-based with 16x16x16 sub-chunks. Chunks are now 256 blocks
 * tall and include biome data.
 *
 * Wire format:
 *   [int]     chunk X (chunk coordinate, not block)
 *   [int]     chunk Z (chunk coordinate, not block)
 *   [boolean] ground-up continuous (true = full column including biome data)
 *   [short]   primary bit mask (bit set = section present)
 *   [short]   add bit mask (bit set = section has add data, always 0 for us)
 *   [int]     compressed data length
 *   [int]     unused (0)
 *   [byte[]]  compressed data (zlib/deflate)
 *
 * Compressed data layout (per section in primary bit mask, LSB first):
 *   4096 bytes block type LSBs (XZY ordering: x + z*16 + y*256... see below)
 *   Then for each section: 2048 bytes metadata nibbles
 *   Then for each section: 2048 bytes block light nibbles
 *   Then for each section: 2048 bytes sky light nibbles
 *   Then for each section in add bit mask: 2048 bytes block type MSBs (unused)
 *   If ground-up continuous: 256 bytes biome data
 *
 * Section ordering: blocks within a section use YZX ordering:
 *   index = (y & 15) << 8 | z << 4 | x
 */
public class MapChunkPacketV28 implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean groundUpContinuous;
    private short primaryBitMask;
    private short addBitMask;
    private byte[] compressedData;

    public MapChunkPacketV28() {}

    public MapChunkPacketV28(int chunkX, int chunkZ, boolean groundUpContinuous,
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
        buf.writeInt(0); // unused
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
        buf.readInt(); // unused
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
