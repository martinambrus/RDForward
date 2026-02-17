package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.13 Play state, S2C packet 0x17: Set Slot.
 *
 * 1.13 removed the damage field from item slots ("The Flattening").
 * Slot format: [short itemId (-1=empty), byte count, NBT (TAG_End)]
 *
 * Wire format:
 *   [byte]  windowId
 *   [short] slotIndex
 *   [short] itemId (-1 = empty)
 *   [byte]  count (if itemId >= 0)
 *   [byte]  0x00 TAG_End (if itemId >= 0, no NBT)
 */
public class NettySetSlotPacketV393 implements Packet {

    private int windowId;
    private int slotIndex;
    private int itemId;
    private int count;

    public NettySetSlotPacketV393() {}

    public NettySetSlotPacketV393(int windowId, int slotIndex, int itemId, int count) {
        this.windowId = windowId;
        this.slotIndex = slotIndex;
        this.itemId = itemId;
        this.count = count;
    }

    @Override
    public int getPacketId() { return 0x17; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(slotIndex);
        if (itemId < 0) {
            buf.writeShort(-1); // empty slot
        } else {
            buf.writeShort(itemId);
            buf.writeByte(count);
            buf.writeByte(0x00); // TAG_End = no NBT
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        slotIndex = buf.readShort();
        itemId = buf.readShort();
        if (itemId >= 0) {
            count = buf.readByte();
            McDataTypes.skipV47SlotNbt(buf);
        }
    }

    public int getWindowId() { return windowId; }
    public int getSlotIndex() { return slotIndex; }
    public int getItemId() { return itemId; }
    public int getCount() { return count; }
}
