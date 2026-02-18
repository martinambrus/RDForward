package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.17.1 Play state, S2C packet 0x16: Set Container Slot.
 *
 * 1.17.1 added a VarInt stateId field before windowId. The state ID is
 * used for inventory synchronization — the client sends it back in
 * Click Container to confirm it saw the latest server state.
 *
 * Wire format:
 *   [VarInt]  stateId (NEW in 1.17.1)
 *   [byte]    windowId
 *   [short]   slotIndex
 *   [boolean] present
 *   [VarInt]  itemId (if present)
 *   [byte]    count (if present)
 *   [byte]    0x00 TAG_End (if present, no NBT)
 */
public class NettySetSlotPacketV756 implements Packet {

    private int stateId;
    private int windowId;
    private int slotIndex;
    private int itemId;
    private int count;

    public NettySetSlotPacketV756() {}

    public NettySetSlotPacketV756(int stateId, int windowId, int slotIndex, int itemId, int count) {
        this.stateId = stateId;
        this.windowId = windowId;
        this.slotIndex = slotIndex;
        this.itemId = itemId;
        this.count = count;
    }

    @Override
    public int getPacketId() { return 0x16; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, stateId);
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
        stateId = McDataTypes.readVarInt(buf);
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

    public int getStateId() { return stateId; }
    public int getWindowId() { return windowId; }
    public int getSlotIndex() { return slotIndex; }
    public int getItemId() { return itemId; }
    public int getCount() { return count; }
}
