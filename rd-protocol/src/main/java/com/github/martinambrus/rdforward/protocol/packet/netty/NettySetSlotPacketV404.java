package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.13.2 Play state, S2C packet 0x17: Set Slot.
 *
 * 1.13.2 changed the item slot wire format from short itemId to
 * boolean present + VarInt itemId.
 *
 * Wire format:
 *   [byte]    windowId
 *   [short]   slotIndex
 *   [boolean] present
 *   [VarInt]  itemId (if present)
 *   [byte]    count (if present)
 *   [byte]    0x00 TAG_End (if present, no NBT)
 */
public class NettySetSlotPacketV404 implements Packet {

    private int windowId;
    private int slotIndex;
    private int itemId;
    private int count;

    public NettySetSlotPacketV404() {}

    public NettySetSlotPacketV404(int windowId, int slotIndex, int itemId, int count) {
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
            buf.writeBoolean(false); // not present
        } else {
            buf.writeBoolean(true);
            McDataTypes.writeVarInt(buf, itemId);
            buf.writeByte(count);
            buf.writeByte(0x00); // TAG_End = no NBT
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        slotIndex = buf.readShort();
        boolean present = buf.readBoolean();
        if (present) {
            itemId = McDataTypes.readVarInt(buf);
            count = buf.readByte();
            McDataTypes.skipV47SlotNbt(buf);
        } else {
            itemId = -1;
            count = 0;
        }
    }

    public int getWindowId() { return windowId; }
    public int getSlotIndex() { return slotIndex; }
    public int getItemId() { return itemId; }
    public int getCount() { return count; }
}
