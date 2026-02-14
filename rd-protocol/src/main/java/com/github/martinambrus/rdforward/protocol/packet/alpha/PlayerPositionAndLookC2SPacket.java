package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0D (Client -> Server): Player Position and Look.
 *
 * Sent when both position and look change. Combines 0x0B and 0x0C.
 *
 * NOTE: The C->S field order is (x, y, stance, z) but the S->C
 * field order swaps y and stance. See {@link PlayerPositionAndLookS2CPacket}.
 *
 * Wire format (41 bytes payload):
 *   [double]  x
 *   [double]  y (feet)
 *   [double]  stance (eyes)
 *   [double]  z
 *   [float]   yaw (degrees)
 *   [float]   pitch (degrees)
 *   [boolean] on ground
 */
public class PlayerPositionAndLookC2SPacket implements Packet {

    private double x;
    private double y;
    private double stance;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public PlayerPositionAndLookC2SPacket() {}

    public PlayerPositionAndLookC2SPacket(double x, double y, double stance, double z,
                                          float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.stance = stance;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public int getPacketId() {
        return 0x0D;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(stance);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        stance = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        onGround = buf.readBoolean();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getStance() { return stance; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isOnGround() { return onGround; }
}
