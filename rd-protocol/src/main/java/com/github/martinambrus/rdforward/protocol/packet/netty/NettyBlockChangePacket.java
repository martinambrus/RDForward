package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, S2C packet 0x23: Block Change.
 *
 * Wire format:
 *   [int]    x
 *   [ubyte]  y
 *   [int]    z
 *   [VarInt] blockId
 *   [ubyte]  blockMetadata
 */
public class NettyBlockChangePacket implements Packet {

    private int x;
    private int y;
    private int z;
    private int blockId;
    private int metadata;

    public NettyBlockChangePacket() {}

    public NettyBlockChangePacket(int x, int y, int z, int blockId, int metadata) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
        this.metadata = metadata;
    }

    @Override
    public int getPacketId() { return 0x23; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        McDataTypes.writeVarInt(buf, blockId);
        buf.writeByte(metadata);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readUnsignedByte();
        z = buf.readInt();
        blockId = McDataTypes.readVarInt(buf);
        metadata = buf.readUnsignedByte();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockId() { return blockId; }
}
