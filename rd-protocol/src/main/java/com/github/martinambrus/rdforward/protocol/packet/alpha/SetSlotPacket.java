package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x67 (Server -> Client): Set Slot.
 *
 * Sets a single inventory slot to a specific item.
 *
 * Wire format:
 *   [byte]  window ID (0 = player inventory)
 *   [short] slot index (36-44 = hotbar slots 0-8)
 *   [short] item ID (-1 = empty slot)
 *   if item ID >= 0:
 *     [byte]  count
 *     [short] damage/metadata
 */
public class SetSlotPacket implements Packet {

    private int windowId;
    private int slot;
    private short itemId;
    private byte count;
    private short damage;

    public SetSlotPacket() {}

    public SetSlotPacket(int windowId, int slot, int itemId, int count, int damage) {
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
        }
    }

    public int getWindowId() { return windowId; }
    public int getSlot() { return slot; }
    public short getItemId() { return itemId; }
    public byte getCount() { return count; }
    public short getDamage() { return damage; }
}
