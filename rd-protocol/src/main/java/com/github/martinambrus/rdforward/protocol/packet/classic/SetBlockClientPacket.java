package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x05 (Client -> Server): Set Block.
 *
 * Sent when the player clicks to place or destroy a block.
 * The mode field distinguishes between destruction (0) and creation (1).
 * For destruction, blockType is the block that WAS there.
 * For creation, blockType is the block being placed.
 *
 * Wire format (8 bytes payload):
 *   [2 bytes] X coordinate (block position)
 *   [2 bytes] Y coordinate (block position)
 *   [2 bytes] Z coordinate (block position)
 *   [1 byte]  mode (0 = destroy, 1 = create)
 *   [1 byte]  block type ID
 */
public class SetBlockClientPacket implements Packet {

    public static final int MODE_DESTROY = 0;
    public static final int MODE_CREATE = 1;

    private int x;
    private int y;
    private int z;
    private int mode;
    private int blockType;

    public SetBlockClientPacket() {}

    public SetBlockClientPacket(int x, int y, int z, int mode, int blockType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.mode = mode;
        this.blockType = blockType;
    }

    @Override
    public int getPacketId() {
        return 0x05;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(x);
        buf.writeShort(y);
        buf.writeShort(z);
        buf.writeByte(mode);
        buf.writeByte(blockType);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readShort();
        y = buf.readShort();
        z = buf.readShort();
        mode = buf.readUnsignedByte();
        blockType = buf.readUnsignedByte();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getMode() { return mode; }
    public int getBlockType() { return blockType; }
}
