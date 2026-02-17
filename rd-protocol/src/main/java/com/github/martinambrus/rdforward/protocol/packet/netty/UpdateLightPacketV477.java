package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, S2C packet 0x24: Update Light.
 *
 * New in 1.14. Light data was removed from chunk sections and moved here.
 * Bitmasks use 18 bits: bit 0 = section -1 (below y=0), bits 1-16 = sections 0-15,
 * bit 17 = section 16 (above y=255).
 *
 * Wire format:
 *   [VarInt]  chunkX
 *   [VarInt]  chunkZ
 *   [VarInt]  skyLightMask (bitmask: which sections have sky light data)
 *   [VarInt]  blockLightMask (bitmask: which sections have block light data)
 *   [VarInt]  emptySkyLightMask (bitmask: sections with all-zero sky light)
 *   [VarInt]  emptyBlockLightMask (bitmask: sections with all-zero block light)
 *   For each set bit in skyLightMask (ascending order):
 *     [VarInt]     length (always 2048)
 *     [byte[2048]] skyLight
 *   For each set bit in blockLightMask (ascending order):
 *     [VarInt]     length (always 2048)
 *     [byte[2048]] blockLight
 */
public class UpdateLightPacketV477 implements Packet {

    private int chunkX;
    private int chunkZ;
    private int skyLightMask;
    private int blockLightMask;
    private int emptySkyLightMask;
    private int emptyBlockLightMask;
    private byte[][] skyLightArrays;
    private byte[][] blockLightArrays;

    public UpdateLightPacketV477() {}

    /**
     * Construct an UpdateLight packet from per-section light data.
     *
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     * @param skyLightMask bitmask of sections with sky light (bit 0 = section -1)
     * @param blockLightMask bitmask of sections with block light
     * @param emptySkyLightMask bitmask of sections with empty sky light
     * @param emptyBlockLightMask bitmask of sections with empty block light
     * @param skyLightArrays sky light arrays, one per set bit in skyLightMask (2048 bytes each)
     * @param blockLightArrays block light arrays, one per set bit in blockLightMask (2048 bytes each)
     */
    public UpdateLightPacketV477(int chunkX, int chunkZ,
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
    public int getPacketId() { return 0x24; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, chunkX);
        McDataTypes.writeVarInt(buf, chunkZ);
        McDataTypes.writeVarInt(buf, skyLightMask);
        McDataTypes.writeVarInt(buf, blockLightMask);
        McDataTypes.writeVarInt(buf, emptySkyLightMask);
        McDataTypes.writeVarInt(buf, emptyBlockLightMask);

        // Sky light arrays
        if (skyLightArrays != null) {
            for (byte[] arr : skyLightArrays) {
                McDataTypes.writeVarInt(buf, arr.length);
                buf.writeBytes(arr);
            }
        }

        // Block light arrays
        if (blockLightArrays != null) {
            for (byte[] arr : blockLightArrays) {
                McDataTypes.writeVarInt(buf, arr.length);
                buf.writeBytes(arr);
            }
        }
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = McDataTypes.readVarInt(buf);
        chunkZ = McDataTypes.readVarInt(buf);
        skyLightMask = McDataTypes.readVarInt(buf);
        blockLightMask = McDataTypes.readVarInt(buf);
        emptySkyLightMask = McDataTypes.readVarInt(buf);
        emptyBlockLightMask = McDataTypes.readVarInt(buf);

        int skyCount = Integer.bitCount(skyLightMask);
        skyLightArrays = new byte[skyCount][];
        for (int i = 0; i < skyCount; i++) {
            int len = McDataTypes.readVarInt(buf);
            skyLightArrays[i] = new byte[len];
            buf.readBytes(skyLightArrays[i]);
        }

        int blockCount = Integer.bitCount(blockLightMask);
        blockLightArrays = new byte[blockCount][];
        for (int i = 0; i < blockCount; i++) {
            int len = McDataTypes.readVarInt(buf);
            blockLightArrays[i] = new byte[len];
            buf.readBytes(blockLightArrays[i]);
        }
    }
}
