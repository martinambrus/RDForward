package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x66 (Client -> Server): Window Click.
 *
 * Sent when the player clicks a slot in an open window/inventory.
 *
 * Wire format:
 *   [byte]  window ID
 *   [short] slot
 *   [byte]  right-click (0 = left, 1 = right)
 *   [short] action number
 *   [short] item ID (-1 = empty)
 *   if item ID >= 0:
 *     [byte]  count
 *     [byte]  damage/metadata
 */
public class WindowClickPacket implements Packet {

    private int windowId;
    private short slot;
    private byte rightClick;
    private short actionNum;
    private short itemId;
    private byte count;
    private byte damage;

    public WindowClickPacket() {}

    @Override
    public int getPacketId() {
        return 0x66;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(slot);
        buf.writeByte(rightClick);
        buf.writeShort(actionNum);
        buf.writeShort(itemId);
        if (itemId >= 0) {
            buf.writeByte(count);
            buf.writeByte(damage);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        slot = buf.readShort();
        rightClick = buf.readByte();
        actionNum = buf.readShort();
        itemId = buf.readShort();
        if (itemId >= 0) {
            count = buf.readByte();
            damage = buf.readByte();
        }
    }

    public int getWindowId() { return windowId; }
    public short getSlot() { return slot; }
    public byte getRightClick() { return rightClick; }
    public short getActionNum() { return actionNum; }
    public short getItemId() { return itemId; }
    public byte getCount() { return count; }
    public byte getDamage() { return damage; }
}
