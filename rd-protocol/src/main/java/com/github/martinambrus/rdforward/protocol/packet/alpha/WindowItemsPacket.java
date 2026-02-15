package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x68 (Server -> Client): Window Items.
 *
 * Sends all slots of a window to the client.
 *
 * Wire format:
 *   [byte]  window ID
 *   [short] count
 *   per slot:
 *     [short] item ID (-1 = empty)
 *     if item ID >= 0:
 *       [byte]  count
 *       [short] damage/metadata
 */
public class WindowItemsPacket implements Packet {

    private int windowId;
    private short[] itemIds;
    private byte[] counts;
    private short[] damages;

    public WindowItemsPacket() {}

    public WindowItemsPacket(int windowId, short[] itemIds, byte[] counts, short[] damages) {
        this.windowId = windowId;
        this.itemIds = itemIds;
        this.counts = counts;
        this.damages = damages;
    }

    @Override
    public int getPacketId() {
        return 0x68;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        int slotCount = itemIds != null ? itemIds.length : 0;
        buf.writeShort(slotCount);
        for (int i = 0; i < slotCount; i++) {
            buf.writeShort(itemIds[i]);
            if (itemIds[i] >= 0) {
                buf.writeByte(counts[i]);
                buf.writeShort(damages[i]);
            }
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        int slotCount = buf.readShort();
        itemIds = new short[slotCount];
        counts = new byte[slotCount];
        damages = new short[slotCount];
        for (int i = 0; i < slotCount; i++) {
            itemIds[i] = buf.readShort();
            if (itemIds[i] >= 0) {
                counts[i] = buf.readByte();
                damages[i] = buf.readShort();
            }
        }
    }

    public int getWindowId() { return windowId; }
    public short[] getItemIds() { return itemIds; }
    public byte[] getCounts() { return counts; }
    public short[] getDamages() { return damages; }
}
