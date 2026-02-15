package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta 1.8+ protocol 0x0F (Client -> Server): Player Block Placement.
 *
 * Same wire format as PlayerBlockPlacementPacketBeta but with short damage
 * instead of byte. Beta 1.8 (v17) no longer needs the phantom KeepAlive trick
 * (it has KeepAlivePacketV17 with int ID), so damage uses its native short type.
 *
 * Wire format:
 *   [int]   x (block position)
 *   [byte]  y (block position, 0-127)
 *   [int]   z (block position)
 *   [byte]  direction (face clicked, or -1)
 *   [short] item ID (-1 = empty hand)
 *   if item ID >= 0:
 *     [byte]  amount
 *     [short] damage/metadata
 */
public class PlayerBlockPlacementPacketV17 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int direction;
    private short itemId;
    private byte amount;
    private short damage;

    public PlayerBlockPlacementPacketV17() {}

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
