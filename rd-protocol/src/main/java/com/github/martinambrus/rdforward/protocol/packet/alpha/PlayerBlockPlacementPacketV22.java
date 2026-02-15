package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.0.0+ protocol 0x0F (Client -> Server): Player Block Placement.
 *
 * Same as V17 but damageable items (tools, weapons, armor) have NBT tag data
 * appended after the damage field. Non-damageable items (blocks, most items)
 * have NO NBT data — the client conditionally reads/writes NBT based on
 * Item.isDamageable().
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
 *     if damageable item:
 *       [short] nbt length (-1 = no NBT, >0 = gzipped NBT bytes follow)
 */
public class PlayerBlockPlacementPacketV22 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int direction;
    private short itemId;
    private byte amount;
    private short damage;

    public PlayerBlockPlacementPacketV22() {}

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
            if (McDataTypes.isNbtDamageableItem(itemId)) {
                McDataTypes.writeEmptyNbtItemTag(buf);
            }
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
            if (McDataTypes.isNbtDamageableItem(itemId)) {
                McDataTypes.skipNbtItemTag(buf);
            }
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
