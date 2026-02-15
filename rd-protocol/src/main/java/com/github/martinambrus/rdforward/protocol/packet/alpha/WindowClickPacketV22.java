package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.0.0+ protocol 0x66 (Client -> Server): Window Click.
 *
 * Same as Beta 1.5+ (WindowClickPacketBeta15) but damageable items have
 * NBT tag data appended after the damage field. Non-damageable items have
 * NO NBT data — the client conditionally reads/writes NBT based on
 * Item.isDamageable().
 *
 * Wire format:
 *   [byte]  window ID
 *   [short] slot
 *   [byte]  right-click (0 = left, 1 = right)
 *   [short] action number
 *   [byte]  shift (0 = normal, 1 = shift-click)
 *   [short] item ID (-1 = empty)
 *   if item ID >= 0:
 *     [byte]  count
 *     [short] damage/metadata
 *     if damageable item:
 *       [short] nbt length (-1 = no NBT, >0 = gzipped NBT bytes follow)
 */
public class WindowClickPacketV22 extends WindowClickPacket {

    private int windowId;
    private short slot;
    private byte rightClick;
    private short actionNum;
    private byte shift;
    private short itemId;
    private byte count;
    private short damage;

    public WindowClickPacketV22() {}

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(slot);
        buf.writeByte(rightClick);
        buf.writeShort(actionNum);
        buf.writeByte(shift);
        buf.writeShort(itemId);
        if (itemId >= 0) {
            buf.writeByte(count);
            buf.writeShort(damage);
            if (McDataTypes.isNbtDamageableItem(itemId)) {
                McDataTypes.writeEmptyNbtItemTag(buf);
            }
        }
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        slot = buf.readShort();
        rightClick = buf.readByte();
        actionNum = buf.readShort();
        shift = buf.readByte();
        itemId = buf.readShort();
        if (itemId >= 0) {
            count = buf.readByte();
            damage = buf.readShort();
            if (McDataTypes.isNbtDamageableItem(itemId)) {
                McDataTypes.skipNbtItemTag(buf);
            }
        }
    }

    @Override
    public int getWindowId() { return windowId; }
    @Override
    public short getSlot() { return slot; }
    @Override
    public byte getRightClick() { return rightClick; }
    @Override
    public short getActionNum() { return actionNum; }
    public byte getShift() { return shift; }
    @Override
    public short getItemId() { return itemId; }
    @Override
    public byte getCount() { return count; }
    @Override
    public byte getDamage() { return (byte) damage; }
    public short getDamageShort() { return damage; }
}
