package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x35 (Server -> Client): Block Change.
 *
 * Sent when a single block changes. Uses int X/Z coordinates (vs Classic's
 * short coords) and includes block metadata.
 *
 * Wire format (11 bytes payload):
 *   [int]  x (block position)
 *   [byte] y (block position, 0-127)
 *   [int]  z (block position)
 *   [byte] block type ID
 *   [byte] block metadata
 */
public class BlockChangePacket implements Packet {

    private int x;
    private int y;
    private int z;
    private int blockType;
    private int metadata;

    public BlockChangePacket() {}

    public BlockChangePacket(int x, int y, int z, int blockType, int metadata) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType;
        this.metadata = metadata;
    }

    @Override
    public int getPacketId() {
        return 0x35;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        buf.writeByte(blockType);
        buf.writeByte(metadata);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readByte();
        z = buf.readInt();
        blockType = buf.readUnsignedByte();
        metadata = buf.readUnsignedByte();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockType() { return blockType; }
    public int getMetadata() { return metadata; }
}
