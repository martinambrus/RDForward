package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE Rotate Head packet (ID 0x23, S2C).
 *
 * Sets the head yaw of an entity (absolute, not delta).
 * Uses int entity ID (unlike MoveEntity packets which use short).
 *
 * Wire format:
 *   [int]  entity ID
 *   [byte] yHeadRot (absolute, packed byte rotation)
 */
public class LCERotateHeadPacket implements Packet {

    private int entityId;
    private int yHeadRot;

    public LCERotateHeadPacket() {}

    public LCERotateHeadPacket(int entityId, int yHeadRot) {
        this.entityId = entityId;
        this.yHeadRot = yHeadRot;
    }

    @Override
    public int getPacketId() { return 0x23; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(yHeadRot);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        yHeadRot = buf.readByte();
    }
}
