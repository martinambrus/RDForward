package com.github.martinambrus.rdforward.protocol.packet;

import io.netty.buffer.ByteBuf;

/**
 * Sent when a block is placed or broken.
 *
 * Direction: both (client requests -> server validates -> server broadcasts)
 *
 * In RubyDung (v1): only x, y, z, blockId fields are used.
 * In Alpha (v2): blockMetadata is also used for block variants.
 *
 * Older clients reading this packet will simply stop at the fields
 * they know about — unknown trailing bytes are ignored. This is the
 * forward-compatibility mechanism.
 */
public class BlockChangePacket implements Packet {

    private int x;
    private int y;
    private int z;
    private int blockId;
    private int blockMetadata; // Alpha+ only, defaults to 0

    public BlockChangePacket() {
    }

    public BlockChangePacket(int x, int y, int z, int blockId) {
        this(x, y, z, blockId, 0);
    }

    public BlockChangePacket(int x, int y, int z, int blockId, int blockMetadata) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
        this.blockMetadata = blockMetadata;
    }

    @Override
    public PacketType getType() {
        return PacketType.BLOCK_CHANGE;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(blockId);
        buf.writeByte(blockMetadata);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        blockId = buf.readInt();
        if (buf.isReadable()) {
            blockMetadata = buf.readByte();
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockId() { return blockId; }
    public int getBlockMetadata() { return blockMetadata; }
}
