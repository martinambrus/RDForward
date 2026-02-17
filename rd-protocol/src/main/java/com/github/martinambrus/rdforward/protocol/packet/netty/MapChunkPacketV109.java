package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, S2C packet 0x20: Chunk Data.
 *
 * 1.9 changed primaryBitMask from ushort to VarInt, uses paletted section format.
 *
 * Wire format:
 *   [int]     chunkX
 *   [int]     chunkZ
 *   [boolean] groundUpContinuous
 *   [VarInt]  primaryBitMask
 *   [VarInt]  dataSize
 *   [byte[]]  data (dataSize bytes, uncompressed, paletted sections)
 *   [VarInt]  blockEntityCount (1.9.4/v110+ only; absent in v107-v109)
 *
 * In 1.9.0-1.9.2 (v107-v109), the packet ends after data — no block entity field.
 * In 1.9.4 (v110+), a VarInt block entity count + NBT compounds are appended.
 */
public class MapChunkPacketV109 implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean groundUpContinuous;
    private int primaryBitMask;
    private byte[] data;
    private boolean writeBlockEntityCount;

    public MapChunkPacketV109() {}

    public MapChunkPacketV109(int chunkX, int chunkZ, boolean groundUpContinuous,
                               int primaryBitMask, byte[] data,
                               boolean writeBlockEntityCount) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.groundUpContinuous = groundUpContinuous;
        this.primaryBitMask = primaryBitMask;
        this.data = data;
        this.writeBlockEntityCount = writeBlockEntityCount;
    }

    @Override
    public int getPacketId() { return 0x20; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(groundUpContinuous);
        McDataTypes.writeVarInt(buf, primaryBitMask);
        McDataTypes.writeVarInt(buf, data.length);
        buf.writeBytes(data);
        if (writeBlockEntityCount) {
            McDataTypes.writeVarInt(buf, 0); // block entity count (v110+)
        }
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        groundUpContinuous = buf.readBoolean();
        primaryBitMask = McDataTypes.readVarInt(buf);
        int dataSize = McDataTypes.readVarInt(buf);
        data = new byte[dataSize];
        buf.readBytes(data);
        // v110+ appends blockEntityCount + NBT; v107-v109 end here
        if (buf.readableBytes() > 0) {
            int blockEntityCount = McDataTypes.readVarInt(buf);
            // Skip block entity NBT
            for (int i = 0; i < blockEntityCount; i++) {
                // Each block entity is an NBT compound; skip it
                while (buf.readableBytes() > 0) {
                    byte t = buf.readByte();
                    if (t == 0) break;
                    buf.skipBytes(buf.readUnsignedShort()); // name
                }
            }
        }
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public int getPrimaryBitMask() { return primaryBitMask; }
    public byte[] getData() { return data; }
}
