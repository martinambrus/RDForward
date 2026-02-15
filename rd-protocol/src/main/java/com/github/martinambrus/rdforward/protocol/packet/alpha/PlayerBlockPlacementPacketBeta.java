package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x0F (Client -> Server): Player Block Placement.
 *
 * Wire format changed from Alpha: coordinates come first, and item data
 * is conditional (only present when itemId >= 0).
 *
 * Wire format:
 *   [int]   x (block position)
 *   [byte]  y (block position, 0-127)
 *   [int]   z (block position)
 *   [byte]  direction (face clicked, or -1)
 *   [short] item ID (-1 = empty hand)
 *   if item ID >= 0:
 *     [byte]  amount
 *     [byte]  damage/metadata
 */
public class PlayerBlockPlacementPacketBeta implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int direction;
    private short itemId;
    private byte amount;
    private byte damage;

    public PlayerBlockPlacementPacketBeta() {}

    public PlayerBlockPlacementPacketBeta(int x, int y, int z, int direction,
                                          short itemId, byte amount, byte damage) {
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
            buf.writeByte(damage);
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
            damage = buf.readByte();
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getDirection() { return direction; }
    public short getItemId() { return itemId; }
    public byte getAmount() { return amount; }
    public byte getDamage() { return damage; }
}
