package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.17 Play state, S2C packet 0x25: Update Light.
 *
 * Same as V735 but:
 * - All 4 masks are BitSets (VarInt longCount + long[]) instead of VarInt
 * - Sky light arrays prefixed with VarInt arrayCount
 * - Block light arrays prefixed with VarInt arrayCount
 *
 * Wire format:
 *   [VarInt]  chunkX
 *   [VarInt]  chunkZ
 *   [boolean] trustEdges
 *   [VarInt]  skyLightMask longCount + [long[]] skyLightMask
 *   [VarInt]  blockLightMask longCount + [long[]] blockLightMask
 *   [VarInt]  emptySkyLightMask longCount + [long[]] emptySkyLightMask
 *   [VarInt]  emptyBlockLightMask longCount + [long[]] emptyBlockLightMask
 *   [VarInt]  skyLightArrayCount
 *   For each sky light array:
 *     [VarInt]     length (always 2048)
 *     [byte[2048]] skyLight
 *   [VarInt]  blockLightArrayCount
 *   For each block light array:
 *     [VarInt]     length (always 2048)
 *     [byte[2048]] blockLight
 */
public class UpdateLightPacketV755 implements Packet {

    private int chunkX;
    private int chunkZ;
    private int skyLightMask;
    private int blockLightMask;
    private int emptySkyLightMask;
    private int emptyBlockLightMask;
    private byte[][] skyLightArrays;
    private byte[][] blockLightArrays;

    public UpdateLightPacketV755() {}

    public UpdateLightPacketV755(int chunkX, int chunkZ,
                                  int skyLightMask, int blockLightMask,
                                  int emptySkyLightMask, int emptyBlockLightMask,
                                  byte[][] skyLightArrays, byte[][] blockLightArrays) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.skyLightMask = skyLightMask;
        this.blockLightMask = blockLightMask;
        this.emptySkyLightMask = emptySkyLightMask;
        this.emptyBlockLightMask = emptyBlockLightMask;
        this.skyLightArrays = skyLightArrays;
        this.blockLightArrays = blockLightArrays;
    }

    @Override
    public int getPacketId() { return 0x25; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, chunkX);
        McDataTypes.writeVarInt(buf, chunkZ);
        buf.writeBoolean(true); // trustEdges

        // Write masks as BitSets (VarInt longCount + long[])
        writeBitSet(buf, skyLightMask);
        writeBitSet(buf, blockLightMask);
        writeBitSet(buf, emptySkyLightMask);
        writeBitSet(buf, emptyBlockLightMask);

        // Sky light arrays with count prefix
        int skyCount = skyLightArrays != null ? skyLightArrays.length : 0;
        McDataTypes.writeVarInt(buf, skyCount);
        if (skyLightArrays != null) {
            for (byte[] arr : skyLightArrays) {
                McDataTypes.writeVarInt(buf, arr.length);
                buf.writeBytes(arr);
            }
        }

        // Block light arrays with count prefix
        int blockCount = blockLightArrays != null ? blockLightArrays.length : 0;
        McDataTypes.writeVarInt(buf, blockCount);
        if (blockLightArrays != null) {
            for (byte[] arr : blockLightArrays) {
                McDataTypes.writeVarInt(buf, arr.length);
                buf.writeBytes(arr);
            }
        }
    }

    private static void writeBitSet(ByteBuf buf, int mask) {
        McDataTypes.writeVarInt(buf, 1); // 1 long is enough for 18-bit mask
        buf.writeLong((long) mask);
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = McDataTypes.readVarInt(buf);
        chunkZ = McDataTypes.readVarInt(buf);
        buf.readBoolean(); // trustEdges

        skyLightMask = readBitSetAsInt(buf);
        blockLightMask = readBitSetAsInt(buf);
        emptySkyLightMask = readBitSetAsInt(buf);
        emptyBlockLightMask = readBitSetAsInt(buf);

        int skyCount = McDataTypes.readVarInt(buf);
        skyLightArrays = new byte[skyCount][];
        for (int i = 0; i < skyCount; i++) {
            int len = McDataTypes.readVarInt(buf);
            skyLightArrays[i] = new byte[len];
            buf.readBytes(skyLightArrays[i]);
        }

        int blockCount = McDataTypes.readVarInt(buf);
        blockLightArrays = new byte[blockCount][];
        for (int i = 0; i < blockCount; i++) {
            int len = McDataTypes.readVarInt(buf);
            blockLightArrays[i] = new byte[len];
            buf.readBytes(blockLightArrays[i]);
        }
    }

    private static int readBitSetAsInt(ByteBuf buf) {
        int longCount = McDataTypes.readVarInt(buf);
        if (longCount == 0) return 0;
        int result = (int) buf.readLong();
        // Skip remaining longs if any
        for (int i = 1; i < longCount; i++) {
            buf.readLong();
        }
        return result;
    }
}
