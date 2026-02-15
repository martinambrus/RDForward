package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.0.0+ protocol 0x68 (Server -> Client): Window Items.
 *
 * Same as Beta WindowItemsPacket but damageable items have NBT tag data
 * appended after the damage field. Non-damageable items have NO NBT data —
 * the client conditionally reads/writes NBT based on Item.isDamageable().
 *
 * Wire format:
 *   [byte]  window ID
 *   [short] count
 *   per slot:
 *     [short] item ID (-1 = empty)
 *     if item ID >= 0:
 *       [byte]  count
 *       [short] damage/metadata
 *       if damageable item:
 *         [short] nbt length (-1 = no NBT, >0 = gzipped NBT bytes follow)
 */
public class WindowItemsPacketV22 implements Packet {

    private int windowId;
    private short[] itemIds;
    private byte[] counts;
    private short[] damages;

    public WindowItemsPacketV22() {}

    public WindowItemsPacketV22(int windowId, short[] itemIds, byte[] counts, short[] damages) {
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
                if (McDataTypes.isNbtDamageableItem(itemIds[i])) {
                    McDataTypes.writeEmptyNbtItemTag(buf);
                }
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
                if (McDataTypes.isNbtDamageableItem(itemIds[i])) {
                    McDataTypes.skipNbtItemTag(buf);
                }
            }
        }
    }

    public int getWindowId() { return windowId; }
    public short[] getItemIds() { return itemIds; }
    public byte[] getCounts() { return counts; }
    public short[] getDamages() { return damages; }
}
