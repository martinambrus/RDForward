package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0x35 (Server -> Client): Block Change.
 *
 * Same as pre-v39 BlockChangePacket but block ID widened from byte to short.
 *
 * Wire format (12 bytes payload):
 *   [int]   x (block position)
 *   [byte]  y (block position, 0-255)
 *   [int]   z (block position)
 *   [short] block type ID
 *   [byte]  block metadata
 */
public class BlockChangePacketV39 implements Packet {

    private int x;
    private int y;
    private int z;
    private int blockType;
    private int metadata;

    public BlockChangePacketV39() {}

    public BlockChangePacketV39(int x, int y, int z, int blockType, int metadata) {
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
        buf.writeShort(blockType);
        buf.writeByte(metadata);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readByte();
        z = buf.readInt();
        blockType = buf.readUnsignedShort();
        metadata = buf.readUnsignedByte();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockType() { return blockType; }
    public int getMetadata() { return metadata; }
}
