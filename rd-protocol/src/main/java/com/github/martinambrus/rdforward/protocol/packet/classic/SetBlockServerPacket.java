package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x06 (Server -> Client): Set Block.
 *
 * Sent by the server to update a single block in the world.
 * This is the server's authoritative block change notification.
 *
 * Wire format (7 bytes payload):
 *   [2 bytes] X coordinate (block position)
 *   [2 bytes] Y coordinate (block position)
 *   [2 bytes] Z coordinate (block position)
 *   [1 byte]  block type ID
 */
public class SetBlockServerPacket implements Packet {

    private int x;
    private int y;
    private int z;
    private int blockType;

    public SetBlockServerPacket() {}

    public SetBlockServerPacket(int x, int y, int z, int blockType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType;
    }

    @Override
    public int getPacketId() {
        return 0x06;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(x);
        buf.writeShort(y);
        buf.writeShort(z);
        buf.writeByte(blockType);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readShort();
        y = buf.readShort();
        z = buf.readShort();
        blockType = buf.readUnsignedByte();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockType() { return blockType; }
}
