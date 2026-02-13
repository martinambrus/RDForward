package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x10 (Client -> Server): Holding Change.
 *
 * Sent when the player scrolls or presses a number key to switch
 * the currently held item slot.
 *
 * Wire format (2 bytes payload):
 *   [short] slot ID (0-8, the hotbar slot index)
 */
public class HoldingChangePacket implements Packet {

    private short slotId;

    public HoldingChangePacket() {}

    public HoldingChangePacket(short slotId) {
        this.slotId = slotId;
    }

    @Override
    public int getPacketId() {
        return 0x10;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(slotId);
    }

    @Override
    public void read(ByteBuf buf) {
        slotId = buf.readShort();
    }

    public short getSlotId() { return slotId; }
}
