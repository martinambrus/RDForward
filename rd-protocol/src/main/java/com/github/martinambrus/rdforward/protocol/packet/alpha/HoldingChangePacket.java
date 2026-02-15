package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x10 (Client -> Server): Holding Change.
 *
 * Sent when the player scrolls or presses a number key to switch
 * the currently held item slot.
 *
 * Wire format (6 bytes payload):
 *   [int]   entity ID
 *   [short] slot ID (0-8, the hotbar slot index)
 */
public class HoldingChangePacket implements Packet {

    private int entityId;
    private short slotId;

    public HoldingChangePacket() {}

    public HoldingChangePacket(int entityId, short slotId) {
        this.entityId = entityId;
        this.slotId = slotId;
    }

    @Override
    public int getPacketId() {
        return 0x10;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeShort(slotId);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        slotId = buf.readShort();
    }

    public int getEntityId() { return entityId; }
    public short getSlotId() { return slotId; }
}
