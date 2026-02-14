package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0F (Client -> Server): Player Block Placement.
 *
 * V14 format (protocol versions 10-14, pre-rewrite SMP):
 *   [int]   x (block position)
 *   [byte]  y (block position, 0-127)
 *   [int]   z (block position)
 *   [byte]  direction (face clicked, or -1)
 *   [short] block/item ID (-1 = empty hand)
 *   if itemId >= 0:
 *     [byte]  amount
 *     [short] damage
 *
 * This differs from v6 which puts itemId FIRST and has no conditional amount/damage.
 */
public class PlayerBlockPlacementPacketV14 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int direction;
    private short itemId;
    private byte amount;
    private short damage;

    public PlayerBlockPlacementPacketV14() {}

    public PlayerBlockPlacementPacketV14(int x, int y, int z, int direction, short itemId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.itemId = itemId;
        this.amount = 1;
        this.damage = 0;
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

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }
    @Override
    public int getZ() { return z; }
    @Override
    public int getDirection() { return direction; }
    @Override
    public short getItemId() { return itemId; }
    public byte getAmount() { return amount; }
    public short getDamage() { return damage; }
}
