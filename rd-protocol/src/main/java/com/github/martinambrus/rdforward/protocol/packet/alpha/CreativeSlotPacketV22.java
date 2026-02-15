package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.0.0+ protocol 0x6B (Client -> Server): Creative Inventory Action.
 *
 * Unlike Beta 1.8's unconditional 4-short format, Release 1.0.0 uses the
 * standard conditional item slot format. Damageable items include NBT tag
 * data after the damage field; non-damageable items do not.
 *
 * Wire format:
 *   [short] slot ID (-1 = drop from cursor)
 *   [short] item ID (-1 if empty/clearing)
 *   if item ID >= 0:
 *     [byte]  count
 *     [short] damage
 *     if damageable item:
 *       [short] nbt length (-1 = no NBT, >0 = gzipped NBT bytes follow)
 */
public class CreativeSlotPacketV22 implements Packet {

    private short slotId;
    private short itemId;
    private byte count;
    private short damage;

    public CreativeSlotPacketV22() {}

    @Override
    public int getPacketId() {
        return 0x6B;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(slotId);
        buf.writeShort(itemId);
        if (itemId >= 0) {
            buf.writeByte(count);
            buf.writeShort(damage);
            if (McDataTypes.isNbtDamageableItem(itemId)) {
                McDataTypes.writeEmptyNbtItemTag(buf);
            }
        }
    }

    @Override
    public void read(ByteBuf buf) {
        slotId = buf.readShort();
        itemId = buf.readShort();
        if (itemId >= 0) {
            count = buf.readByte();
            damage = buf.readShort();
            if (McDataTypes.isNbtDamageableItem(itemId)) {
                McDataTypes.skipNbtItemTag(buf);
            }
        }
    }

    public short getSlotId() { return slotId; }
    public short getItemId() { return itemId; }
    public byte getCount() { return count; }
    public short getDamage() { return damage; }
}
