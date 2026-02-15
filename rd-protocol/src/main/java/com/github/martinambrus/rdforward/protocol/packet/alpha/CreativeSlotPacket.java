package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta 1.8+ protocol 0x6B (bidirectional): Creative Inventory Action.
 *
 * Sent when the player picks an item from the creative inventory or
 * clears a slot. The server should accept and silently consume this.
 *
 * Unlike WindowClick's conditional item data, CreativeSlot always sends
 * all four fields unconditionally — even when itemId is -1 (empty).
 * All fields are shorts (confirmed by decompiling the Beta 1.8 client).
 *
 * Wire format (8 bytes):
 *   [short] slot ID (-1 = drop from cursor)
 *   [short] item ID (-1 if empty/clearing)
 *   [short] count
 *   [short] damage
 */
public class CreativeSlotPacket implements Packet {

    private short slotId;
    private short itemId;
    private short count;
    private short damage;

    public CreativeSlotPacket() {}

    @Override
    public int getPacketId() {
        return 0x6B;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(slotId);
        buf.writeShort(itemId);
        buf.writeShort(count);
        buf.writeShort(damage);
    }

    @Override
    public void read(ByteBuf buf) {
        slotId = buf.readShort();
        itemId = buf.readShort();
        count = buf.readShort();
        damage = buf.readShort();
    }

    public short getSlotId() { return slotId; }
    public short getItemId() { return itemId; }
    public short getCount() { return count; }
    public short getDamage() { return damage; }
}
