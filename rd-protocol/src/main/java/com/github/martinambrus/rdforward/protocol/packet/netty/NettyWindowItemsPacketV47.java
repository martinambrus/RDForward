package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x30: Window Items.
 *
 * Same structure as 1.7.2 but uses V47 slot format (byte TAG_End for no NBT).
 *
 * Wire format:
 *   [ubyte] windowId
 *   [short] count
 *   [slot]  slotData * count (V47 format)
 */
public class NettyWindowItemsPacketV47 implements Packet {

    private int windowId;
    private short[] itemIds;
    private byte[] counts;
    private short[] damages;

    public NettyWindowItemsPacketV47() {}

    public NettyWindowItemsPacketV47(int windowId, short[] itemIds, byte[] counts, short[] damages) {
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
                McDataTypes.writeEmptyV47Slot(buf);
            } else {
                McDataTypes.writeV47SlotItem(buf, itemIds[i], counts[i], damages[i]);
            }
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readUnsignedByte();
        int count = buf.readShort();
        for (int i = 0; i < count; i++) {
            McDataTypes.skipV47SlotData(buf);
        }
    }
}
