package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.5 Play state, S2C packet 0x15: Set Container Slot.
 *
 * 1.20.5 replaced the boolean-present item slot format with data components:
 * count comes first (0 = empty), followed by itemId and component counts
 * only if count > 0.
 *
 * Wire format:
 *   [VarInt]  stateId
 *   [byte]    windowId
 *   [short]   slotIndex
 *   [VarInt]  count (0 = empty)
 *   if count > 0:
 *     [VarInt]  itemId
 *     [VarInt]  addedComponentCount (0)
 *     [VarInt]  removedComponentCount (0)
 */
public class NettySetSlotPacketV766 implements Packet {

    private int stateId;
    private int windowId;
    private int slotIndex;
    private int itemId;
    private int count;

    public NettySetSlotPacketV766() {}

    public NettySetSlotPacketV766(int stateId, int windowId, int slotIndex, int itemId, int count) {
        this.stateId = stateId;
        this.windowId = windowId;
        this.slotIndex = slotIndex;
        this.itemId = itemId;
        this.count = count;
    }

    @Override
    public int getPacketId() { return 0x15; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, stateId);
        buf.writeByte(windowId);
        buf.writeShort(slotIndex);
        McDataTypes.writeVarInt(buf, count);
        if (count > 0) {
            McDataTypes.writeVarInt(buf, itemId);
            McDataTypes.writeVarInt(buf, 0); // addedComponentCount
            McDataTypes.writeVarInt(buf, 0); // removedComponentCount
        }
    }

    @Override
    public void read(ByteBuf buf) {
        stateId = McDataTypes.readVarInt(buf);
        windowId = buf.readByte();
        slotIndex = buf.readShort();
        count = McDataTypes.readVarInt(buf);
        if (count > 0) {
            itemId = McDataTypes.readVarInt(buf);
            int addedCount = McDataTypes.readVarInt(buf);
            int removedCount = McDataTypes.readVarInt(buf);
            // Skip component data (we don't need to parse it)
            // For now, only handle items with 0 components
        } else {
            itemId = -1;
        }
    }

    public int getStateId() { return stateId; }
    public int getWindowId() { return windowId; }
    public int getSlotIndex() { return slotIndex; }
    public int getItemId() { return itemId; }
    public int getCount() { return count; }
}
