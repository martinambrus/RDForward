package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, S2C packet 0x21: Chunk Data.
 *
 * Key differences from v393/v109:
 * 1. Heightmaps NBT compound added (MOTION_BLOCKING + WORLD_SURFACE)
 * 2. Section format: short blockCount before palette data, NO light data
 * 3. Light data moved to UpdateLightPacketV477
 *
 * Wire format:
 *   [int]     chunkX
 *   [int]     chunkZ
 *   [boolean] fullChunk
 *   [VarInt]  primaryBitMask
 *   [NBT]     heightmaps (TAG_Compound with MOTION_BLOCKING + WORLD_SURFACE)
 *   [VarInt]  dataSize
 *   [byte[]]  data (section data + biomes, no light)
 *   [VarInt]  blockEntityCount (0)
 */
public class MapChunkPacketV477 implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean fullChunk;
    private int primaryBitMask;
    private long[] motionBlockingHeightmap;
    private long[] worldSurfaceHeightmap;
    private byte[] data;

    public MapChunkPacketV477() {}

    public MapChunkPacketV477(int chunkX, int chunkZ, boolean fullChunk,
                               int primaryBitMask,
                               long[] motionBlockingHeightmap,
                               long[] worldSurfaceHeightmap,
                               byte[] data) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.fullChunk = fullChunk;
        this.primaryBitMask = primaryBitMask;
        this.motionBlockingHeightmap = motionBlockingHeightmap;
        this.worldSurfaceHeightmap = worldSurfaceHeightmap;
        this.data = data;
    }

    @Override
    public int getPacketId() { return 0x21; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(fullChunk);
        McDataTypes.writeVarInt(buf, primaryBitMask);

        // Write heightmaps as NBT compound
        writeHeightmapsNbt(buf);

        McDataTypes.writeVarInt(buf, data.length);
        buf.writeBytes(data);
        McDataTypes.writeVarInt(buf, 0); // blockEntityCount
    }

    /**
     * Write heightmaps as NBT: TAG_Compound with MOTION_BLOCKING and WORLD_SURFACE
     * long arrays. Uses raw NBT encoding (no external library needed).
     *
     * Each heightmap stores 256 values at 9 bits per entry, spanning-packed into longs.
     * 1.14's SimpleBitStorage still uses spanning packing (entries can cross long boundaries).
     */
    private void writeHeightmapsNbt(ByteBuf buf) {
        // TAG_Compound (type 0x0A) with empty name
        buf.writeByte(0x0A);
        buf.writeShort(0); // name length = 0

        // MOTION_BLOCKING: TAG_Long_Array (type 0x0C)
        writeLongArrayTag(buf, "MOTION_BLOCKING", motionBlockingHeightmap);

        // WORLD_SURFACE: TAG_Long_Array (type 0x0C)
        writeLongArrayTag(buf, "WORLD_SURFACE", worldSurfaceHeightmap);

        // TAG_End
        buf.writeByte(0x00);
    }

    private void writeLongArrayTag(ByteBuf buf, String name, long[] values) {
        buf.writeByte(0x0C); // TAG_Long_Array
        byte[] nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeInt(values.length);
        for (long v : values) {
            buf.writeLong(v);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        fullChunk = buf.readBoolean();
        primaryBitMask = McDataTypes.readVarInt(buf);

        // Skip NBT heightmaps (simplified: just skip the compound)
        McDataTypes.skipNbtRootTag(buf);

        int dataSize = McDataTypes.readVarInt(buf);
        data = new byte[dataSize];
        buf.readBytes(data);

        // Skip block entities
        int blockEntityCount = McDataTypes.readVarInt(buf);
        for (int i = 0; i < blockEntityCount; i++) {
            McDataTypes.skipNbtRootTag(buf);
        }
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public int getPrimaryBitMask() { return primaryBitMask; }
    public byte[] getData() { return data; }
}
