package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x21: Chunk Data.
 *
 * 1.8 uses a new section format with ushort blockStates (blockId << 4 | meta),
 * no addBitMask, VarInt data size, and no zlib compression (raw data in packet,
 * packet-level compression is handled by the VarInt frame layer if enabled).
 *
 * Wire format:
 *   [int]     chunkX
 *   [int]     chunkZ
 *   [boolean] groundUpContinuous
 *   [ushort]  primaryBitMask
 *   [VarInt]  dataSize
 *   [byte[]]  data (dataSize bytes, uncompressed)
 */
public class MapChunkPacketV47 implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean groundUpContinuous;
    private int primaryBitMask;
    private byte[] data;

    public MapChunkPacketV47() {}

    public MapChunkPacketV47(int chunkX, int chunkZ, boolean groundUpContinuous,
                              int primaryBitMask, byte[] data) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.groundUpContinuous = groundUpContinuous;
        this.primaryBitMask = primaryBitMask;
        this.data = data;
    }

    @Override
    public int getPacketId() { return 0x21; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(groundUpContinuous);
        buf.writeShort(primaryBitMask);
        McDataTypes.writeVarInt(buf, data.length);
        buf.writeBytes(data);
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        groundUpContinuous = buf.readBoolean();
        primaryBitMask = buf.readUnsignedShort();
        int dataSize = McDataTypes.readVarInt(buf);
        data = new byte[dataSize];
        buf.readBytes(data);
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public int getPrimaryBitMask() { return primaryBitMask; }
    public byte[] getData() { return data; }
}
