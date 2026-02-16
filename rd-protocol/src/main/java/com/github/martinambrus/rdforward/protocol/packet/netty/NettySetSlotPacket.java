package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, S2C packet 0x2F: Set Slot.
 *
 * Wire format:
 *   [byte]  windowId
 *   [short] slotIndex
 *   [slot]  slotData (Netty format: short itemId, if >= 0: byte count, short damage, byte TAG_End)
 */
public class NettySetSlotPacket implements Packet {

    private int windowId;
    private int slotIndex;
    private int itemId;
    private int count;
    private int damage;

    public NettySetSlotPacket() {}

    public NettySetSlotPacket(int windowId, int slotIndex, int itemId, int count, int damage) {
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
            McDataTypes.writeEmptyNettySlot(buf);
        } else {
            McDataTypes.writeNettySlotItem(buf, itemId, count, damage);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        slotIndex = buf.readShort();
        McDataTypes.skipNettySlotData(buf);
    }
}
