package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0F (Client -> Server): Player Block Placement.
 *
 * Sent when the player right-clicks to place a block or use an item.
 * In Alpha 1.2.6 (protocol 14), includes item stack data when item ID >= 0.
 *
 * Direction values: 0=bottom(-Y), 1=top(+Y), 2=-Z, 3=+Z, 4=-X, 5=+X,
 *                   -1=special (use item without target block)
 *
 * Wire format:
 *   [int]   x (block position)
 *   [byte]  y (block position, 0-127)
 *   [int]   z (block position)
 *   [byte]  direction (face clicked, or -1)
 *   [short] block/item ID (-1 = empty hand)
 *   If block/item ID >= 0:
 *     [byte]  amount (stack size)
 *     [short] damage/metadata
 */
public class PlayerBlockPlacementPacket implements Packet {

    private int x;
    private int y;
    private int z;
    private int direction;
    private short itemId;
    private byte amount;
    private short damage;

    public PlayerBlockPlacementPacket() {}

    public PlayerBlockPlacementPacket(int x, int y, int z, int direction,
                                      short itemId, byte amount, short damage) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.itemId = itemId;
        this.amount = amount;
        this.damage = damage;
    }

    @Override
    public int getPacketId() {
        return 0x0F;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        buf.writeByte(direction);
        buf.writeShort(itemId);
        if (itemId >= 0) {
            buf.writeByte(amount);
            buf.writeShort(damage);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readByte();
        z = buf.readInt();
        direction = buf.readByte();
        itemId = buf.readShort();
        if (itemId >= 0) {
            amount = buf.readByte();
            damage = buf.readShort();
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getDirection() { return direction; }
    public short getItemId() { return itemId; }
    public byte getAmount() { return amount; }
    public short getDamage() { return damage; }
}
