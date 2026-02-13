package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x11 (Server -> Client): Add to Inventory.
 *
 * Adds an item stack to the player's inventory. The client determines
 * which slot to place the item in (first matching partial stack or
 * first empty slot).
 *
 * Wire format (5 bytes payload):
 *   [short] item ID
 *   [byte]  count
 *   [short] damage/durability
 */
public class AddToInventoryPacket implements Packet {

    private short itemId;
    private byte count;
    private short damage;

    public AddToInventoryPacket() {}

    public AddToInventoryPacket(int itemId, int count, int damage) {
        this.itemId = (short) itemId;
        this.count = (byte) count;
        this.damage = (short) damage;
    }

    @Override
    public int getPacketId() {
        return 0x11;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(itemId);
        buf.writeByte(count);
        buf.writeShort(damage);
    }

    @Override
    public void read(ByteBuf buf) {
        itemId = buf.readShort();
        count = buf.readByte();
        damage = buf.readShort();
    }

    public short getItemId() { return itemId; }
    public byte getCount() { return count; }
    public short getDamage() { return damage; }
}
