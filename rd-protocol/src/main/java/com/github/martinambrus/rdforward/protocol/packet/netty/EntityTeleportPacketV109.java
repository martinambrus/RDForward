package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, S2C packet 0x4A: Entity Teleport.
 *
 * 1.9 changed coordinates from fixed-point int to double.
 *
 * Wire format:
 *   [VarInt]  entityId
 *   [double]  x
 *   [double]  y
 *   [double]  z
 *   [byte]    yaw
 *   [byte]    pitch
 *   [boolean] onGround
 */
public class EntityTeleportPacketV109 implements Packet {

    private int entityId;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;

    public EntityTeleportPacketV109() {}

    public EntityTeleportPacketV109(int entityId, double x, double y, double z,
                                     int yaw, int pitch) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() { return 0x4A; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeBoolean(true); // onGround
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
        buf.readBoolean();
    }
}
