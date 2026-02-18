package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.16 Play state, S2C packet 0x24: Update Light.
 *
 * Same as V477 but with a boolean trustEdges field inserted after chunkZ.
 *
 * Wire format:
 *   [VarInt]  chunkX
 *   [VarInt]  chunkZ
 *   [boolean] trustEdges (NEW in 1.16, set to true)
 *   [VarInt]  skyLightMask
 *   [VarInt]  blockLightMask
 *   [VarInt]  emptySkyLightMask
 *   [VarInt]  emptyBlockLightMask
 *   For each set bit in skyLightMask:
 *     [VarInt]     length (always 2048)
 *     [byte[2048]] skyLight
 *   For each set bit in blockLightMask:
 *     [VarInt]     length (always 2048)
 *     [byte[2048]] blockLight
 */
public class UpdateLightPacketV735 implements Packet {

    private int chunkX;
    private int chunkZ;
    private int skyLightMask;
    private int blockLightMask;
    private int emptySkyLightMask;
    private int emptyBlockLightMask;
    private byte[][] skyLightArrays;
    private byte[][] blockLightArrays;

    public UpdateLightPacketV735() {}

    public UpdateLightPacketV735(int chunkX, int chunkZ,
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
        buf.writeBoolean(true); // trustEdges
        McDataTypes.writeVarInt(buf, skyLightMask);
        McDataTypes.writeVarInt(buf, blockLightMask);
        McDataTypes.writeVarInt(buf, emptySkyLightMask);
        McDataTypes.writeVarInt(buf, emptyBlockLightMask);

        if (skyLightArrays != null) {
            for (byte[] arr : skyLightArrays) {
                McDataTypes.writeVarInt(buf, arr.length);
                buf.writeBytes(arr);
            }
        }

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
        buf.readBoolean(); // trustEdges
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
