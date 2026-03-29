package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.13+ Play state, S2C: Entity Event (a.k.a. Entity Status).
 *
 * Wire format:
 *   [int]  entity ID
 *   [byte] event ID
 *
 * OP-level events:
 *   24 = permission level 0 (non-op)
 *   25 = permission level 1
 *   26 = permission level 2
 *   27 = permission level 3
 *   28 = permission level 4
 */
public class NettyEntityEventPacket implements Packet {

    /** Base event ID for OP permission level updates. Add the desired level (0-4). */
    public static final byte OP_PERMISSION_BASE = 24;

    private int entityId;
    private byte eventId;

    public NettyEntityEventPacket() {}

    public NettyEntityEventPacket(int entityId, int eventId) {
        this.entityId = entityId;
        this.eventId = (byte) eventId;
    }

    /** Create an OP permission level update event. */
    public static NettyEntityEventPacket opLevel(int entityId, int level) {
        return new NettyEntityEventPacket(entityId, OP_PERMISSION_BASE + level);
    }

    @Override
    public int getPacketId() { return 0x1A; } // v393 default; registry handles remapping

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(eventId);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        eventId = buf.readByte();
    }
}
