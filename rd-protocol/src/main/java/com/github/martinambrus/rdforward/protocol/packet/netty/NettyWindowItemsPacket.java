package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, S2C packet 0x30: Window Items.
 *
 * Wire format:
 *   [ubyte] windowId
 *   [short] count
 *   [slot]  slotData * count (Netty format)
 */
public class NettyWindowItemsPacket implements Packet {

    private int windowId;
    private short[] itemIds;
    private byte[] counts;
    private short[] damages;

    public NettyWindowItemsPacket() {}

    public NettyWindowItemsPacket(int windowId, short[] itemIds, byte[] counts, short[] damages) {
        this.windowId = windowId;
        this.itemIds = itemIds;
        this.counts = counts;
        this.damages = damages;
    }

    @Override
    public int getPacketId() { return 0x30; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(itemIds.length);
        for (int i = 0; i < itemIds.length; i++) {
            if (itemIds[i] < 0) {
                McDataTypes.writeEmptyNettySlot(buf);
            } else {
                McDataTypes.writeNettySlotItem(buf, itemIds[i], counts[i], damages[i]);
            }
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readUnsignedByte();
        int count = buf.readShort();
        itemIds = new short[count];
        counts = new byte[count];
        damages = new short[count];
        for (int i = 0; i < count; i++) {
            McDataTypes.skipNettySlotData(buf);
        }
    }
}
