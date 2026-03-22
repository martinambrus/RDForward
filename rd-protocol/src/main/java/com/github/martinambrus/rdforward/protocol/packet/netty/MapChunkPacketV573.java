package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.15 Play state, S2C packet 0x22: Chunk Data.
 *
 * Key difference from v477: biomes moved from inside the data array to a
 * separate int[1024] field between heightmaps NBT and dataSize.
 *
 * Wire format:
 *   [int]       chunkX
 *   [int]       chunkZ
 *   [boolean]   fullChunk
 *   [VarInt]    primaryBitMask
 *   [NBT]       heightmaps (TAG_Compound with MOTION_BLOCKING + WORLD_SURFACE)
 *   [int[1024]] biomes (if fullChunk) — 3D biome storage, 4096 bytes
 *   [VarInt]    dataSize
 *   [byte[]]    data (section data only, no biomes, no light)
 *   [VarInt]    blockEntityCount (0)
 */
public class MapChunkPacketV573 implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean fullChunk;
    private int primaryBitMask;
    private long[] motionBlockingHeightmap;
    private long[] worldSurfaceHeightmap;
    private int[] biomes;
    private byte[] data;

    public MapChunkPacketV573() {}

    public MapChunkPacketV573(int chunkX, int chunkZ, boolean fullChunk,
                               int primaryBitMask,
                               long[] motionBlockingHeightmap,
                               long[] worldSurfaceHeightmap,
                               int[] biomes,
                               byte[] data) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.fullChunk = fullChunk;
        this.primaryBitMask = primaryBitMask;
        this.motionBlockingHeightmap = motionBlockingHeightmap;
        this.worldSurfaceHeightmap = worldSurfaceHeightmap;
        this.biomes = biomes;
        this.data = data;
    }

    @Override
    public int getPacketId() { return 0x22; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(fullChunk);
        McDataTypes.writeVarInt(buf, primaryBitMask);

        // Write heightmaps as NBT compound
        writeHeightmapsNbt(buf);

        // Biomes: separate field in 1.15+ (was inside data array in 1.14)
        if (fullChunk && biomes != null) {
            for (int i = 0; i < biomes.length; i++) {
                buf.writeInt(biomes[i]);
            }
        }

        McDataTypes.writeVarInt(buf, data.length);
        buf.writeBytes(data);
        McDataTypes.writeVarInt(buf, 0); // blockEntityCount
    }

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

        // Skip NBT heightmaps
        McDataTypes.skipNbtRootTag(buf);

        // Read biomes (if fullChunk)
        if (fullChunk) {
            biomes = new int[1024];
            for (int i = 0; i < 1024; i++) {
                biomes[i] = buf.readInt();
            }
        }

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
