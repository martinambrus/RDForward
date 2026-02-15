package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x05 (Server -> Client): Entity Equipment.
 *
 * Replaces Alpha's PlayerInventory (0x05) in Beta. Sends a single
 * equipment slot for an entity (held item, armor).
 *
 * Wire format (8 bytes payload):
 *   [int]   entity ID
 *   [short] slot (0 = held item, 1-4 = armor)
 *   [short] item ID (-1 = empty)
 */
public class EntityEquipmentPacket implements Packet {

    private int entityId;
    private short slot;
    private short itemId;

    public EntityEquipmentPacket() {}

    public EntityEquipmentPacket(int entityId, int slot, int itemId) {
        this.entityId = entityId;
        this.slot = (short) slot;
        this.itemId = (short) itemId;
    }

    @Override
    public int getPacketId() {
        return 0x05;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeShort(slot);
        buf.writeShort(itemId);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        slot = buf.readShort();
        itemId = buf.readShort();
    }

    public int getEntityId() { return entityId; }
    public short getSlot() { return slot; }
    public short getItemId() { return itemId; }
}
