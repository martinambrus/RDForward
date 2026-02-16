package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0x6B (Client -> Server): Creative Inventory Action.
 *
 * Same as V22 but NBT is unconditional for ALL items (not just damageable).
 *
 * Wire format:
 *   [short] slot ID (-1 = drop from cursor)
 *   [short] item ID (-1 if empty/clearing)
 *   if item ID >= 0:
 *     [byte]  count
 *     [short] damage
 *     [short] nbt length (-1 = no NBT, >=0 = gzipped NBT bytes follow)
 */
public class CreativeSlotPacketV39 implements Packet {

    private short slotId;
    private short itemId;
    private byte count;
    private short damage;

    public CreativeSlotPacketV39() {}

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
            McDataTypes.writeEmptyNbtItemTag(buf);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        slotId = buf.readShort();
        itemId = buf.readShort();
        if (itemId >= 0) {
            count = buf.readByte();
            damage = buf.readShort();
            McDataTypes.skipNbtItemTag(buf);
        }
    }

    public short getSlotId() { return slotId; }
    public short getItemId() { return itemId; }
    public byte getCount() { return count; }
    public short getDamage() { return damage; }
}
