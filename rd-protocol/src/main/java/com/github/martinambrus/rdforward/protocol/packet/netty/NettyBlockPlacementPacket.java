package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.BlockPlacementData;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x08: Block Placement.
 *
 * Wire format:
 *   [int]   x
 *   [ubyte] y
 *   [int]   z
 *   [byte]  direction
 *   [slot]  held item (Netty slot data)
 *   [byte]  cursorX
 *   [byte]  cursorY
 *   [byte]  cursorZ
 */
public class NettyBlockPlacementPacket implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int direction;
    private short heldItemId;
    private byte cursorX;
    private byte cursorY;
    private byte cursorZ;

    public NettyBlockPlacementPacket() {}

    public NettyBlockPlacementPacket(int x, int y, int z, int direction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.heldItemId = -1;
    }

    public NettyBlockPlacementPacket(int x, int y, int z, int direction, short heldItemId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.heldItemId = heldItemId;
    }

    @Override
    public int getPacketId() { return 0x08; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        buf.writeByte(direction);
        if (heldItemId >= 0) {
            McDataTypes.writeNettySlotItem(buf, heldItemId, 64, 0);
        } else {
            McDataTypes.writeEmptyNettySlot(buf);
        }
        buf.writeByte(cursorX);
        buf.writeByte(cursorY);
        buf.writeByte(cursorZ);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readUnsignedByte();
        z = buf.readInt();
        direction = buf.readByte();
        // Read item ID from slot, then skip the rest of the slot data.
        // NBT format: short nbtLength (-1 = none, >= 0 = that many bytes of gzipped NBT).
        heldItemId = buf.readShort();
        if (heldItemId >= 0) {
            buf.skipBytes(1); // count
            buf.skipBytes(2); // damage
            short nbtLength = buf.readShort();
            if (nbtLength > 0) {
                buf.skipBytes(nbtLength);
            }
        }
        cursorX = buf.readByte();
        cursorY = buf.readByte();
        cursorZ = buf.readByte();
    }

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }
    @Override
    public int getZ() { return z; }
    @Override
    public int getDirection() { return direction; }
    @Override
    public short getItemId() { return heldItemId; }
}
