package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0x67 (bidirectional): Set Slot.
 *
 * Same as V22 but NBT is unconditional for ALL items (not just damageable).
 *
 * Wire format:
 *   [byte]  window ID (0 = player inventory)
 *   [short] slot index (36-44 = hotbar slots 0-8)
 *   [short] item ID (-1 = empty slot)
 *   if item ID >= 0:
 *     [byte]  count
 *     [short] damage/metadata
 *     [short] nbt length (-1 = no NBT, >=0 = gzipped NBT bytes follow)
 */
public class SetSlotPacketV39 implements Packet {

    private int windowId;
    private int slot;
    private short itemId;
    private byte count;
    private short damage;

    public SetSlotPacketV39() {}

    public SetSlotPacketV39(int windowId, int slot, int itemId, int count, int damage) {
        this.windowId = windowId;
        this.slot = slot;
        this.itemId = (short) itemId;
        this.count = (byte) count;
        this.damage = (short) damage;
    }

    @Override
    public int getPacketId() {
        return 0x67;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(slot);
        buf.writeShort(itemId);
        if (itemId >= 0) {
            buf.writeByte(count);
            buf.writeShort(damage);
            McDataTypes.writeEmptyNbtItemTag(buf);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        slot = buf.readShort();
        itemId = buf.readShort();
        if (itemId >= 0) {
            count = buf.readByte();
            damage = buf.readShort();
            McDataTypes.skipNbtItemTag(buf);
        }
    }

    public int getWindowId() { return windowId; }
    public int getSlot() { return slot; }
    public short getItemId() { return itemId; }
    public byte getCount() { return count; }
    public short getDamage() { return damage; }
}
