package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * S2C packet: Set Head Rotation (Entity Head Look).
 *
 * Sets the head yaw of an entity independently of the body yaw.
 * Without this packet, the client defaults head yaw to 0 (South),
 * causing players to appear to look in the wrong direction.
 *
 * Wire format:
 *   [VarInt] entityId
 *   [Byte]   headYaw (angle/256 * 360)
 *
 * Present in all Netty protocol versions (1.7.2 through 1.21.11+).
 * Packet ID varies per version — registered in NettyPacketRegistry.
 */
public class EntityHeadRotationPacket implements Packet {

    private int entityId;
    private int headYaw;

    public EntityHeadRotationPacket() {}

    public EntityHeadRotationPacket(int entityId, int headYaw) {
        this.entityId = entityId;
        this.headYaw = headYaw;
    }

    @Override
    public int getPacketId() { return 0x19; } // base ID (1.7.2), overridden by registry

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeByte(headYaw);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        headYaw = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public int getHeadYaw() { return headYaw; }
}
