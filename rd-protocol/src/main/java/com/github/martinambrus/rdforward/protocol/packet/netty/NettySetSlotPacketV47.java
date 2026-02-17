package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x2F: Set Slot.
 *
 * Same structure as 1.7.2 but uses V47 slot format (byte TAG_End for no NBT).
 *
 * Wire format:
 *   [byte]  windowId
 *   [short] slotIndex
 *   [slot]  slotData (V47 format)
 */
public class NettySetSlotPacketV47 implements Packet {

    private int windowId;
    private int slotIndex;
    private int itemId;
    private int count;
    private int damage;

    public NettySetSlotPacketV47() {}

    public NettySetSlotPacketV47(int windowId, int slotIndex, int itemId, int count, int damage) {
        this.windowId = windowId;
        this.slotIndex = slotIndex;
        this.itemId = itemId;
        this.count = count;
        this.damage = damage;
    }

    @Override
    public int getPacketId() { return 0x2F; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(slotIndex);
        if (itemId < 0) {
            McDataTypes.writeEmptyV47Slot(buf);
        } else {
            McDataTypes.writeV47SlotItem(buf, itemId, count, damage);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        slotIndex = buf.readShort();
        itemId = buf.readShort();
        if (itemId >= 0) {
            count = buf.readByte();
            damage = buf.readShort();
            McDataTypes.skipV47SlotNbt(buf);
        }
    }

    public int getWindowId() { return windowId; }
    public int getSlotIndex() { return slotIndex; }
    public int getItemId() { return itemId; }
    public int getCount() { return count; }
}
