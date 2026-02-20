package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.2 Play state, S2C packet 0x20: Entity Position Sync.
 *
 * Replaces EntityTeleport (removed in v768). Uses float degrees for
 * yaw/pitch instead of byte angles, and adds delta movement velocity.
 *
 * Wire format:
 *   [VarInt]  entityId
 *   [double]  x
 *   [double]  y
 *   [double]  z
 *   [double]  deltaMovX (0.0)
 *   [double]  deltaMovY (0.0)
 *   [double]  deltaMovZ (0.0)
 *   [float]   yaw (degrees)
 *   [float]   pitch (degrees)
 *   [boolean] onGround
 */
public class EntityPositionSyncPacketV768 implements Packet {

    private int entityId;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public EntityPositionSyncPacketV768() {}

    /**
     * Constructor accepting byte angles (0-255) for compatibility with
     * existing callers that use EntityTeleportPacketV109 conventions.
     * Byte angles are converted to float degrees internally.
     */
    public EntityPositionSyncPacketV768(int entityId, double x, double y, double z,
                                         int yawByteAngle, int pitchByteAngle) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yawByteAngle * 360.0f / 256.0f;
        this.pitch = pitchByteAngle * 360.0f / 256.0f;
    }

    @Override
    public int getPacketId() { return 0x20; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(0.0); // deltaMovX
        buf.writeDouble(0.0); // deltaMovY
        buf.writeDouble(0.0); // deltaMovZ
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeBoolean(true); // onGround
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        buf.readDouble(); // deltaMovX
        buf.readDouble(); // deltaMovY
        buf.readDouble(); // deltaMovZ
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        buf.readBoolean(); // onGround
    }
}
