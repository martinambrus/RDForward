package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0F (Client -> Server): Player Block Placement.
 *
 * Sent when the player right-clicks to place a block or use an item.
 *
 * Direction values: 0=bottom(-Y), 1=top(+Y), 2=-Z, 3=+Z, 4=-X, 5=+X,
 *                   -1=special (use item without target block)
 *
 * Wire format (12 bytes payload, protocol versions 6-10):
 *   [short] block/item ID (-1 = empty hand)
 *   [int]   x (block position)
 *   [byte]  y (block position, 0-127)
 *   [int]   z (block position)
 *   [byte]  direction (face clicked, or -1)
 */
public class PlayerBlockPlacementPacket implements Packet {

    private int x;
    private int y;
    private int z;
    private int direction;
    private short itemId;

    public PlayerBlockPlacementPacket() {}

    public PlayerBlockPlacementPacket(int x, int y, int z, int direction, short itemId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.itemId = itemId;
    }

    @Override
    public int getPacketId() {
        return 0x0F;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(itemId);
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        buf.writeByte(direction);
    }

    @Override
    public void read(ByteBuf buf) {
        itemId = buf.readShort();
        x = buf.readInt();
        y = buf.readByte();
        z = buf.readInt();
        direction = buf.readByte();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getDirection() { return direction; }
    public short getItemId() { return itemId; }
}
